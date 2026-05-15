import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL      = "jdbc:postgresql://aws-0-eu-west-1.pooler.supabase.com:6543/postgres";
    private static final String USER     = "postgres.hntarmduljscudcbhlvk";
    private static final String PASS     = "database_project123";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}