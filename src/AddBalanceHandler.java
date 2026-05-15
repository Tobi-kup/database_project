import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;
import org.json.JSONObject;

public class AddBalanceHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        JSONObject response = new JSONObject();

        try(Connection conn = Database.getConnection()){

            PreparedStatement ps = conn.prepareStatement(
                    "UPDATE users SET balance = balance + ? WHERE id = ?"
            );

            ps.setDouble(1, Double.parseDouble(data.get("amount")));
            ps.setInt(2, Integer.parseInt(data.get("userId")));

            int updated = ps.executeUpdate();

            response.put("success", updated > 0);

        } catch(Exception e){
            response.put("success", false);
            response.put("message", e.getMessage());
        }

        sendJson(exchange, response.toString());
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {

        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200, json.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}