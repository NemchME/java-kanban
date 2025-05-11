package tests;

import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void testTaskEqualityById() {
        Task t1 = new Task("Task1", "Desc1", Status.NEW);
        Task t2 = new Task("Task2", "Desc2", Status.DONE);
        t1.setId(1);
        t2.setId(1);
        assertEquals(t1, t2);
    }
}