import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testAddSingleTask() {
        manager.add(task1);
        ArrayList<Task> history = manager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void testAddMultipleTasks() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        ArrayList<Task> history = manager.getHistory();
        assertEquals(3, history.size());
        assertArrayEquals(new Task[]{task1, task2, task3}, history.toArray());
    }

    @Test
    void testHistoryOrderPreservation() {
        manager.add(task1);
        manager.add(task2);
        manager.add(task3);

        ArrayList<Task> history = manager.getHistory();
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    @Test
    void testAddNullTask() {
        manager.add(null);
        assertEquals(0, manager.getHistory().size());
    }

    @Test
    void testHistorySizeLimit() {
        // Добавляем больше задач, чем максимальный размер истории
        for (int i = 0; i < 15; i++) {
            Task task = new Task("Task " + i, "Desc " + i, Status.NEW);
            task.setId(i);
            manager.add(task);
        }

        ArrayList<Task> history = manager.getHistory();
        assertEquals(10, history.size());
        assertEquals(5, history.get(0).getId()); // Первая задача должна быть с ID=5
        assertEquals(14, history.get(9).getId()); // Последняя - с ID=14
    }

    @Test
    void testDuplicateTaskAddition() {
        manager.add(task1);
        manager.add(task1); // Дубликат

        ArrayList<Task> history = manager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void testGetHistoryReturnsCopy() {
        manager.add(task1);
        ArrayList<Task> history1 = manager.getHistory();
        ArrayList<Task> history2 = manager.getHistory();

        assertNotSame(history1, history2);
        assertEquals(history1, history2);
    }

    @Test
    void testEmptyHistory() {
        assertTrue(manager.getHistory().isEmpty());
    }

    @Test
    void testTaskUpdateInHistory() {
        manager.add(task1);
        task1.setStatus(Status.DONE);

        ArrayList<Task> history = manager.getHistory();
        assertEquals(Status.DONE, history.get(0).getStatus());
    }
}