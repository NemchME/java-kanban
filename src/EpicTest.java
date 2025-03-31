import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private Epic epic;
    private TaskManager manager;

    @BeforeEach
    void setUp() {
        manager = Managers.getDefault();
        epic = manager.createEpic(new Epic("Test Epic", "Test Description"));
    }

    @Test
    void testEpicCreation() {
        assertNotNull(epic);
        assertEquals("Test Epic", epic.getName());
        assertEquals("Test Description", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void testAddSubtaskToEpic() {
        Subtask subtask = manager.createSubtask(
                new Subtask("Subtask 1", "Desc 1", Status.NEW, epic.getId()));

        assertEquals(1, epic.getSubtaskIds().size());
        assertEquals(subtask.getId(), epic.getSubtaskIds().getFirst());
    }

    @Test
    void testEpicStatusUpdate() {
        // Эпик без подзадач должен быть NEW
        assertEquals(Status.NEW, epic.getStatus());

        // Добавляем подзадачу NEW
        Subtask subtask1 = manager.createSubtask(
                new Subtask("Sub 1", "Desc", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, epic.getStatus());

        // Меняем одну подзадачу на DONE
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        assertEquals(Status.DONE, epic.getStatus());

        // Добавляем вторую подзадачу IN_PROGRESS
        Subtask subtask2 = manager.createSubtask(
                new Subtask("Sub 2", "Desc", Status.IN_PROGRESS, epic.getId()));
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        // Все подзадачи DONE
        subtask2.setStatus(Status.DONE);
        manager.updateSubtask(subtask2);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void testEpicToString() {
        String expected = "Epic{name='Test Epic', description='Test Description', " +
                "id=" + epic.getId() + ", status=NEW, subtaskIds=[]}";
        assertEquals(expected, epic.toString());
    }
}