import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int taskIdCounter = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task == null) {
            System.out.println("Задача с id=" + id + " не найдена.");
        }
        return task;
    }

    public Task createTask(Task task) {
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        return task;
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
        } else {
            System.out.println("Не удалось обновить задачу с id=" + task.getId() + ". Задача не найдена.");
        }
    }

    public void deleteTaskById(int id) {
        if (tasks.remove(id) == null) {
            System.out.println("Не удалось удалить задачу с id=" + id + ". Задача не найдена.");
        }
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            System.out.println("Эпик с id=" + id + " не найден.");
        }
        return epic;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(taskIdCounter++);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public void updateEpic(Epic epic) {
        Epic savedEpic = epics.get(epic.getId());
        if (savedEpic != null) {
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
        } else {
            System.out.println("Не удалось обновить эпик с id=" + epic.getId() + ". Эпик не найден.");
        }
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
        } else {
            System.out.println("Не удалось удалить эпик с id=" + id + ". Эпик не найден.");
        }
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        ArrayList<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
        } else {
            System.out.println("Эпик с id=" + epicId + " не найден. Невозможно получить подзадачи.");
        }
        return result;
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Эпик с id=" + epicId + " не найден. Невозможно обновить статус.");
            return;
        }

        ArrayList<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : epicSubtasks) {
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

    public void deleteAllSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask == null) {
            System.out.println("Подзадача с id=" + id + " не найдена.");
        }
        return subtask;
    }

    public Subtask createSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            subtask.setId(taskIdCounter++);
            subtasks.put(subtask.getId(), subtask);
            epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
            return subtask;
        } else {
            System.out.println("Не удалось создать подзадачу. Эпик с id=" + subtask.getEpicId() + " не найден.");
            return null;
        }
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId()) &&
                epics.containsKey(subtask.getEpicId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
        } else {
            System.out.println("Не удалось обновить подзадачу с id=" + subtask.getId() +
                    ". Подзадача или эпик не найдены.");
        }
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        } else {
            System.out.println("Не удалось удалить подзадачу с id=" + id + ". Подзадача не найдена.");
        }
    }
}