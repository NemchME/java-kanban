package tests;

import taskManager.Managers;
import taskManager.TaskManager;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    private TaskManager manager = Managers.getDefault();

    @Test
    void testAddAndFindTasks() {
        Task task = manager.createTask(new Task("A", "B", Status.NEW));
        Epic epic = manager.createEpic(new Epic("C", "D"));
        Subtask sub = manager.createSubtask(new Subtask("E", "F", Status.NEW, epic.getId()));

        assertEquals(task, manager.getTaskById(task.getId()));
        assertEquals(epic, manager.getEpicById(epic.getId()));
        assertEquals(sub, manager.getSubtaskById(sub.getId()));
    }

    @Test
    void testManualAndGeneratedIds() {
        Task task1 = manager.createTask(new Task("A", "B", Status.NEW));
        Task task2 = new Task("C", "D", Status.NEW);
        task2.setId(100);
        manager.createTask(task2);
        assertNotEquals(task1.getId(), task2.getId());
    }

    @Test
    void testTaskImmutability() {
        Task original = new Task("A", "B", Status.NEW);
        original.setId(1);
        manager.createTask(original);
        Task saved = manager.getTaskById(1);
        assertEquals("A", saved.getName());
        assertEquals("B", saved.getDescription());
        assertEquals(Status.NEW, saved.getStatus());
    }
}