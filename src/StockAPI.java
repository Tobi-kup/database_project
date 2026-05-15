import java.net.*;
import java.io.*;

public class StockAPI {

    private static final String API_KEY = "<YOUR_ALPHA_VANTAGE_KEY>"; // Hier eintragen

    // Holt Preis + %Change
    public static String getQuote(String symbol) throws Exception {
        String urlStr = "https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol="
                + symbol + "&apikey=" + API_KEY;
        URL url = new URL(urlStr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=in.readLine())!=null) sb.append(line);
        in.close();
        return sb.toString();
    }

    // Holt tägliche Preise für Chart
    public static String getDailyChart(String symbol) throws Exception {
        String urlStr = "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY_ADJUSTED&symbol="
                + symbol + "&apikey=" + API_KEY + "&outputsize=compact";
        URL url = new URL(urlStr);
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while((line=in.readLine())!=null) sb.append(line);
        in.close();
        return sb.toString();
    }
}