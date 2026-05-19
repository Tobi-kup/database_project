import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.util.HashMap;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class SPAHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        String uri = exchange.getRequestURI().getPath();

        if (uri.equals("/")) {
            uri = "/index.html";
        }

        Path path = Path.of("public", uri);

        if (!Files.exists(path)) {
            exchange.sendResponseHeaders(404, -1);
            return;
        }

        String contentType = getContentType(uri);

        exchange.getResponseHeaders().set("Content-Type", contentType);

        byte[] bytes = Files.readAllBytes(path);

        exchange.sendResponseHeaders(200, bytes.length);

        OutputStream os = exchange.getResponseBody();

        os.write(bytes);

        os.close();
    }

    private String getContentType(String uri) {

        if (uri.endsWith(".html")) {
            return "text/html";
        }

        if (uri.endsWith(".css")) {
            return "text/css";
        }

        if (uri.endsWith(".js")) {
            return "application/javascript";
        }

        return "text/plain";
    }


    // ---------------- JSON PARSER ----------------
    public static HashMap<String,String> parseJson(String json){

        HashMap<String,String> map = new HashMap<>();

        json = json.replace("{","")
                .replace("}","")
                .replace("\"","");

        String[] pairs = json.split(",");

        for(String pair : pairs){
            String[] kv = pair.split(":");
            if(kv.length == 2){
                map.put(
                        kv[0].trim(),
                        kv[1].trim()
                );
            }
        }

        return map;
    }


    // ---------------- RESPONSE HELPER ----------------
    public static void send(HttpExchange exchange, String response) throws IOException {

        exchange.getResponseHeaders().set("Content-Type", "application/json");

        exchange.sendResponseHeaders(200, response.getBytes().length);

        OutputStream os = exchange.getResponseBody();

        os.write(response.getBytes());

        os.close();
    }
}
