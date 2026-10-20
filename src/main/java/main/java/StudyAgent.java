package main.java;

import main.java.AIPlanner;
import main.java.FileStorage;
import java.util.List;
import java.io.IOException;

public class StudyAgent {
    private final AIPlanner planner;
    private final StudyTools tools;
    private StudyPlan plan;

    public StudyAgent() throws IOException {
        this.planner = new AIPlanner();
        this.tools = new StudyTools();
        this.plan = FileStorage.load();

        if (plan == null) {
            System.out.println("暂无历史计划，请先创建学习计划。");
        } else {
            System.out.println("已恢复学习计划：" + plan.getGoal());
        }
    }

    public boolean hasPlan() {
        return plan != null;
    }

    // 提供当前学习计划，供 Swing 界面读取
    public StudyPlan getPlan() {
        return plan;
    }

    public void createPlan(
            String goal, int days, int minutesPerDay)
            throws IOException {

        if (goal == null || goal.isBlank()) {
            throw new IllegalArgumentException("学习目标不能为空。");
        }

        if (days < 1 || days > 60) {
            throw new IllegalArgumentException(
                    "计划天数必须在 1～60 天之间。");
        }

        if (minutesPerDay < 1 || minutesPerDay > 720) {
            throw new IllegalArgumentException(
                    "每日学习时间必须在 1～720 分钟之间。");
        }

        plan = planner.createPlan(goal, days, minutesPerDay);
        FileStorage.save(plan);

        System.out.println("学习计划已生成并保存。");
        tools.showTasks(plan);
    }

    public void showTasks() {
        if (!hasPlan()) {
            System.out.println("请先创建学习计划。");
            return;
        }

        tools.showTasks(plan);
    }

    public void completeTask(int id) throws IOException {
        if (!hasPlan()) {
            System.out.println("请先创建学习计划。");
            return;
        }

        if (tools.completeTask(plan, id)) {
            FileStorage.save(plan);
        }
    }

    public void showProgress() {
        if (!hasPlan()) {
            System.out.println("请先创建学习计划。");
            return;
        }

        tools.showProgress(plan);
    }


    public void replan() throws IOException {
        if (!hasPlan()) {
            System.out.println("请先创建学习计划。");
            return;
        }

        List<StudyTask> unfinished = plan.getTasks().stream()
                .filter(task -> !task.isCompleted())
                .sorted(java.util.Comparator
                        .comparing(StudyTask::getDate)
                        .thenComparingInt(StudyTask::getId))
                .toList();

        if (unfinished.isEmpty()) {
            System.out.println("所有学习任务都已完成，无需重规划！");
            return;
        }

        System.out.println("当前有 " + unfinished.size() + " 项未完成任务。");

        // 先请求 AI。失败时不会修改当前计划。
        List<StudyTask> replacements =
                planner.replanTasks(plan.getGoal(), unfinished);

        if (replacements.size() != unfinished.size()) {
            throw new IOException("新计划任务数量不正确，原计划未修改。");
        }

        // 先准备替换方案，再一次性更新现有任务列表。
        java.util.Map<Integer, StudyTask> replacementById =
                new java.util.HashMap<>();

        for (StudyTask task : replacements) {
            if (replacementById.put(task.getId(), task) != null) {
                throw new IOException("新计划包含重复任务编号，原计划未修改。");
            }
        }

        List<StudyTask> updatedTasks =
                new java.util.ArrayList<>(plan.getTasks());

        for (int i = 0; i < updatedTasks.size(); i++) {
            StudyTask oldTask = updatedTasks.get(i);

            if (oldTask.isCompleted()) {
                continue;
            }

            StudyTask replacement = replacementById.get(oldTask.getId());

            if (replacement == null) {
                throw new IOException(
                        "新计划缺少任务 " + oldTask.getId()
                                + "，原计划未修改。");
            }

            updatedTasks.set(i, replacement);
        }

        // 所有校验通过后才更新原计划。
        plan.getTasks().clear();
        plan.getTasks().addAll(updatedTasks);

        try {
            FileStorage.save(plan);
        } catch (IOException e) {
            System.out.println(
                    "警告：新计划已在当前运行中更新，但保存失败。");
            throw e;
        }

        System.out.println("AI 动态重规划完成，并已保存新计划！");
        tools.showTasks(plan);
    }

}
