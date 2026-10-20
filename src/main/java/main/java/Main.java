package main.java;

import java.io.IOException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            StudyAgent agent = new StudyAgent();

            while (true) {
                System.out.println("\n===== StudyMate 学习规划智能体 =====");
                System.out.println("1. 创建学习计划");
                System.out.println("2. 查看全部任务");
                System.out.println("3. 标记任务完成");
                System.out.println("4. 查看学习进度");
                System.out.println("5. 动态重规划");
                System.out.println("6. 退出");
                System.out.print("请选择功能：");

                String choice = scanner.nextLine().trim();

                try {
                    switch (choice) {
                        case "1" -> {
                            if (agent.hasPlan()) {
                                System.out.println(
                                        "注意：创建新计划会替换当前计划。");
                                System.out.print("确认替换吗？(y/n)：");

                                String confirm =
                                        scanner.nextLine().trim();

                                if (!confirm.equalsIgnoreCase("y")) {
                                    System.out.println("已取消创建。");
                                    break;
                                }
                            }

                            System.out.print("你想学习什么？");
                            String goal = scanner.nextLine().trim();

                            System.out.print("计划持续多少天？");
                            int days = Integer.parseInt(
                                    scanner.nextLine().trim());

                            System.out.print("每天学习多少分钟？");
                            int minutes = Integer.parseInt(
                                    scanner.nextLine().trim());

                            agent.createPlan(goal, days, minutes);
                        }

                        case "2" -> agent.showTasks();

                        case "3" -> {
                            System.out.print("请输入任务编号：");
                            int id = Integer.parseInt(
                                    scanner.nextLine().trim());
                            agent.completeTask(id);
                        }

                        case "4" -> agent.showProgress();

                        case "5" -> agent.replan();

                        case "6" -> {
                            System.out.println(
                                    "学习计划已保存在本地，感谢使用 StudyMate！");
                            return;
                        }

                        default ->
                                System.out.println("请输入 1～6。");
                    }

                } catch (NumberFormatException e) {
                    System.out.println("请输入有效的整数。");
                } catch (IllegalArgumentException e) {
                    System.out.println("输入错误：" + e.getMessage());
                } catch (IOException e) {
                    System.out.println(
                            "文件读写失败：" + e.getMessage());
                }
            }

        } catch (IOException e) {
            System.out.println(
                    "StudyMate 启动失败：" + e.getMessage());
        }
    }
}
