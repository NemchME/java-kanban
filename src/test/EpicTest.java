package test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.Managers;
import taskmanager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    private TaskManager manager;
    private Epic epic;

    @BeforeEach
    void init() {
        manager = Managers.getDefault();
        epic = manager.createEpic(new Epic("Epic", "Description"));
    }

    @Test
    void testEpicCreation() {
        assertNotNull(epic);
        assertEquals("Epic", epic.getName());
        assertEquals(Status.NEW, epic.getStatus());
        assertTrue(epic.getSubtaskIds().isEmpty());
    }

    @Test
    void testSubtaskAdditionToEpic() {
        Subtask sub = manager.createSubtask(new Subtask("Sub", "Desc", Status.NEW, epic.getId()));
        assertTrue(epic.getSubtaskIds().contains(sub.getId()));
    }

    @Test
    void testSubtaskRemovalFromEpic() {
        Subtask sub = manager.createSubtask(new Subtask("Sub", "Desc", Status.NEW, epic.getId()));
        manager.deleteSubtaskById(sub.getId());
        assertFalse(epic.getSubtaskIds().contains(sub.getId()));
    }

    @Test
    void testEpicStatusUpdates() {
        assertEquals(Status.NEW, epic.getStatus());

        Subtask sub1 = manager.createSubtask(new Subtask("Sub1", "Desc", Status.NEW, epic.getId()));
        assertEquals(Status.NEW, epic.getStatus());

        sub1.setStatus(Status.DONE);
        manager.updateSubtask(sub1);
        assertEquals(Status.DONE, epic.getStatus());

        Subtask sub2 = manager.createSubtask(new Subtask("Sub2", "Desc", Status.IN_PROGRESS, epic.getId()));
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        sub2.setStatus(Status.DONE);
        manager.updateSubtask(sub2);
        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    void testEpicCannotAddItselfAsSubtask() {
        int initialSize = epic.getSubtaskIds().size();
        epic.addSubtaskId(epic.getId());
        assertEquals(initialSize, epic.getSubtaskIds().size());
    }
}