import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.awt.Desktop;
import java.net.URI;

public class Main {
    public static void main(String[] args) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080),0);

        server.createContext("/", new SPAHandler());
        server.createContext("/register", new RegisterHandler());
        server.createContext("/login", new LoginHandler());
        server.createContext("/addBalance", new AddBalanceHandler());
        server.createContext("/stocks/list", new StocksListHandler()); // falls noch vorhanden

        server.setExecutor(null);
        server.start();

        System.out.println("Server läuft auf http://localhost:8080");
        Desktop.getDesktop().browse(new URI("http://localhost:8080"));
    }
}