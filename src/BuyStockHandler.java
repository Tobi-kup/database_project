import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.HashMap;

public class BuyStockHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        int userId = Integer.parseInt(data.get("userId"));
        String symbol = data.get("symbol");
        int quantity = Integer.parseInt(data.get("quantity"));
        double price = Double.parseDouble(data.get("price"));

        double totalCost = quantity * price;

        try(Connection conn = Database.getConnection()){

            conn.setAutoCommit(false);

            // 1. Check balance
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM users WHERE id=?"
            );
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if(!rs.next()){
                send(exchange, "{\"success\":false,\"message\":\"User not found\"}");
                return;
            }

            double balance = rs.getDouble("balance");

            if(balance < totalCost){
                send(exchange, "{\"success\":false,\"message\":\"Not enough money\"}");
                return;
            }

            // 2. Deduct balance
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE users SET balance = balance - ? WHERE id=?"
            );
            ps2.setDouble(1, totalCost);
            ps2.setInt(2, userId);
            ps2.executeUpdate();

            // 3. Update portfolio (upsert logic)
            PreparedStatement ps3 = conn.prepareStatement(
                    "SELECT quantity, price FROM portfolio WHERE user_id=? AND symbol=?"
            );
            ps3.setInt(1, userId);
            ps3.setString(2, symbol);

            ResultSet pr = ps3.executeQuery();

            if(pr.next()){

                int oldQty = pr.getInt("quantity");
                double oldAvg = pr.getDouble("price");

                int newQty = oldQty + quantity;
                double newAvg = ((oldQty * oldAvg) + totalCost) / newQty;

                PreparedStatement ps4 = conn.prepareStatement(
                        "UPDATE portfolio SET quantity=?, price=? WHERE user_id=? AND symbol=?"
                );

                ps4.setInt(1, newQty);
                ps4.setDouble(2, newAvg);
                ps4.setInt(3, userId);
                ps4.setString(4, symbol);

                ps4.executeUpdate();

            } else {

                PreparedStatement ps4 = conn.prepareStatement(
                        "INSERT INTO portfolio(user_id, symbol, quantity, price) VALUES(?,?,?,?)"
                );

                ps4.setInt(1, userId);
                ps4.setString(2, symbol);
                ps4.setInt(3, quantity);
                ps4.setDouble(4, price);

                ps4.executeUpdate();
            }

            conn.commit();

            send(exchange, "{\"success\":true}");

        } catch(Exception e){
            send(exchange, "{\"success\":false,\"message\":\""+e.getMessage()+"\"}");
        }
    }

    private void send(HttpExchange exchange, String json) throws IOException {

        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}