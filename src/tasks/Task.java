package tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task implements Comparable<Task> {
    protected String name;
    protected String description;
    protected int id;
    protected Status status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String name, String description, Status status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public Duration getDuration() {
        return duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return (startTime != null && duration != null) ? startTime.plus(duration) : null;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getStartTimeString() {
        return startTime == null ? "" : startTime.toString();
    }

    public String getDurationMinutesString() {
        return duration == null ? "" : String.valueOf(duration.toMinutes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "tasks.Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }

    @Override
    public int compareTo(Task other) {
        if (this.startTime == null && other.startTime == null) {
            return Integer.compare(this.id, other.id);
        }
        if (this.startTime == null) return 1;
        if (other.startTime == null) return -1;

        int cmp = this.startTime.compareTo(other.startTime);
        if (cmp != 0) return cmp;

        return Integer.compare(this.id, other.id);
    }
}