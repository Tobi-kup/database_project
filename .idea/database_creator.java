import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.*;

public class Database_Creator{
  public static void main(String[] args){

  String url = "https://hntarmduljscudcbhlvk.supabase.co/";
  String user = "database_project";
  String password = "database_project123";

  try {
    Connection conn = DriverManager.getConnection(url, user, password);
    System.out.println("Verbunden!");

  } catch (SQLException e) {
    e.printStackTrace();
  }

  Statement stmt = conn.createStatement();

}
}