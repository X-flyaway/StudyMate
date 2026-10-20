package main.java;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudyPlan implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String goal;
    private final LocalDate createdAt;
    private final List<StudyTask> tasks;
    private int nextTaskId;

    public StudyPlan(String goal) {
        this.goal = goal;
        this.createdAt = LocalDate.now();
        this.tasks = new ArrayList<>();
        this.nextTaskId = 1;
    }

    public String getGoal() {
        return goal;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public List<StudyTask> getTasks() {
        return tasks;
    }

    public int getNextTaskId() {
        return nextTaskId++;
    }

    public void addTask(StudyTask task) {
        tasks.add(task);
    }
}
