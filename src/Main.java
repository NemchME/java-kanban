import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;
import taskmanager.http.HttpTaskServer;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        if (true) {
            try {
                HttpTaskServer server = new HttpTaskServer();
                server.start();
            } catch (Exception e) {
                e.printStackTrace();
            }

            return;
        }
        TaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Помыть посуду", "Помыть всю посуду вечером", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 6, 10, 18, 0));
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Сделать уроки", "Выполнить домашнее задание", Status.IN_PROGRESS);
        task2.setStartTime(LocalDateTime.of(2025, 6, 10, 19, 0));
        task2.setDuration(Duration.ofMinutes(60));
        manager.createTask(task2);

        Epic epic1 = new Epic("Переезд", "Организовать переезд в новый офис");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Упаковать вещи", "Упаковать офисные принадлежности",
                Status.NEW, epic1.getId());
        subtask1.setStartTime(LocalDateTime.of(2025, 6, 10, 14, 0));
        subtask1.setDuration(Duration.ofMinutes(90));
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Нанять грузчиков", "Найти транспортную компанию",
                Status.DONE, epic1.getId());
        subtask2.setStartTime(LocalDateTime.of(2025, 6, 10, 16, 0));
        subtask2.setDuration(Duration.ofMinutes(60));
        manager.createSubtask(subtask2);

        Epic epicWithoutSubtasks = new Epic("Пустой эпик", "Эпик без подзадач");
        manager.createEpic(epicWithoutSubtasks);

        System.out.println("=== Первоначальное состояние ===");
        printAllTasks(manager);

        System.out.println("\n=== Приоритетный список задач ===");
        printPrioritizedTasks(manager);

        System.out.println("\n=== История просмотров (должна быть пустая) ===");
        printHistory(manager.getHistory());

        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());
        manager.getTaskById(task2.getId());
        manager.getEpicById(epicWithoutSubtasks.getId());
        manager.getTaskById(task1.getId());
        manager.getSubtaskById(subtask2.getId());

        System.out.println("\n=== История после просмотра задач ===");
        printHistory(manager.getHistory());

        System.out.println("\n=== Попытка получить несуществующую задачу ===");
        manager.getTaskById(100);

        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        manager.deleteTaskById(task2.getId());

        Subtask subtask3 = new Subtask("Распаковать вещи", "Распаковать на новом месте",
                Status.NEW, epic1.getId());
        subtask3.setStartTime(LocalDateTime.of(2025, 6, 10, 18, 30));
        subtask3.setDuration(Duration.ofMinutes(45));
        manager.createSubtask(subtask3);

        System.out.println("\n=== Состояние перед удалением эпика ===");
        printAllTasks(manager);

        System.out.println("\n=== История перед удалением эпика ===");
        printHistory(manager.getHistory());

        manager.deleteEpicById(epic1.getId());

        System.out.println("\n=== Состояние после удаления эпика ===");
        printAllTasks(manager);

        System.out.println("\n=== История после удаления эпика ===");
        printHistory(manager.getHistory());

        System.out.println("\n=== Состояние после всех изменений ===");
        printAllTasks(manager);

        System.out.println("\n=== Приоритетный список задач после всех изменений ===");
        printPrioritizedTasks(manager);

        System.out.println("\n=== История просмотров после всех изменений ===");
        printHistory(manager.getHistory());
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            printTaskWithTime(task);
        }
        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            printTaskWithTime(epic);

            List<Subtask> subtasks = manager.getSubtasksByEpicId(epic.getId());
            if (!subtasks.isEmpty()) {
                System.out.println("  Подзадачи:");
                for (Subtask subtask : subtasks) {
                    System.out.print("  --> ");
                    printTaskWithTime(subtask);
                }
            }
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            printTaskWithTime(subtask);
        }
    }

    private static void printHistory(List<Task> history) {
        if (history.isEmpty()) {
            System.out.println("История просмотров пуста");
        } else {
            for (Task task : history) {
                printTaskWithTime(task);
            }
        }
    }

    private static void printTaskWithTime(Task task) {
        System.out.println(task.getClass().getSimpleName() + " ID=" + task.getId()
                + ", status=" + task.getStatus()
                + ", start=" + task.getStartTime()
                + ", duration=" + (task.getDuration() != null ? task.getDuration().toMinutes() + " мин" : "N/A")
                + ", end=" + task.getEndTime());
    }

    private static void printPrioritizedTasks(TaskManager manager) {
        for (Task task : manager.getPrioritizedTasks()) {
            printTaskWithTime(task);
        }
    }
}