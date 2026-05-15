import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.HashMap;

public class RegisterHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!"POST".equals(exchange.getRequestMethod())) return;

        String body = new String(exchange.getRequestBody().readAllBytes());
        HashMap<String,String> data = SPAHandler.parseJson(body);

        try(Connection conn = Database.getConnection()){
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO users(email,password,balance) VALUES(?,?,0)"
            );
            ps.setString(1, data.get("email"));
            ps.setString(2, data.get("password"));
            ps.execute();

            SPAHandler.send(exchange, "Registration successful!");
        }catch(Exception e){
            SPAHandler.send(exchange, "Error: " + e.getMessage());
        }
    }
}