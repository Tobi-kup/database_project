import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.net.HttpURLConnection;

public class StockAPI {

    private static final String API_KEY =
            "d83lv2hr01qkm5c8m9hgd83lv2hr01qkm5c8m9i0";

    public static JSONObject getQuote(String symbol) throws Exception {

        String urlStr =
                "https://finnhub.io/api/v1/quote?symbol="
                        + symbol
                        + "&token="
                        + API_KEY;

        String json = readUrl(urlStr);

        return new JSONObject(json);
    }

    public static double[] getChartData(String symbol) throws Exception {

        JSONObject quote = getQuote(symbol);

        double current = quote.getDouble("c");

        double[] fake = new double[30];

        double value = current;

        for(int i = 29; i >= 0; i--) {
            value += (Math.random() - 0.5) * 2;
            fake[i] = value;
        }

        return fake;
    }

    private static String readUrl(String urlStr) throws Exception {

        URL url = new URL(urlStr);

        HttpURLConnection conn =
                (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");

        conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0"
        );

        BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream())
        );

        StringBuilder sb = new StringBuilder();

        String line;

        while ((line = in.readLine()) != null) {
            sb.append(line);
        }

        in.close();

        return sb.toString();
    }
}