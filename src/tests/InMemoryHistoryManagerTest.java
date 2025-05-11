package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskManager.InMemoryHistoryManager;
import tasks.Status;
import tasks.Task;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    private InMemoryHistoryManager manager;
    private Task task1, task2, task3;

    @BeforeEach
    void setUp() {
        manager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1", Status.NEW);
        task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        task3 = new Task("Task 3", "Description 3", Status.DONE);
        task1.setId(1);
        task2.setId(2);
        task3.setId(3);
    }

    @Test
    void testAddAndRetrieveTasks() {
        manager.add(task1);
        manager.add(task2);
        ArrayList<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testRemoveTaskFromHistory() {
        manager.add(task1);
        manager.add(task2);
        manager.remove(task1.getId());
        ArrayList<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertFalse(history.contains(task1));
    }

    @Test
    void testPreventDuplicateInHistory() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task1);  // должен переместиться в конец
        ArrayList<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void testAddNullDoesNothing() {
        manager.add(null);
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void testGetHistoryReturnsNewList() {
        manager.add(task1);
        ArrayList<Task> list1 = manager.getHistory();
        ArrayList<Task> list2 = manager.getHistory();
        assertNotSame(list1, list2);
    }

    @Test
    void testUpdatedTaskInHistoryReflectsChanges() {
        manager.add(task1);
        task1.setStatus(Status.DONE);
        assertEquals(Status.DONE, manager.getHistory().get(0).getStatus());
    }
}