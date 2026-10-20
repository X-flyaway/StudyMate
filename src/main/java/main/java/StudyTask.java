package main.java;

import java.io.Serializable;
import java.time.LocalDate;

public class StudyTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int id;
    private LocalDate date;
    private final String title;
    private final int minutes;
    private boolean completed;

    public StudyTask(int id, LocalDate date, String title, int minutes) {
        this.id = id;
        this.date = date;
        this.title = title;
        this.minutes = minutes;
        this.completed = false;
    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public int getMinutes() {
        return minutes;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public String toString() {
        String status = completed ? "已完成" : "未完成";
        return id + ". [" + status + "] "
                + date + " | " + title + " | "
                + minutes + " 分钟";
    }
}
