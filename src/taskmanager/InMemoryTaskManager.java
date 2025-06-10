package taskmanager;

import tasks.*;

import java.util.*;
import java.util.stream.Collectors;
import java.time.Duration;

public class InMemoryTaskManager implements TaskManager {
    protected int taskIdCounter = 1;
    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    protected final HistoryManager historyManager = Managers.getDefaultHistory();
    protected final Set<Task> prioritizedTasks = new TreeSet<>();

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    private boolean hasIntersection(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) return false;
        return prioritizedTasks.stream()
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .anyMatch(existing -> existing.getId() != newTask.getId() &&
                        !(newTask.getEndTime().isBefore(existing.getStartTime()) ||
                                newTask.getStartTime().isAfter(existing.getEndTime())));
    }

    void addPrioritized(Task task) {
        if (task.getStartTime() != null && !hasIntersection(task)) {
            prioritizedTasks.add(task);
        }
    }

    private void removePrioritized(Task task) {
        prioritizedTasks.remove(task);
    }

    @Override
    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.values().forEach(this::removePrioritized);
        tasks.keySet().forEach(historyManager::remove);
        tasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) historyManager.add(task);
        return task;
    }

    @Override
    public Task createTask(Task task) {
        if (hasIntersection(task)) {
            System.out.println("Ошибка: задача пересекается по времени с другой задачей");
            return null;
        }
        task.setId(taskIdCounter++);
        tasks.put(task.getId(), task);
        addPrioritized(task);
        return task;
    }

    @Override
    public void updateTask(Task task) {
        if (!tasks.containsKey(task.getId())) return;
        removePrioritized(tasks.get(task.getId()));
        if (hasIntersection(task)) {
            System.out.println("Ошибка: обновление привело к пересечению задач");
            return;
        }
        tasks.put(task.getId(), task);
        addPrioritized(task);
    }

    @Override
    public void deleteTaskById(int id) {
        Task task = tasks.remove(id);
        if (task != null) {
            removePrioritized(task);
            historyManager.remove(id);
        }
    }

    @Override
    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllEpics() {
        epics.keySet().forEach(historyManager::remove);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.values().forEach(this::removePrioritized);
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) historyManager.add(epic);
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
        if (!epics.containsKey(epic.getId())) return;
        Epic oldEpic = epics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());
        updateEpicStatus(epic.getId());
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            historyManager.remove(id);
            for (Integer subId : epic.getSubtaskIds()) {
                historyManager.remove(subId);
                removePrioritized(subtasks.remove(subId));
            }
        }
    }

    @Override
    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpicId(int epicId) {
        Epic epic = epics.get(epicId);
        return epic == null ? new ArrayList<>() :
                epic.getSubtaskIds().stream()
                        .map(subtasks::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(ArrayList::new));
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.values().forEach(this::removePrioritized);
        subtasks.keySet().forEach(historyManager::remove);
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubtaskIds().clear();
            updateEpicStatus(epic.getId());
        }
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask sub = subtasks.get(id);
        if (sub != null) historyManager.add(sub);
        return sub;
    }

    @Override
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) return null;
        if (hasIntersection(subtask)) {
            System.out.println("Ошибка: пересечение по времени с другой задачей");
            return null;
        }
        subtask.setId(taskIdCounter++);
        subtasks.put(subtask.getId(), subtask);
        epics.get(subtask.getEpicId()).addSubtaskId(subtask.getId());
        addPrioritized(subtask);
        updateEpicStatus(subtask.getEpicId());
        return subtask;
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        if (!subtasks.containsKey(subtask.getId())) return;
        if (!epics.containsKey(subtask.getEpicId())) return;
        removePrioritized(subtasks.get(subtask.getId()));
        if (hasIntersection(subtask)) {
            System.out.println("Ошибка: пересечение при обновлении");
            return;
        }
        subtasks.put(subtask.getId(), subtask);
        addPrioritized(subtask);
        updateEpicStatus(subtask.getEpicId());
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            removePrioritized(subtask);
            historyManager.remove(id);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtaskId(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) return;

        List<Subtask> subs = getSubtasksByEpicId(epicId);
        if (subs.isEmpty()) {
            epic.setStatus(Status.NEW);
            epic.setStartTime(null);
            epic.setDuration(Duration.ZERO);
            epic.setEndTime(null);
            return;
        }

        boolean allNew = subs.stream().allMatch(s -> s.getStatus() == Status.NEW);
        boolean allDone = subs.stream().allMatch(s -> s.getStatus() == Status.DONE);
        epic.setStatus(allDone ? Status.DONE : allNew ? Status.NEW : Status.IN_PROGRESS);

        epic.setDuration(epic.calculateDuration(this));
        epic.setStartTime(epic.calculateStartTime(this));
        epic.setEndTime(epic.calculateEndTime(this));
    }
}