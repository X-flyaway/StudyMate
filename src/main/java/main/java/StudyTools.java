package main.java;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class StudyTools {

    public void showTasks(StudyPlan plan) {
        List<StudyTask> tasks = plan.getTasks();

        if (tasks.isEmpty()) {
            System.out.println("目前没有学习任务。");
            return;
        }

        System.out.println("\n学习目标：" + plan.getGoal());
        System.out.println("创建日期：" + plan.getCreatedAt());
        System.out.println("===== 学习任务 =====");

        tasks.stream()
                .sorted(Comparator.comparing(StudyTask::getDate)
                        .thenComparingInt(StudyTask::getId))
                .forEach(System.out::println);
    }

    public boolean completeTask(StudyPlan plan, int id) {
        for (StudyTask task : plan.getTasks()) {
            if (task.getId() == id) {
                if (task.isCompleted()) {
                    System.out.println("这项任务已经完成。");
                } else {
                    task.setCompleted(true);
                    System.out.println("任务已标记为完成！");
                }
                return true;
            }
        }

        System.out.println("没有找到编号为 " + id + " 的任务。");
        return false;
    }

    public void showProgress(StudyPlan plan) {
        List<StudyTask> tasks = plan.getTasks();
        int total = tasks.size();
        int completed = 0;
        int overdue = 0;
        LocalDate today = LocalDate.now();

        for (StudyTask task : tasks) {
            if (task.isCompleted()) {
                completed++;
            } else if (task.getDate().isBefore(today)) {
                overdue++;
            }
        }

        if (total == 0) {
            System.out.println("还没有学习任务。");
            return;
        }

        double rate = completed * 100.0 / total;

        System.out.printf(
                "总任务：%d，已完成：%d，未完成：%d，完成率：%.1f%%%n",
                total, completed, total - completed, rate
        );
        System.out.println("逾期未完成：" + overdue + " 项");
    }

    public int replan(StudyPlan plan) {
        LocalDate today = LocalDate.now();

        List<StudyTask> unfinished = plan.getTasks().stream()
                .filter(task -> !task.isCompleted())
                .sorted(Comparator.comparing(StudyTask::getDate)
                        .thenComparingInt(StudyTask::getId))
                .toList();

        if (unfinished.isEmpty()) {
            System.out.println("所有学习任务都已完成，无需重规划！");
            return 0;
        }

        System.out.println("发现 " + unfinished.size() + " 项未完成任务。");
        System.out.println("将从今天开始，按原有顺序重新安排这些任务。");

        int changed = 0;

        for (int i = 0; i < unfinished.size(); i++) {
            StudyTask task = unfinished.get(i);
            LocalDate newDate = today.plusDays(i);

            if (!task.getDate().equals(newDate)) {
                System.out.println(
                        "任务 " + task.getId()
                                + "：" + task.getDate()
                                + " → " + newDate
                );

                task.setDate(newDate);
                changed++;
            }
        }

        if (changed == 0) {
            System.out.println("任务日期已经合理，无需调整。");
        } else {
            System.out.println("重规划完成，共调整 " + changed + " 项任务。");
        }

        return changed;
    }
}
