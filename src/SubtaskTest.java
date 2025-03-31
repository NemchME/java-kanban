import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private Subtask subtask;
    private final int testEpicId = 1;

    @BeforeEach
    void setUp() {
        subtask = new Subtask("Test Subtask", "Test Description", Status.NEW, testEpicId);
    }

    @Test
    void testSubtaskCreation() {
        assertNotNull(subtask);
        assertEquals("Test Subtask", subtask.getName());
        assertEquals("Test Description", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(testEpicId, subtask.getEpicId());
    }

    @Test
    void testSubtaskEqualityById() {
        Subtask sub1 = new Subtask("A", "B", Status.NEW, 1);
        Subtask sub2 = new Subtask("C", "D", Status.DONE, 2);
        sub1.setId(3);
        sub2.setId(3);
        assertEquals(sub1, sub2, "Подзадачи должны быть равны при одинаковом ID");
    }

    @Test
    void testSetEpicId() {
        int newEpicId = 5;
        subtask.setEpicId(newEpicId);
        assertEquals(newEpicId, subtask.getEpicId());
    }

    @Test
    void testSubtaskInheritance() {
        assertTrue(subtask instanceof Task, "Subtask должен наследоваться от Task");
    }

    @Test
    void testSubtaskStatusChange() {
        subtask.setStatus(Status.IN_PROGRESS);
        assertEquals(Status.IN_PROGRESS, subtask.getStatus());
    }

    @Test
    void testDifferentSubtasksNotEqual() {
        Subtask sub1 = new Subtask("A", "B", Status.NEW, 1);
        Subtask sub2 = new Subtask("A", "B", Status.NEW, 1);
        sub1.setId(1);
        sub2.setId(2);
        assertNotEquals(sub1, sub2, "Подзадачи с разными ID не должны быть равны");
    }

    @Test
    void testSubtaskWithSameFieldsButDifferentEpic() {
        Subtask sub1 = new Subtask("A", "B", Status.NEW, 1);
        Subtask sub2 = new Subtask("A", "B", Status.NEW, 2);
        sub1.setId(3);
        sub2.setId(3);
        assertEquals(sub1, sub2, "ID эпика не должен влиять на равенство подзадач");
    }
}