package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;
import taskmanager.http.HttpTaskServer;
import taskmanager.misc.DurationAdapterForGson;
import taskmanager.misc.TimeAdapterForGson;
import tasks.Epic;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EpicHandlerTest {
    private static HttpTaskServer server;
    private static TaskManager taskManager;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson())
            .registerTypeAdapter(Duration.class, new DurationAdapterForGson())
            .create();
    private static final String BASE_URL = "http://localhost:8080/epics";

    @BeforeAll
    static void initServer() {
        try {
            taskManager = new InMemoryTaskManager();
            server = new HttpTaskServer(taskManager);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        server.start();
    }

    @AfterAll
    static void destroyServer() {
        server.stop();
    }

    @BeforeEach
    void resetManager() {
        taskManager.deleteAllTasks();
        taskManager.deleteAllEpics();
    }

    @Test
    void testGetAllEpicsWhenEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Epic> epics = gson.fromJson(response.body(), new TypeToken<List<Epic>>(){}.getType());
        assertTrue(epics.isEmpty());
    }

    @Test
    void testCreateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Test Description");
        String epicJson = gson.toJson(epic);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(1, taskManager.getAllEpics().size());
    }

    @Test
    void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epic.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Epic retrievedEpic = gson.fromJson(response.body(), Epic.class);
        assertEquals(epic.getId(), retrievedEpic.getId());
    }

    @Test
    void testGetNonExistentEpic() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(new Epic("Original", "Original"));
        epic.setName("Updated");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(epic)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals("Updated", taskManager.getEpicById(epic.getId()).getName());
    }

    @Test
    void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = taskManager.createEpic(new Epic("To delete", "Delete me"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + epic.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertNull(taskManager.getEpicById(epic.getId()));
    }

    @Test
    void testDeleteAllEpics() throws IOException, InterruptedException {
        taskManager.createEpic(new Epic("Epic 1", "Desc"));
        taskManager.createEpic(new Epic("Epic 2", "Desc"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertTrue(taskManager.getAllEpics().isEmpty());
    }
}