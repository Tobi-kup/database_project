import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import java.util.HashMap;

public class SellStockHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        int userId = Integer.parseInt(data.get("userId"));
        String symbol = data.get("symbol");
        int quantity = Integer.parseInt(data.get("quantity"));
        double price = Double.parseDouble(data.get("price"));

        try(Connection conn = Database.getConnection()){

            conn.setAutoCommit(false);

            // 1. Get position
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT quantity, price FROM portfolio WHERE user_id=? AND symbol=?"
            );

            ps.setInt(1, userId);
            ps.setString(2, symbol);

            ResultSet rs = ps.executeQuery();

            if(!rs.next()){
                send(exchange, "{\"success\":false,\"message\":\"No position\"}");
                return;
            }

            int currentQty = rs.getInt("quantity");

            if(quantity > currentQty){
                send(exchange, "{\"success\":false,\"message\":\"Not enough shares\"}");
                return;
            }

            double revenue = quantity * price;

            // 2. Update or delete position
            int newQty = currentQty - quantity;

            if(newQty == 0){

                PreparedStatement del = conn.prepareStatement(
                        "DELETE FROM portfolio WHERE user_id=? AND symbol=?"
                );

                del.setInt(1, userId);
                del.setString(2, symbol);

                del.executeUpdate();

            } else {

                PreparedStatement upd = conn.prepareStatement(
                        "UPDATE portfolio SET quantity=? WHERE user_id=? AND symbol=?"
                );

                upd.setInt(1, newQty);
                upd.setInt(2, userId);
                upd.setString(3, symbol);

                upd.executeUpdate();
            }

            // 3. Add money back
            PreparedStatement ps2 = conn.prepareStatement(
                    "UPDATE users SET balance = balance + ? WHERE id=?"
            );

            ps2.setDouble(1, revenue);
            ps2.setInt(2, userId);

            ps2.executeUpdate();

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