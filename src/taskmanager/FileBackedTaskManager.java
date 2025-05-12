package taskmanager;

import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
                } else {
                    tasks.put(task.getId(), task);
                }
                updateTaskIdCounter(task.getId());
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Error loading from file", e);
        }
    }

    private void updateTaskIdCounter(int id) {
        if (taskIdCounter <= id) {
            taskIdCounter = id + 1;
        }
    }

    protected void save() {
        try {
            StringBuilder builder = new StringBuilder("id,type,name,status,description,epic\n");
            appendTasksToBuilder(builder);
            Files.writeString(file.toPath(), builder.toString());
        } catch (IOException e) {
            throw new ManagerSaveException("Error saving to file", e);
        }
    }

    private void appendTasksToBuilder(StringBuilder builder) {
        for (Task task : getAllTasks()) builder.append(toString(task)).append("\n");
        for (Epic epic : getAllEpics()) builder.append(toString(epic)).append("\n");
        for (Subtask subtask : getAllSubtasks()) builder.append(toString(subtask)).append("\n");
    }

    private static String toString(Task task) {
        return switch (task) {
            case Subtask subtask -> String.format("%d,%s,%s,%s,%s,%d",
                    subtask.getId(), TaskType.SUBTASK, subtask.getName(),
                    subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());
            case Epic epic -> String.format("%d,%s,%s,%s,%s,",
                    epic.getId(), TaskType.EPIC, epic.getName(),
                    epic.getStatus(), epic.getDescription());
            default -> String.format("%d,%s,%s,%s,%s,",
                    task.getId(), TaskType.TASK, task.getName(),
                    task.getStatus(), task.getDescription());
        };
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        TaskType type = TaskType.valueOf(parts[1]);
        String name = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        return switch (type) {
            case TASK -> createTask(id, name, description, status);
            case EPIC -> createEpic(id, name, description, status);
            case SUBTASK -> createSubtask(id, name, description, status, Integer.parseInt(parts[5]));
        };
    }

    private static Task createTask(int id, String name, String description, Status status) {
        Task task = new Task(name, description, status);
        task.setId(id);
        return task;
    }

    private static Epic createEpic(int id, String name, String description, Status status) {
        Epic epic = new Epic(name, description);
        epic.setId(id);
        epic.setStatus(status);
        return epic;
    }

    private static Subtask createSubtask(int id, String name, String description, Status status, int epicId) {
        Subtask subtask = new Subtask(name, description, status, epicId);
        subtask.setId(id);
        return subtask;
    }

    @Override
    public Task createTask(Task task) {
        Task t = super.createTask(task);
        save();
        return t;
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
        Epic e = super.createEpic(epic);
        save();
        return e;
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
        Subtask s = super.createSubtask(subtask);
        save();
        return s;
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