import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BalanceHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String query = exchange.getRequestURI().getQuery();
        int userId = Integer.parseInt(query.split("=")[1]);

        try(Connection conn = Database.getConnection()){

            PreparedStatement ps = conn.prepareStatement(
                    "SELECT balance FROM users WHERE id=?"
            );

            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            double balance = 0;

            if(rs.next()){
                balance = rs.getDouble("balance");
            }

            String json = "{\"balance\":" + balance + "}";

            send(exchange, json);

        } catch(Exception e){
            send(exchange, "{\"error\":\""+e.getMessage()+"\"}");
        }
    }

    private void send(HttpExchange exchange, String response) throws IOException {

        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}