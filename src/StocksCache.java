import org.json.JSONObject;
import org.json.JSONArray;

import java.sql.*;
import java.util.*;

public class StocksCache {

    private static final String[] SYMBOLS = {
            "AAPL","GOOG","MSFT","NVDA","TSLA",
            "AMZN","META","NFLX","AMD","INTC"
    };

    public static void startAutoUpdate() {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    updateAllStocks();
                    Thread.sleep(5 * 60 * 1000); // 5 min
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        });
        t.setDaemon(true);
        t.start();
    }


    public static void updateAllStocks() {
        try (Connection conn = Database.getConnection()) {
            for (String symbol : SYMBOLS) {
                JSONObject quote = StockAPI.getQuote(symbol);
                double price = quote.getDouble("c");
                double change = quote.getDouble("dp");

                double[] chartArr = StockAPI.getChartData(symbol);

                JSONArray chartJson = new JSONArray();
                for (double v : chartArr) {
                    chartJson.put(v);
                }

                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO stock_cache(symbol, price, change_percent, chart, last_updated) " +
                                "VALUES (?, ?, ?, ?, ?) " +
                                "ON CONFLICT (symbol) DO UPDATE SET " +
                                "price = EXCLUDED.price, " +
                                "change_percent = EXCLUDED.change_percent, " +
                                "chart = EXCLUDED.chart, " +
                                "last_updated = EXCLUDED.last_updated"
                );

                ps.setString(1, symbol);
                ps.setDouble(2, price);
                ps.setString(3, change + "%");
                ps.setString(4, chartJson.toString());
                ps.setLong(5, System.currentTimeMillis());

                ps.executeUpdate();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<JSONObject> getStocks() {
        List<JSONObject> list = new ArrayList<>();
        try (Connection conn = Database.getConnection()) {
            PreparedStatement ps =
                    conn.prepareStatement("SELECT * FROM stock_cache");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                JSONObject obj = new JSONObject();

                obj.put("symbol", rs.getString("symbol"));
                obj.put("price", rs.getDouble("price"));
                obj.put("change", rs.getString("change_percent"));

                JSONArray chart =
                        new JSONArray(rs.getString("chart"));

                obj.put("chart", chart);

                list.add(obj);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}