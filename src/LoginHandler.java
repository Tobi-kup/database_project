import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import org.json.JSONObject;
import java.util.HashMap;

public class LoginHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        JSONObject response = new JSONObject();

        try(Connection conn = Database.getConnection()){

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, balance FROM users WHERE email=? AND password=?"
            );

            ps.setString(1, data.get("email"));
            ps.setString(2, data.get("password"));

            ResultSet rs = ps.executeQuery();

            if(rs.next()){

                response.put("success", true);
                response.put("userId", rs.getInt("id"));
                response.put("balance", rs.getDouble("balance"));

            } else {
                response.put("success", false);
                response.put("message", "Invalid credentials");
            }

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