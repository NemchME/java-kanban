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

public class TaskHandlerTest {
    private static HttpTaskServer server;
    private static TaskManager taskManager;
    private static final HttpClient httpClient = HttpClient.newHttpClient();
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson())
            .registerTypeAdapter(Duration.class, new DurationAdapterForGson())
            .create();
    private static final String BASE_URL = "http://localhost:8080/tasks";

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
    void testGetAllTasksWhenEmpty() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        List<Task> tasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        assertTrue(tasks.isEmpty());
    }

    @Test
    void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task("Test Task", "Description", Status.NEW);
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals(1, taskManager.getAllTasks().size());
    }

    @Test
    void testGetTaskById() throws IOException, InterruptedException {
        Task task = taskManager.createTask(new Task("Test Task", "Description", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + task.getId()))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_OK, response.statusCode());
        Task retrievedTask = gson.fromJson(response.body(), Task.class);
        assertEquals(task.getId(), retrievedTask.getId());
    }

    @Test
    void testGetNonExistentTask() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/999"))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }

    @Test
    void testUpdateTask() throws IOException, InterruptedException {
        Task task = taskManager.createTask(new Task("Original", "Original", Status.NEW));
        task.setName("Updated");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(task)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertEquals("Updated", taskManager.getTaskById(task.getId()).getName());
    }

    @Test
    void testDeleteTask() throws IOException, InterruptedException {
        Task task = taskManager.createTask(new Task("To delete", "Delete me", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/" + task.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertNull(taskManager.getTaskById(task.getId()));
    }

    @Test
    void testDeleteAllTasks() throws IOException, InterruptedException {
        taskManager.createTask(new Task("Task 1", "Desc", Status.NEW));
        taskManager.createTask(new Task("Task 2", "Desc", Status.NEW));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .DELETE()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        assertTrue(taskManager.getAllTasks().isEmpty());
    }

    @Test
    void testTaskWithTimeFields() throws IOException, InterruptedException {
        Task task = new Task("Timed Task", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        String taskJson = gson.toJson(task);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_CREATED, response.statusCode());
        Task created = taskManager.getAllTasks().get(0);
        assertNotNull(created.getStartTime());
        assertEquals(Duration.ofHours(1), created.getDuration());
    }

    @Test
    void testUnsupportedMethod() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(HttpURLConnection.HTTP_NOT_FOUND, response.statusCode());
    }
}