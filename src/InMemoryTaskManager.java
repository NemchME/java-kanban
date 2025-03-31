import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int taskIdCounter = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            System.out.println("Ошибка: задача (ID=" + id + ") не найдена");
        } else {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) {
            System.out.println("Ошибка: задача (ID=" + task.getId() + ") не найдена");
            return;
        }
        tasks.put(task.getId(), task);
    }

    @Override
    public void deleteTaskById(int id) {
        if (tasks.remove(id) == null) {
            System.out.println("Ошибка: задача (ID=" + id + ") не найдена");
        }
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            System.out.println("Ошибка: эпик (ID=" + id + ") не найден");
        } else {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(taskIdCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (!epics.containsKey(epic.getId())) {
            System.out.println("Ошибка: эпик (ID=" + epic.getId() + ") не найден");
            return;
        }
        Epic saved = epics.get(epic.getId());
        saved.setName(epic.getName());
        saved.setDescription(epic.getDescription());
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic == null) {
            System.out.println("Ошибка: эпик (ID=" + id + ") не найден");
            return;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        ArrayList<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Ошибка: эпик (ID=" + epicId + ") не найден");
            return result;
        }
        for (Integer subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                result.add(subtask);
            }
        }
        return result;
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            System.out.println("Ошибка: подзадача (ID=" + id + ") не найдена");
        } else {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            System.out.println("Ошибка: эпик (ID=" + subtask.getEpicId() + ") не найден");
            return null;
        }
        subtask.setId(taskIdCounter++);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) {
            System.out.println("Ошибка: подзадача (ID=" + subtask.getId() + ") не найдена");
            return;
        }
        if (!epics.containsKey(subtask.getEpicId())) {
            System.out.println("Ошибка: эпик (ID=" + subtask.getEpicId() + ") не найден");
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            System.out.println("Ошибка: подзадача (ID=" + id + ") не найдена");
            return;
        }
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.removeSubtaskId(id);
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        ArrayList<Subtask> subtasks = getSubtasksByEpicId(epicId);
        if (subtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}