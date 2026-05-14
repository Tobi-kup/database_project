import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

public class Database_Creator {

  public static void main(String[] args) {

    String url = "jdbc:postgresql://db.hntarmduljscudcbhlvk.supabase.co:5432/postgres";
    String user = "postgres";
    String password = "database_project123";

    try {
      Connection conn = DriverManager.getConnection(url, user, password);
      System.out.println("Verbunden!");

      // Show every table name
      DatabaseMetaData metaData = conn.getMetaData();
      ResultSet tables = metaData.getTables(null, "public", "%", new String[]{"TABLE"});
      while (tables.next()) {
        String tableName = tables.getString("TABLE_NAME");
        System.out.println(tableName);
      }


      conn.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}