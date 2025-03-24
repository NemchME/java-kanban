public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();

        Task task1 = taskManager.createTask(new Task("Task 1", "Description of task 1",
                Status.NEW));
        Task task2 = taskManager.createTask(new Task("Task 2", "Description of task 2",
                Status.IN_PROGRESS));

        Epic epic1 = taskManager.createEpic(new Epic("Epic 1", "Epic with two subtasks"));
        Subtask subtask1 = taskManager.createSubtask(new Subtask("Subtask 1",
                "Description of subtask 1", Status.NEW, epic1.getId()));
        Subtask subtask2 = taskManager.createSubtask(new Subtask("Subtask 2",
                "Description of subtask 2", Status.DONE, epic1.getId()));

        Epic epic2 = taskManager.createEpic(new Epic("Epic 2", "Epic with one subtask"));
        Subtask subtask3 = taskManager.createSubtask(new Subtask("Subtask 3",
                "Description of subtask 3", Status.NEW, epic2.getId()));

        System.out.println("All Tasks:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nAll Epics:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nAll Subtasks:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        task1.setStatus(Status.DONE);
        task2.setStatus(Status.DONE);

        subtask1.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask1);

        subtask2.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtask2);

        subtask3.setStatus(Status.DONE);
        taskManager.updateSubtask(subtask3);

        System.out.println("\nAll Tasks After Status Change:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nAll Epics After Status Change:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nAll Subtasks After Status Change:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        taskManager.deleteTaskById(task1.getId());
        taskManager.deleteEpicById(epic1.getId());

        System.out.println("\nAll Tasks After Deletion:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nAll Epics After Deletion:");
        for (Epic epic : taskManager.getAllEpics()) {
            System.out.println(epic);
        }

        System.out.println("\nAll Subtasks After Deletion:");
        for (Subtask subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}
