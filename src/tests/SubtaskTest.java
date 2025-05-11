package tests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    private Subtask subtask;

    @BeforeEach
    void setUp() {
        subtask = new Subtask("Subtask", "Description", Status.NEW, 1);
        subtask.setId(10);
    }

    @Test
    void testSubtaskFields() {
        assertEquals("Subtask", subtask.getName());
        assertEquals("Description", subtask.getDescription());
        assertEquals(Status.NEW, subtask.getStatus());
        assertEquals(1, subtask.getEpicId());
    }

    @Test
    void testSetEpicId() {
        subtask.setEpicId(5);
        assertEquals(5, subtask.getEpicId());
    }

    @Test
    void testSubtaskEqualsById() {
        Subtask copy = new Subtask("Other", "Other", Status.DONE, 99);
        copy.setId(10);
        assertEquals(subtask, copy);
    }

    @Test
    void testSubtaskNotEqualIfDifferentId() {
        Subtask other = new Subtask("Same", "Same", Status.NEW, 1);
        other.setId(11);
        assertNotEquals(subtask, other);
    }

    @Test
    void testSubtaskIsInstanceOfTask() {
        assertTrue(subtask instanceof Task);
    }
}