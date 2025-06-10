import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public abstract class TaskManagerTest<T extends TaskManager> {

    protected T manager;

    protected abstract T createManager();

    @BeforeEach
    public void setup() {
        manager = createManager();
    }

    @Test
    public void shouldCalculateEpicStatus_AllNew() {
        Epic epic = manager.createEpic(new Epic("Epic", "desc"));
        manager.createSubtask(new Subtask("s1", "desc", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    public void shouldCalculateEpicStatus_AllDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "desc"));
        manager.createSubtask(new Subtask("s1", "desc", Status.DONE, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.DONE, epic.getId()));
        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    public void shouldCalculateEpicStatus_MixedStatuses() {
        Epic epic = manager.createEpic(new Epic("Epic", "desc"));
        manager.createSubtask(new Subtask("s1", "desc", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.DONE, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    public void shouldCalculateEpicStatus_AllInProgress() {
        Epic epic = manager.createEpic(new Epic("Epic", "desc"));
        manager.createSubtask(new Subtask("s1", "desc", Status.IN_PROGRESS, epic.getId()));
        manager.createSubtask(new Subtask("s2", "desc", Status.IN_PROGRESS, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    public void shouldNotAllowOverlappingTasks() {
        Task task1 = new Task("Task1", "desc", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "desc", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 6, 10, 10, 30));
        task2.setDuration(Duration.ofMinutes(60));

        assertNull(manager.createTask(task2), "Task2 should not be allowed due to time overlap with Task1");
    }

    @Test
    public void shouldTrackSubtaskToEpicLink() {
        Epic epic = manager.createEpic(new Epic("Epic", "desc"));
        Subtask subtask = new Subtask("Sub", "desc", Status.NEW, epic.getId());
        manager.createSubtask(subtask);
        List<Subtask> subtasks = manager.getSubtasksByEpicId(epic.getId());
        assertTrue(subtasks.contains(subtask));
    }

    @Test
    public void shouldReturnEmptyHistoryInitially() {
        assertTrue(manager.getHistory().isEmpty(), "История должна быть пустой при запуске");
    }

    @Test
    public void shouldStoreHistoryCorrectly() {
        Task task = manager.createTask(new Task("Task", "desc", Status.NEW));
        manager.getTaskById(task.getId());
        List<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }

    @Test
    public void shouldNotDuplicateHistoryEntries() {
        Task task = manager.createTask(new Task("Task", "desc", Status.NEW));
        manager.getTaskById(task.getId());
        manager.getTaskById(task.getId());
        assertEquals(1, manager.getHistory().size(), "История не должна содержать дубликаты");
    }

    @Test
    public void shouldRemoveTaskFromHistory() {
        Task t1 = manager.createTask(new Task("t1", "desc", Status.NEW));
        Task t2 = manager.createTask(new Task("t2", "desc", Status.NEW));
        Task t3 = manager.createTask(new Task("t3", "desc", Status.NEW));

        manager.getTaskById(t1.getId());
        manager.getTaskById(t2.getId());
        manager.getTaskById(t3.getId());

        manager.deleteTaskById(t2.getId());
        List<Task> history = manager.getHistory();

        assertEquals(2, history.size());
        assertFalse(history.contains(t2));
    }
}