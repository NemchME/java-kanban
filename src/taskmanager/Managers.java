package taskmanager;

import java.io.File;

public class Managers {
    private Managers() {
    }

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static TaskManager getFileBackedTaskManager(File file) {
        return new FileBackedTaskManager(file);
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        return FileBackedTaskManager.loadFromFile(file);
    }
}