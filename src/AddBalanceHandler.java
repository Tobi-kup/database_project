import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class AddBalanceHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        try(Connection conn = Database.getConnection()){
            PreparedStatement ps = conn.prepareStatement("UPDATE users SET balance = balance + ? WHERE id = ?");
            ps.setDouble(1, Double.parseDouble(data.get("amount")));
            ps.setInt(2, Integer.parseInt(data.get("userId")));
            ps.executeUpdate();

            sendJson(exchange, "{\"success\":true}");
        } catch(Exception e){
            sendJson(exchange, "{\"success\":false,\"message\":\""+e.getMessage()+"\"}");
        }
    }

    private void sendJson(HttpExchange exchange, String json) throws IOException {
        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200,json.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(json.getBytes());
        os.close();
    }
}