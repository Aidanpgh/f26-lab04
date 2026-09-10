package edu.cmu.cs214.lab04;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServiceMainTest {

    private HttpServer server;
    private HttpClient client;

    @BeforeEach
    void startOnEphemeralPort() throws IOException {
        server = ServiceMain.start(0);
        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void stopServer() {
        ServiceMain.stop(server);
    }

    @Test
    void healthReportsOk() throws Exception {
        HttpResponse<String> response = get("/api/health");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"ok\""), response.body());
    }

    @Test
    void roomsListsRoomsWithCapacities() throws Exception {
        HttpResponse<String> response = get("/api/rooms");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"id\":\"WEH-5202\""), response.body());
        assertTrue(response.body().contains("\"capacity\":120"), response.body());
    }

    @Test
    void unknownPathIsNotFound() throws Exception {
        assertEquals(404, get("/api/nope").statusCode());
    }

    @Test
    void portDefaultsWhenEnvironmentValueIsMissingOrEmpty() {
        assertEquals(ServiceMain.DEFAULT_PORT, ServiceMain.readPort(null));
        assertEquals(ServiceMain.DEFAULT_PORT, ServiceMain.readPort(""));
        assertEquals(9090, ServiceMain.readPort("9090"));
    }

    private HttpResponse<String> get(String path) throws Exception {
        URI uri = URI.create("http://localhost:" + server.getAddress().getPort() + path);
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
