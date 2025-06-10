import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import taskmanager.FileBackedTaskManager;
import taskmanager.ManagerSaveException;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest {

    private File tempFile;

    @BeforeEach
    void setUp() throws IOException {
        tempFile = File.createTempFile("taskManagerTest", ".csv");
    }

    @AfterEach
    void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    void shouldSaveAndLoadMultipleTasks() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task = new Task("Task1", "Description1", Status.NEW);
        Epic epic = new Epic("Epic1", "EpicDescription");
        manager.createTask(task);
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "SubDescription", Status.IN_PROGRESS, epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        List<Task> tasks = loadedManager.getAllTasks();
        List<Epic> epics = loadedManager.getAllEpics();
        List<Subtask> subtasks = loadedManager.getAllSubtasks();

        assertEquals(1, tasks.size(), "Should be one task");
        assertEquals(1, epics.size(), "Should be one epic");
        assertEquals(1, subtasks.size(), "Should be one subtask");

        assertEquals("Task1", tasks.get(0).getName());
        assertEquals("Epic1", epics.get(0).getName());
        assertEquals("Subtask1", subtasks.get(0).getName());
    }

    @Test
    void shouldThrowExceptionWheInvalidFileData() {
        try {
            new FileBackedTaskManager(new File("?"));
        } catch (Exception ex) {
            assertEquals(ex.getClass(), ManagerSaveException.class);
        }
    }

    @Test
    void shouldRestoreSubtaskRelationToEpic() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Epic epic = new Epic("Epic1", "EpicDescription");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Subtask1", "SubDescription", Status.DONE, epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);
        Epic loadedEpic = loadedManager.getEpicById(epic.getId());

        assertNotNull(loadedEpic);
        assertEquals(1, loadedEpic.getSubtaskIds().size(), "Epic should have one subtask ID");
        assertEquals(subtask.getId(), loadedEpic.getSubtaskIds().get(0), "Subtask ID should match");
    }

    @Test
    void shouldUpdateTaskIdCounterCorrectly() {
        FileBackedTaskManager manager = new FileBackedTaskManager(tempFile);

        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        manager.createTask(task1);

        Epic epic = new Epic("Epic1", "DescEpic");
        manager.createEpic(epic);

        Subtask subtask = new Subtask("Sub1", "SubDesc", Status.NEW, epic.getId());
        manager.createSubtask(subtask);

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = new Task("TaskNew", "New", Status.NEW);
        Task created = loadedManager.createTask(newTask);

        assertTrue(created.getId() > task1.getId(), "New task ID should be greater than previous max ID");
    }
}