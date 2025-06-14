package taskmanager.http;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.TaskManager;
import taskmanager.misc.DurationAdapterForGson;
import taskmanager.misc.TimeAdapterForGson;
import tasks.Epic;
import tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager manager) {
        this.taskManager = manager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new TimeAdapterForGson())
                .registerTypeAdapter(Duration.class, new DurationAdapterForGson())
                .create();
    }

    private void throwableHandler(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        String[] pathParts = path.split("/");

        switch (exchange.getRequestMethod()) {
            case "GET" -> {
                if(pathParts.length == 2) {
                    String response = this.gson.toJson(taskManager.getAllEpics());
                    sendSuccess(exchange, response);
                } else {
                    int id = Integer.parseInt(pathParts[2]);
                    Task task = taskManager.getEpicById(id);
                    if (task != null) {
                        sendSuccess(exchange, gson.toJson(task));
                    } else {
                        sendNotFound(exchange);
                    }
                }
            }

            case "POST" -> {
                String body = readRequestBody(exchange);
                Epic newEpic = gson.fromJson(body, Epic.class);
                if (newEpic.getId() == 0) {
                    Epic createdTask = taskManager.createEpic(newEpic);
                    if (createdTask != null) {
                        sendCreated(exchange);
                    } else {
                        sendHasInteractions(exchange);
                    }
                } else {
                    taskManager.updateEpic(newEpic);
                    sendCreated(exchange);
                }
            }

            case "DELETE" -> {
                if (pathParts.length == 2) {
                    taskManager.deleteAllEpics();
                    sendCreated(exchange);
                } else {
                    int id = Integer.parseInt(pathParts[2]);
                    taskManager.deleteEpicById(id);
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
        } catch (Exception ex) {
            sendInternalError(exchange);
        }
    }
}
