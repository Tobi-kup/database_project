import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONObject;
import org.json.JSONArray;

public class StocksListHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws java.io.IOException {

        String[] symbols = {"AAPL","GOOG","MSFT","NVDA","TSLA"};
        ArrayList<HashMap<String,String>> stocks = new ArrayList<>();

        for(String s : symbols){
            HashMap<String,String> stock = new HashMap<>();
            stock.put("symbol", s);

            try{
                String json = StockAPI.getQuote(s);
                JSONObject obj = new JSONObject(json).getJSONObject("Global Quote");
                stock.put("price", obj.getString("05. price"));
                stock.put("change", obj.getString("10. change percent"));
            } catch(Exception e){
                stock.put("price","N/A");
                stock.put("change","N/A");
            }

            stocks.add(stock);
        }

        JSONArray arr = new JSONArray(stocks);
        String response = arr.toString();

        exchange.getResponseHeaders().set("Content-Type","application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }
}