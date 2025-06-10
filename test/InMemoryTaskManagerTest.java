import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.Managers;
import taskmanager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager;

    @BeforeEach
    void setup() {
        manager = Managers.getDefault();
    }

    @Test
    void testTaskCreationAndRetrieval() {
        Task task = manager.createTask(new Task("Task", "Desc", Status.NEW));
        Task fetched = manager.getTaskById(task.getId());
        assertEquals(task, fetched);
    }

    @Test
    void testEpicAndSubtaskLink() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub = manager.createSubtask(new Subtask("Sub", "Desc", Status.NEW, epic.getId()));
        assertTrue(epic.getSubtaskIds().contains(sub.getId()));
    }

    @Test
    void testDeletingSubtaskCleansEpic() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub = manager.createSubtask(new Subtask("Sub", "Desc", Status.NEW, epic.getId()));
        manager.deleteSubtaskById(sub.getId());
        assertFalse(epic.getSubtaskIds().contains(sub.getId()));
    }

    @Test
    void testTaskSettersAffectManagerStorage() {
        Task task = manager.createTask(new Task("Old", "Old", Status.NEW));
        task.setName("New Name");
        task.setStatus(Status.DONE);
        assertEquals("New Name", manager.getTaskById(task.getId()).getName());
        assertEquals(Status.DONE, manager.getTaskById(task.getId()).getStatus());
    }

    @Test
    void testEpicDeletionRemovesSubtasksAndHistory() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        Subtask sub = manager.createSubtask(new Subtask("Sub", "Desc", Status.NEW, epic.getId()));

        manager.getEpicById(epic.getId());
        manager.getSubtaskById(sub.getId());

        manager.deleteEpicById(epic.getId());

        assertNull(manager.getEpicById(epic.getId()));
        assertNull(manager.getSubtaskById(sub.getId()));
        assertFalse(manager.getHistory().contains(epic));
        assertFalse(manager.getHistory().contains(sub));
    }

    @Test
    void testGetUnknownIdDoesNotAffectHistory() {
        List<Task> initialHistory = manager.getHistory();
        assertNull(manager.getTaskById(999));
        assertEquals(initialHistory, manager.getHistory());
    }

    @Test
    void testTaskTimeOverlap() {
        Task task1 = new Task("Task1", "Desc", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 0));
        task1.setDuration(Duration.ofMinutes(60));
        manager.createTask(task1);

        Task task2 = new Task("Task2", "Desc", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 6, 10, 12, 30));
        task2.setDuration(Duration.ofMinutes(60));

        assertNull(manager.createTask(task2), "Should reject overlapping task");
    }

    @Test
    void testEpicStatusCalculation_AllNew() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub1", "Desc", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Sub2", "Desc", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testEpicStatusCalculation_AllDone() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub1", "Desc", Status.DONE, epic.getId()));
        manager.createSubtask(new Subtask("Sub2", "Desc", Status.DONE, epic.getId()));
        assertEquals(Status.DONE, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testEpicStatusCalculation_Mixed() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub1", "Desc", Status.NEW, epic.getId()));
        manager.createSubtask(new Subtask("Sub2", "Desc", Status.DONE, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }

    @Test
    void testEpicStatusCalculation_AllInProgress() {
        Epic epic = manager.createEpic(new Epic("Epic", "Desc"));
        manager.createSubtask(new Subtask("Sub1", "Desc", Status.IN_PROGRESS, epic.getId()));
        manager.createSubtask(new Subtask("Sub2", "Desc", Status.IN_PROGRESS, epic.getId()));
        assertEquals(Status.IN_PROGRESS, manager.getEpicById(epic.getId()).getStatus());
    }
}