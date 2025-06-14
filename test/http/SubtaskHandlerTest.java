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
import tasks.Subtask;

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

public class SubtaskHandlerTest {
    private static HttpTaskServer server;
    private static TaskManager taskManager;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson())
            .registerTypeAdapter(Duration.class, new DurationAdapterForGson())
            .create();
    private static final String BASE_URL = "http://localhost:8080/subtasks";
    private static Epic testEpic;

    @BeforeAll
    static void initServer() {
        try {
            taskManager = new InMemoryTaskManager();
            server = new HttpTaskServer(taskManager);
            testEpic = taskManager.createEpic(new Epic("Test Epic", "Description"));
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
        taskManager.deleteAllSubtasks();
    }

    @Test
    void testGetAllSubtasksWhenEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertTrue(subtasks.isEmpty());
    }

    @Test
    void testCreateSubtask() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Test Subtask", "Description", Status.NEW, testEpic.getId());
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(1, taskManager.getAllSubtasks().size());
    }

    @Test
    void testGetSubtaskById() throws IOException, InterruptedException {
        Subtask subtask = taskManager.createSubtask(new Subtask("Test Subtask", "Description", Status.NEW, testEpic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + subtask.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Subtask retrievedSubtask = gson.fromJson(response.body(), Subtask.class);
        assertEquals(subtask.getId(), retrievedSubtask.getId());
    }

    @Test
    void testGetSubtasksByEpicId() throws IOException, InterruptedException {
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1", "Desc", Status.NEW, testEpic.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2", "Desc", Status.NEW, testEpic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + testEpic.getId() + "/epic"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Subtask> subtasks = gson.fromJson(response.body(), new TypeToken<List<Subtask>>() {
        }.getType());
        assertEquals(2, subtasks.size());
    }

    @Test
    void testUpdateSubtask() throws IOException, InterruptedException {
        Subtask subtask = taskManager.createSubtask(new Subtask("Original", "Desc", Status.NEW, testEpic.getId()));
        subtask.setName("Updated");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(subtask)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals("Updated", taskManager.getSubtaskById(subtask.getId()).getName());
    }

    @Test
    void testDeleteSubtask() throws IOException, InterruptedException {
        Subtask subtask = taskManager.createSubtask(new Subtask("To delete", "Desc", Status.NEW, testEpic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertNull(taskManager.getSubtaskById(subtask.getId()));
    }

    @Test
    void testDeleteAllSubtasks() throws IOException, InterruptedException {
        taskManager.createSubtask(new Subtask("Subtask 1", "Desc", Status.NEW, testEpic.getId()));
        taskManager.createSubtask(new Subtask("Subtask 2", "Desc", Status.NEW, testEpic.getId()));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertTrue(taskManager.getAllSubtasks().isEmpty());
    }

    @Test
    void testSubtaskWithTimeFields() throws IOException, InterruptedException {
        Subtask subtask = new Subtask("Timed Subtask", "Desc", Status.NEW, testEpic.getId());
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofHours(1));
        String subtaskJson = gson.toJson(subtask);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        Subtask created = taskManager.getAllSubtasks().get(0);
        assertNotNull(created.getStartTime());
        assertEquals(Duration.ofHours(1), created.getDuration());
    }
}