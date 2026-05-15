import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database_Setup {

    private static final String URL      = "jdbc:postgresql://db.hntarmduljscudcbhlvk.supabase.co:5432/postgres";
    private static final String USER     = "postgres";
    private static final String PASSWORD = "database_project123";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            createTables(conn);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        Statement stmt = conn.createStatement();

        // Create a users table
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                id        SERIAL PRIMARY KEY,
                username  VARCHAR(50)  NOT NULL UNIQUE,
                email     VARCHAR(100) NOT NULL UNIQUE,
                password  VARCHAR(255) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            );
        """;

        stmt.execute(createUsers);
        System.out.println("✅ Table 'users' created (or already exists).");
        stmt.close();
    }
}