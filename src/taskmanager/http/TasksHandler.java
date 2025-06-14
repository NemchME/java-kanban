package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.TaskManager;
import taskmanager.misc.DurationAdapterForGson;
import taskmanager.misc.TimeAdapterForGson;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson()).registerTypeAdapter(Duration.class, new DurationAdapterForGson()).create();
    }

    private void throwableHandler(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        switch (method) {
            case "GET" -> {
                if (pathParts.length == 2) {
                    String response = gson.toJson(taskManager.getAllTasks());
                    sendSuccess(exchange, response);
                } else {
                    int id = Integer.parseInt(pathParts[2]);
                    Task task = taskManager.getTaskById(id);
                    if (task != null) {
                        sendSuccess(exchange, gson.toJson(task));
                    } else {
                        sendNotFound(exchange);
                    }
                }
            }
            case "POST" -> {
                String body = readRequestBody(exchange);
                Task newTask = gson.fromJson(body, Task.class);
                if (newTask.getId() == 0) {
                    Task createdTask = taskManager.createTask(newTask);
                    if (createdTask != null) {
                        sendCreated(exchange);
                    } else {
                        sendHasInteractions(exchange);
                    }
                } else {
                    taskManager.updateTask(newTask);
                    sendCreated(exchange);
                }
            }
            case "DELETE" -> {
                if (pathParts.length == 2) {
                    taskManager.deleteAllTasks();
                    sendCreated(exchange);
                } else {
                    int id = Integer.parseInt(pathParts[2]);
                    taskManager.deleteTaskById(id);
                    sendCreated(exchange);
                }
            }
            default -> sendNotFound(exchange);
        }
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            this.throwableHandler(exchange);
        } catch (Exception e) {
            sendInternalError(exchange);
        }
    }
}
