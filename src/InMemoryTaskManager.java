import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int taskIdCounter = 1;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final ArrayList<Task> history = new ArrayList<>();
    private static final int MAX_HISTORY_SIZE = 10;

    @Override
    public List<Task> getAllTasks() {
        System.out.println("Получен список всех задач. Количество: " + tasks.size());
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        System.out.println("Удалены все задачи. Количество удаленных: " + tasks.size());
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            addToHistory(task);
            System.out.println("Задача с ID=" + id + " успешно найдена");
        } else {
            System.out.println("Ошибка: задача с ID=" + id + " не найдена");
        }
        return task;
    }

    @Override
    public Task createTask(Task task) {
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        System.out.println("Создана новая задача с ID=" + task.getId());
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            tasks.put(task.getId(), task);
            System.out.println("Задача с ID=" + task.getId() + " успешно обновлена");
        } else {
            System.out.println("Ошибка обновления: задача с ID=" + task.getId() + " не найдена");
        }
    }

    @Override
    public void deleteTaskById(int id) {
        Task removed = tasks.remove(id);
        if (removed != null) {
            System.out.println("Задача с ID=" + id + " успешно удалена");
        } else {
            System.out.println("Ошибка удаления: задача с ID=" + id + " не найдена");
        }
    }

    @Override
    public List<Epic> getAllEpics() {
        System.out.println("Получен список всех эпиков. Количество: " + epics.size());
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        System.out.println("Удалены все эпики. Количество: " + epics.size() +
                " и все подзадачи. Количество: " + subtasks.size());
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            addToHistory(epic);
            System.out.println("Эпик с ID=" + id + " успешно найден");
        } else {
            System.out.println("Ошибка: эпик с ID=" + id + " не найден");
        }
        return epic;
    }

    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(taskIdCounter++);
        epics.put(epic.getId(), epic);
        System.out.println("Создан новый эпик с ID=" + epic.getId());
        return epic;
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic savedEpic = epics.get(epic.getId());
            savedEpic.setName(epic.getName());
            savedEpic.setDescription(epic.getDescription());
            System.out.println("Эпик с ID=" + epic.getId() + " успешно обновлен");
        } else {
            System.out.println("Ошибка обновления: эпик с ID=" + epic.getId() + " не найден");
        }
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            int subtasksCount = epic.getSubtaskIds().size();
            for (Integer subtaskId : epic.getSubtaskIds()) {
                subtasks.remove(subtaskId);
            }
            System.out.println("Эпик с ID=" + id + " и его " + subtasksCount + " подзадач успешно удалены");
        } else {
            System.out.println("Ошибка удаления: эпик с ID=" + id + " не найден");
        }
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        System.out.println("Получен список всех подзадач. Количество: " + subtasks.size());
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Subtask> getSubtasksByEpicId(int epicId) {
        List<Subtask> result = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic != null) {
            for (Integer subtaskId : epic.getSubtaskIds()) {
                Subtask subtask = subtasks.get(subtaskId);
                if (subtask != null) {
                    result.add(subtask);
                }
            }
            System.out.println("Получены подзадачи для эпика ID=" + epicId + ". Количество: " + result.size());
        } else {
            System.out.println("Ошибка: эпик с ID=" + epicId + " не найден, невозможно получить подзадачи");
        }
        return result;
    }

    @Override
    public void deleteAllSubtasks() {
        System.out.println("Удалены все подзадачи. Количество: " + subtasks.size());
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            addToHistory(subtask);
            System.out.println("Подзадача с ID=" + id + " успешно найдена");
        } else {
            System.out.println("Ошибка: подзадача с ID=" + id + " не найдена");
        }
        return subtask;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicId())) {
            subtask.setId(taskIdCounter++);
            subtasks.put(subtask.getId(), subtask);
            epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
            System.out.println("Создана новая подзадача с ID=" + subtask.getId() +
                    " для эпика ID=" + subtask.getEpicId());
            return subtask;
        } else {
            System.out.println("Ошибка создания: эпик с ID=" + subtask.getEpicId() + " не найден");
            return null;
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId()) &&
                epics.containsKey(subtask.getEpicId())) {
            subtasks.put(subtask.getId(), subtask);
            updateEpicStatus(subtask.getEpicId());
            System.out.println("Подзадача с ID=" + subtask.getId() + " успешно обновлена");
        } else {
            System.out.println("Ошибка обновления: подзадача с ID=" + subtask.getId() +
                    " или эпик с ID=" + subtask.getEpicId() + " не найдены");
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
            System.out.println("Подзадача с ID=" + id + " успешно удалена");
        } else {
            System.out.println("Ошибка удаления: подзадача с ID=" + id + " не найдена");
        }
    }

    private void addToHistory(Task task) {
        history.add(task);
        if (history.size() > MAX_HISTORY_SIZE) {
            history.remove(0);
        }
        System.out.println("Задача с ID=" + task.getId() + " добавлена в историю");
    }

    @Override
    public List<Task> getHistory() {
        System.out.println("Запрошена история просмотров. Количество элементов: " + history.size());
        return new ArrayList<>(history);
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            System.out.println("Ошибка обновления статуса: эпик с ID=" + epicId + " не найден");
            return;
        }

        List<Subtask> epicSubtasks = getSubtasksByEpicId(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            System.out.println("Статус эпика ID=" + epicId + " обновлен на NEW (нет подзадач)");
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
            System.out.println("Статус эпика ID=" + epicId + " обновлен на NEW");
        } else if (allDone) {
            epic.setStatus(Status.DONE);
            System.out.println("Статус эпика ID=" + epicId + " обновлен на DONE");
        } else {
            epic.setStatus(Status.IN_PROGRESS);
            System.out.println("Статус эпика ID=" + epicId + " обновлен на IN_PROGRESS");
        }
    }
}