import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;
import tasks.Epic;
import tasks.Status;
import tasks.Subtask;
import tasks.Task;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();

        Task task1 = new Task("Помыть посуду", "Помыть всю посуду вечером", Status.NEW);
        Task task2 = new Task("Сделать уроки", "Выполнить домашнее задание", Status.IN_PROGRESS);
        manager.createTask(task1);
        manager.createTask(task2);

        Epic epic1 = new Epic("Переезд", "Организовать переезд в новый офис");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Упаковать вещи", "Упаковать офисные принадлежности",
                Status.NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Нанять грузчиков", "Найти транспортную компанию",
                Status.DONE, epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        System.out.println("=== Первоначальное состояние ===");
        printAllTasks(manager);

        System.out.println("\n=== История просмотров (должна быть пустая) ===");
        printHistory(manager.getHistory());

        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());

        System.out.println("\n=== История после просмотра задач ===");
        printHistory(manager.getHistory());

        System.out.println("\n=== Попытка получить несуществующую задачу ===");
        manager.getTaskById(100);

        task1.setStatus(Status.DONE);
        manager.updateTask(task1);

        manager.deleteTaskById(task2.getId());

        System.out.println("\n=== Состояние после изменений ===");
        printAllTasks(manager);

        System.out.println("\n=== История просмотров после изменений ===");
        printHistory(manager.getHistory());
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("\nЭпики:");
        for (Epic epic : manager.getAllEpics()) {
            System.out.println(epic);

            List<Subtask> subtasks = manager.getSubtasksByEpicId(epic.getId());
            if (!subtasks.isEmpty()) {
                System.out.println("  Подзадачи:");
                for (Subtask subtask : subtasks) {
                    System.out.println("  --> " + subtask);
                }
            }
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }

    private static void printHistory(List<Task> history) {
        if (history.isEmpty()) {
            System.out.println("История просмотров пуста");
            return;
        }

        for (Task task : history) {
            if (task instanceof Epic) {
                System.out.println("[Эпик] " + task);
            } else if (task instanceof Subtask) {
                System.out.println("[Подзадача] " + task);
            } else {
                System.out.println("[Задача] " + task);
            }
        }
    }
}