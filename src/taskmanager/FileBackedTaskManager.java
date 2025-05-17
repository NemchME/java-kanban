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

    public static void main(String[] args) {
        File file = new File("task_manager_data.csv");

        try {
            FileBackedTaskManager manager1 = new FileBackedTaskManager(file);

            Task task1 = new Task("Task1", "Task1 desc", Status.NEW);
            Task task2 = new Task("Task2", "Task2 desc", Status.NEW);
            manager1.createTask(task1);
            manager1.createTask(task2);

            Epic epic1 = new Epic("Epic1", "Epic1 desc");
            Epic epic2 = new Epic("Epic2", "Epic2 desc");
            manager1.createEpic(epic1);
            manager1.createEpic(epic2);

            Subtask subtask1 = new Subtask("Subtask1", "Subtask1 desc", Status.NEW, epic1.getId());
            Subtask subtask2 = new Subtask("Subtask2", "Subtask2 desc", Status.NEW, epic1.getId());
            Subtask subtask3 = new Subtask("Subtask3", "Subtask3 desc", Status.NEW, epic2.getId());
            Subtask subtask4 = new Subtask("Subtask4", "Subtask4 desc", Status.NEW, epic2.getId());
            manager1.createSubtask(subtask1);
            manager1.createSubtask(subtask2);
            manager1.createSubtask(subtask3);
            manager1.createSubtask(subtask4);

            FileBackedTaskManager manager2 = FileBackedTaskManager.loadFromFile(file);

            System.out.println("Checking tasks:");
            System.out.println("Manager1 tasks: " + manager1.getAllTasks());
            System.out.println("Manager2 tasks: " + manager2.getAllTasks());
            System.out.println("Match: " + manager1.getAllTasks().equals(manager2.getAllTasks()));

            System.out.println("\nChecking epics:");
            System.out.println("Manager1 epics: " + manager1.getAllEpics());
            System.out.println("Manager2 epics: " + manager2.getAllEpics());
            System.out.println("Match: " + manager1.getAllEpics().equals(manager2.getAllEpics()));

            System.out.println("\nChecking subtasks:");
            System.out.println("Manager1 subtasks: " + manager1.getAllSubtasks());
            System.out.println("Manager2 subtasks: " + manager2.getAllSubtasks());
            System.out.println("Match: " + manager1.getAllSubtasks().equals(manager2.getAllSubtasks()));

            System.out.println("\nChecking epic-subtask relationships:");
            for (Epic epic : manager1.getAllEpics()) {
                System.out.println("Epic " + epic.getId() + " subtasks in manager1: " +
                        manager1.getSubtasksByEpicId(epic.getId()));
                System.out.println("Epic " + epic.getId() + " subtasks in manager2: " +
                        manager2.getSubtasksByEpicId(epic.getId()));
            }
        } finally {
            file.delete();
        }
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