package taskmanager.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.TaskManager;
import taskmanager.misc.DurationAdapterForGson;
import taskmanager.misc.TimeAdapterForGson;
import tasks.Subtask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler  {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson())
                .registerTypeAdapter(Duration.class, new DurationAdapterForGson())
                .create();
    }

    private void throwableHandler(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        switch (method) {
            case "GET":
                if (pathParts.length == 2) {
                    String response = gson.toJson(taskManager.getAllSubtasks());
                    sendSuccess(exchange, response);
                } else if (pathParts.length == 4 && pathParts[3].equals("epic")) {
                    int epicId = Integer.parseInt(pathParts[2]);
                    String response = gson.toJson(taskManager.getSubtasksByEpicId(epicId));
                    sendSuccess(exchange, response);
                } else {
                    int id = Integer.parseInt(pathParts[2]);
                    Subtask subtask = taskManager.getSubtaskById(id);
                    if (subtask != null) {
                        sendSuccess(exchange, gson.toJson(subtask));
                    } else {
                        sendNotFound(exchange);
                    }
                }
                break;
            case "POST":
                String body = readRequestBody(exchange);
                Subtask newSubtask = gson.fromJson(body, Subtask.class);
                if (newSubtask.getId() == 0) {
                    Subtask createdSubtask = taskManager.createSubtask(newSubtask);
                    if (createdSubtask != null) {
                        sendCreated(exchange);
                    } else {
                        sendHasInteractions(exchange);
                    }
                } else {
                    taskManager.updateSubtask(newSubtask);
                    sendCreated(exchange);
                }
                break;
            case "DELETE":
                if (pathParts.length == 2) {
                    taskManager.deleteAllSubtasks();
                    sendCreated(exchange);
                } else {
                    int id = Integer.parseInt(pathParts[2]);
                    taskManager.deleteSubtaskById(id);
                    sendCreated(exchange);
                }
                break;
            default:
                sendNotFound(exchange);
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
