package taskmanager;

import tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        manager.load();
        return manager;
    }

    private void load() {
        try {
            String content = Files.readString(file.toPath());
            if (content.isEmpty()) return;

            String[] lines = content.split("\n");
            for (int i = 1; i < lines.length; i++) {
                Task task = fromString(lines[i]);
                if (task == null) continue;

                if (task instanceof Epic epic) {
                    epics.put(epic.getId(), epic);
                } else if (task instanceof Subtask subtask) {
                    subtasks.put(subtask.getId(), subtask);
                    Epic epic = epics.get(subtask.getEpicId());
                    if (epic != null) epic.addSubtaskId(subtask.getId());
                    addPrioritized(subtask);
                } else {
                    tasks.put(task.getId(), task);
                    addPrioritized(task);
                }
                updateTaskIdCounter(task.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка загрузки из файла", e);
        }
    }

    private void updateTaskIdCounter(int id) {
        if (taskIdCounter <= id) {
            taskIdCounter = id + 1;
        }
    }

    protected void save() {
        try {
            StringBuilder builder = new StringBuilder("id,type,name,status,description,epic,startTime,duration\n");
            appendTasksToBuilder(builder);
            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения в файл", e);
        }
    }

    private void appendTasksToBuilder(StringBuilder builder) {
        getAllTasks().forEach(t -> builder.append(toString(t)).append("\n"));
        getAllEpics().forEach(e -> builder.append(toString(e)).append("\n"));
        getAllSubtasks().forEach(s -> builder.append(toString(s)).append("\n"));
    }

    private static String toString(Task task) {
        String startTime = task.getStartTimeString();
        String duration = task.getDurationMinutesString();

        return switch (task) {
            case Subtask subtask -> String.format("%d,%s,%s,%s,%s,%d,%s,%s",
                    subtask.getId(), TaskType.SUBTASK, subtask.getName(),
                    subtask.getStatus(), subtask.getDescription(),
                    subtask.getEpicId(), startTime, duration);
            case Epic epic -> String.format("%d,%s,%s,%s,%s,,%s,%s",
                    epic.getId(), TaskType.EPIC, epic.getName(),
                    epic.getStatus(), epic.getDescription(),
                    epic.getStartTimeString(), epic.getDurationMinutesString());
            default -> String.format("%d,%s,%s,%s,%s,,%s,%s",
                    task.getId(), TaskType.TASK, task.getName(),
                    task.getStatus(), task.getDescription(),
                    startTime, duration);
        };
    }

    private static Task fromString(String line) {
        String[] parts = line.split(",", -1);
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String desc = parts[4];
        String startStr = parts[6];
        String durationStr = parts[7];
        LocalDateTime startTime = startStr.isEmpty() ? null : LocalDateTime.parse(startStr);
        Duration duration = durationStr.isEmpty() ? null : Duration.ofMinutes(Long.parseLong(durationStr));
        return switch (type) {
            case TASK -> {
                Task task = new Task(name, desc, status);
                task.setId(id);
                task.setStartTime(startTime);
                task.setDuration(duration);
                yield task;
            }
            case EPIC -> {
                Epic epic = new Epic(name, desc);
                epic.setId(id);
                epic.setStatus(status);
                yield epic;
            }
            case SUBTASK -> {
                int epicId = Integer.parseInt(parts[5]);
                Subtask sub = new Subtask(name, desc, status, epicId);
                sub.setId(id);
                sub.setStartTime(startTime);
                sub.setDuration(duration);
                yield sub;
            }
        };
    }

    // Все override-методы create/update/delete сохраняют автоматически
    @Override
    public Task createTask(Task task) {
        Task result = super.createTask(task);
        save();
        return result;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void deleteTaskById(int id) {
        super.deleteTaskById(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public Epic createEpic(Epic epic) {
        Epic result = super.createEpic(epic);
        save();
        return result;
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void deleteEpicById(int id) {
        super.deleteEpicById(id);
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        Subtask result = super.createSubtask(subtask);
        save();
        return result;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteSubtaskById(int id) {
        super.deleteSubtaskById(id);
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }
}