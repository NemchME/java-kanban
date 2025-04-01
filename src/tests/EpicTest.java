package tests;

import taskManager.Managers;
import taskManager.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;

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
        assertTrue(epic.getSubtaskIds().contains(subtask.getId()));
    }

    @Test
    void testEpicCannotAddItselfAsSubtask() {
        int initialSize = epic.getSubtaskIds().size();
        epic.addSubtaskId(epic.getId()); // Попытка добавить себя как подзадачу

        assertEquals(initialSize, epic.getSubtaskIds().size(),
                "Размер списка подзадач не должен измениться");
    }

    @Test
    void testSubtaskCannotBeItsOwnEpic() {
        Subtask subtask = manager.createSubtask(
                new Subtask("Test", "Desc", Status.NEW, epic.getId()));

        // Попытка создать подзадачу с эпиком, равным её ID
        Subtask invalidSubtask = new Subtask("Invalid", "Desc", Status.NEW, subtask.getId());
        assertNull(manager.createSubtask(invalidSubtask),
                "Должен вернуть null, так как эпик не существует");
    }

    @Test
    void testEpicEqualsById() {
        Epic epic1 = new Epic("Epic 1", "Desc");
        epic1.setId(1);

        Epic epic2 = new Epic("Epic 2", "Different Desc");
        epic2.setId(1);

        Epic epic3 = new Epic("Epic 3", "Desc");
        epic3.setId(2);

        assertEquals(epic1, epic2, "Эпики с одинаковым id должны быть равны");
        assertNotEquals(epic1, epic3, "Эпики с разным id не должны быть равны");
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

        // Смешанные статусы
        Subtask subtask3 = manager.createSubtask(
                new Subtask("Sub 3", "Desc", Status.NEW, epic.getId()));
        Subtask subtask4 = manager.createSubtask(
                new Subtask("Sub 4", "Desc", Status.DONE, epic.getId()));
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void testEpicToString() {
        String expected = "tasks.Epic{name='Test Epic', description='Test Description', " +
                "id=" + epic.getId() + ", status=NEW, subtaskIds=[]}";
        assertEquals(expected, epic.toString());
    }

    @Test
    void testRemoveSubtaskFromEpic() {
        Subtask subtask = manager.createSubtask(
                new Subtask("Sub", "Desc", Status.NEW, epic.getId()));

        assertTrue(epic.getSubtaskIds().contains(subtask.getId()));

        manager.deleteSubtaskById(subtask.getId());
        assertFalse(epic.getSubtaskIds().contains(subtask.getId()));
        assertEquals(Status.NEW, epic.getStatus());
    }
}