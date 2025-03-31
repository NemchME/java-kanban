import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Получаем менеджер задач
        TaskManager manager = Managers.getDefault();

        // Создаем задачи
        Task task1 = new Task("Помыть посуду", "Помыть посуду вечером");
        Task task2 = new Task("Сделать уроки", "Выполнить задания по Java");
        manager.createTask(task1);
        manager.createTask(task2);

        // Создаем эпик с подзадачами
        Epic epic1 = new Epic("Переезд", "Организовать переезд в другой город");
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Собрать коробки", "Купить и собрать коробки", epic1.getId());
        Subtask subtask2 = new Subtask("Упаковать вещи", "Упаковать одежду и книги", epic1.getId());
        manager.createSubtask(subtask1);
        manager.createSubtask(subtask2);

        // Проверяем историю просмотров (пока пустая)
        System.out.println("\nИстория после создания задач:");
        printHistory(manager);

        // Вызываем методы для добавления в историю
        manager.getTaskById(task1.getId());
        manager.getEpicById(epic1.getId());
        manager.getSubtaskById(subtask1.getId());

        System.out.println("\nИстория после просмотра задач:");
        printHistory(manager);

        // Просматриваем еще раз некоторые задачи
        manager.getTaskById(task2.getId());
        manager.getSubtaskById(subtask2.getId());
        manager.getEpicById(epic1.getId()); // Повторный просмотр

        System.out.println("\nИстория после дополнительных просмотров:");
        printHistory(manager);

        // Проверяем лимит истории (10 элементов)
        for (int i = 3; i <= 12; i++) {
            Task task = new Task("Тестовая задача " + i, "Описание");
            manager.createTask(task);
            manager.getTaskById(task.getId());
        }

        System.out.println("\nИстория после заполнения (должна содержать 10 последних просмотров):");
        printHistory(manager);

        // Полная печать всех задач
        System.out.println("\nВсе задачи в системе:");
        printAllTasks(manager);
    }

    private static void printHistory(TaskManager manager) {
        List<Task> history = manager.getHistory();
        if (history.isEmpty()) {
            System.out.println("История просмотров пуста");
            return;
        }

        System.out.println("История просмотров (" + history.size() + "):");
        for (Task task : history) {
            String type = "";
            if (task instanceof Epic) {
                type = "Эпик";
            } else if (task instanceof Subtask) {
                type = "Подзадача";
            } else {
                type = "Задача";
            }
            System.out.printf("[%s] %s (ID=%d)%n", type, task.getName(), task.getId());
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("\nОбычные задачи:");
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
                    System.out.println("  - " + subtask);
                }
            }
        }

        System.out.println("\nВсе подзадачи:");
        for (Subtask subtask : manager.getAllSubtasks()) {
            System.out.println(subtask);
        }
    }
}