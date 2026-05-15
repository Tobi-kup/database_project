import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;

public class LoginHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        try(Connection conn = Database.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "SELECT id, balance FROM users WHERE email=? AND password=?"
            );
            ps.setString(1, data.get("email"));
            ps.setString(2, data.get("password"));
            ResultSet rs = ps.executeQuery();

            if(rs.next()){
                int userId = rs.getInt("id");
                double balance = rs.getDouble("balance");

                // Sende als einfacher Text für Frontend-Kompatibilität
                SPAHandler.send(exchange, "Login successful! Balance: " + balance);
            } else {
                SPAHandler.send(exchange, "Invalid login credentials!");
            }
        }catch(Exception e){
            SPAHandler.send(exchange, "Error: " + e.getMessage());
        }
    }
}