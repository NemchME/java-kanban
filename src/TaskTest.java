import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    @Test
    void testTaskEqualityById() {
        Task task1 = new Task("A", "B", Status.NEW);
        Task task2 = new Task("C", "D", Status.DONE);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2);
    }
}