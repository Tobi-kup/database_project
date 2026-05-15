import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

public class PortfolioHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();
        int userId = Integer.parseInt(query.split("=")[1]);

        JSONArray arr = new JSONArray();

        try(Connection conn = Database.getConnection()){

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT symbol, quantity, price FROM portfolio WHERE user_id=?"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            while(rs.next()){

                JSONObject obj = new JSONObject();

                obj.put("symbol", rs.getString("symbol"));
                obj.put("quantity", rs.getInt("quantity"));
                obj.put("avgPrice", rs.getDouble("price"));

                arr.put(obj);
            }

        } catch(Exception e){
            e.printStackTrace();
        }

        String response = arr.toString();

        exchange.getResponseHeaders()
                .set("Content-Type","application/json");

        exchange.sendResponseHeaders(200, response.length());

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}