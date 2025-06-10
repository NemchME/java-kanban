package tasks;

import taskmanager.TaskManager;
import taskmanager.Managers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {
    private final ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(String name, String description) {
        super(name, description, Status.NEW);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        if (subtaskId == this.id) return;
        subtaskIds.add(subtaskId);
    }

    public void removeSubtaskId(int subtaskId) {
        subtaskIds.remove((Integer) subtaskId);
    }

    @Override
    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public Duration getDuration() {
        Duration total = Duration.ZERO;
        TaskManager tm = Managers.getDefault();
        for (int id : subtaskIds) {
            Task t = tm.getSubtaskById(id);
            if (t != null && t.getDuration() != null) total = total.plus(t.getDuration());
        }
        return total;
    }

    @Override
    public LocalDateTime getStartTime() {
        LocalDateTime earliest = null;
        TaskManager tm = Managers.getDefault();
        for (int id : subtaskIds) {
            Task t = tm.getSubtaskById(id);
            if (t != null && t.getStartTime() != null) {
                if (earliest == null || t.getStartTime().isBefore(earliest)) {
                    earliest = t.getStartTime();
                }
            }
        }
        return earliest;
    }

    @Override
    public LocalDateTime getEndTime() {
        LocalDateTime latest = null;
        TaskManager tm = Managers.getDefault();
        for (int id : subtaskIds) {
            Task t = tm.getSubtaskById(id);
            if (t != null && t.getEndTime() != null) {
                if (latest == null || t.getEndTime().isAfter(latest)) {
                    latest = t.getEndTime();
                }
            }
        }
        return latest;
    }

    @Override
    public String toString() {
        return "tasks.Epic{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", subtaskIds=" + subtaskIds +
                '}';
    }
}