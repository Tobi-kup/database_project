import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database_Connection_Test {

    //direct connection:
    //    private static final String URL      = "jdbc:postgresql://db.hntarmduljscudcbhlvk.supabase.co:5432/postgres";
    //    private static final String USER     = "postgres";
    //    private static final String PASSWORD = "database_project123";

    //pooling
    private static final String URL      = "jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:6543/postgres";
    private static final String USER     = "postgres.hntarmduljscudcbhlvk";
    private static final String PASSWORD = "database_project123";

    public static void main(String[] args) {
        System.out.println("=== Supabase Connection Test ===");
        testConnection();
    }

    public static void testConnection() {
        System.out.println("Attempting to connect...");

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {

            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ SUCCESS: Connected to database!");
                System.out.println("   Driver:   " + conn.getMetaData().getDriverName());
                System.out.println("   URL:      " + conn.getMetaData().getURL());
                System.out.println("   DB Name:  " + conn.getCatalog());
                System.out.println("   Valid:    " + conn.isValid(5)); // 5 sec timeout check
            } else {
                System.out.println("❌ FAILED: Connection object is null or closed.");
            }

        } catch (SQLException e) {
            System.out.println("❌ FAILED: Could not connect to database.");
            System.out.println("   Error Code:    " + e.getErrorCode());
            System.out.println("   SQL State:     " + e.getSQLState());
            System.out.println("   Message:       " + e.getMessage());
            printErrorHint(e);
        }
    }

    private static void printErrorHint(SQLException e) {
        String msg = e.getMessage().toLowerCase();

        if (msg.contains("password"))
            System.out.println("💡 Hint: Password seems wrong. Check your Supabase DB password.");
        else if (msg.contains("connection refused") || msg.contains("connect"))
            System.out.println("💡 Hint: Could not reach the server. Check your internet or the URL.");
        else if (msg.contains("driver"))
            System.out.println("💡 Hint: PostgreSQL JDBC driver not found. Add the .jar to your classpath.");
        else
            System.out.println("💡 Hint: Check your URL, user, and password.");
    }
}