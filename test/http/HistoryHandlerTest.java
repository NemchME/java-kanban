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
import tasks.Status;
import tasks.Task;

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

public class HistoryHandlerTest {
    private static HttpTaskServer server;
    private static TaskManager taskManager;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson())
            .registerTypeAdapter(Duration.class, new DurationAdapterForGson())
            .create();
    private static final String HISTORY_URL = "http://localhost:8080/history";

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
    void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertTrue(history.isEmpty());
    }

    @Test
    void testGetHistoryWithTasks() throws IOException, InterruptedException {
        Task task = taskManager.createTask(new Task("Test Task", "Description", Status.NEW));
        Epic epic = taskManager.createEpic(new Epic("Test Epic", "Description"));

        taskManager.getTaskById(task.getId());
        taskManager.getEpicById(epic.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(2, history.size());
    }

    @Test
    void testUnsupportedMethods() throws IOException, InterruptedException {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> postResponse = httpClient.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, postResponse.statusCode());

        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = httpClient.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, deleteResponse.statusCode());
    }

    @Test
    void testHistoryWithTimeFields() throws IOException, InterruptedException {
        Task task = new Task("Task with time", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        taskManager.createTask(task);

        taskManager.getTaskById(task.getId());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HISTORY_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Task> history = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertEquals(1, history.size());
        assertNotNull(history.get(0).getStartTime());
        assertEquals(Duration.ofHours(1), history.get(0).getDuration());
    }
}