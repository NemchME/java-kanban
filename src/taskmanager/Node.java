package taskmanager;

import tasks.Task;

public class Node {
    Task task;
    Node prev;
    Node next;

    public Node(Task task) {
        this.task = task;
        this.prev = null;
        this.next = null;
    }
}
