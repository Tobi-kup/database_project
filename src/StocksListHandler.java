import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.util.List;
import org.json.JSONArray;

public class StocksListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) {

        try {

            List<org.json.JSONObject> stocks =
                    StocksCache.getStocks();

            JSONArray arr = new JSONArray(stocks);

            String response = arr.toString();

            exchange.getResponseHeaders()
                    .set("Content-Type", "application/json");

            exchange.sendResponseHeaders(200, response.length());

            OutputStream os = exchange.getResponseBody();
            os.write(response.getBytes());
            os.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}