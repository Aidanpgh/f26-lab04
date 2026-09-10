package edu.cmu.cs214.lab04;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A small room-directory HTTP service with no runtime dependencies.
 *
 * <p>Routes:
 * <ul>
 *   <li>{@code GET /api/health} returns {@code {"status":"ok"}}</li>
 *   <li>{@code GET /api/rooms} returns a fixed JSON array of rooms</li>
 *   <li>every other path returns 404</li>
 * </ul>
 *
 * <p>The listen port comes from the {@code PORT} environment variable and
 * defaults to 8080.
 */
public final class ServiceMain {

    /** Port used when PORT is unset or empty. */
    static final int DEFAULT_PORT = 8080;

    private static final String HEALTH_JSON = "{\"status\":\"ok\"}";

    private static final String ROOMS_JSON = """
            [
              {"id":"WEH-5202","name":"Wean 5202","capacity":40},
              {"id":"GHC-4401","name":"Gates 4401","capacity":24},
              {"id":"POS-146","name":"Posner 146","capacity":120},
              {"id":"TEP-2700","name":"Tepper 2700","capacity":16}
            ]""";

    private static final String NOT_FOUND_JSON = "{\"error\":\"not found\"}";

    private static final String METHOD_NOT_ALLOWED_JSON = "{\"error\":\"method not allowed\"}";

    private ServiceMain() {
    }

    public static void main(String[] args) throws IOException {
        HttpServer server = start(readPort(System.getenv("PORT")));
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop(server)));
        System.out.println("lab04-service listening on " + server.getAddress().getPort());
    }

    /**
     * Starts the service on the given port. Pass 0 to bind an ephemeral port,
     * which is what the tests do.
     */
    static HttpServer start(int port) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", ServiceMain::handle);
        server.setExecutor(Executors.newFixedThreadPool(4));
        server.start();
        return server;
    }

    /** Stops the server and shuts down its request threads. */
    static void stop(HttpServer server) {
        server.stop(0);
        if (server.getExecutor() instanceof ExecutorService executor) {
            executor.shutdownNow();
        }
    }

    /** Reads a port from an environment value, falling back to {@link #DEFAULT_PORT}. */
    static int readPort(String value) {
        if (value == null || value.isBlank()) {
            return DEFAULT_PORT;
        }
        return Integer.parseInt(value.trim());
    }

    private static void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            if (!"GET".equals(exchange.getRequestMethod())) {
                respond(exchange, 405, METHOD_NOT_ALLOWED_JSON);
                return;
            }
            switch (exchange.getRequestURI().getPath()) {
                case "/api/health" -> respond(exchange, 200, HEALTH_JSON);
                case "/api/rooms" -> respond(exchange, 200, ROOMS_JSON);
                default -> respond(exchange, 404, NOT_FOUND_JSON);
            }
        }
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
