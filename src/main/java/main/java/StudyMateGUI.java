
package main.java;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.awt.Dimension;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.prefs.Preferences;
import java.awt.AlphaComposite;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JPanel;
import java.awt.Color;
import java.awt.RenderingHints;

public class StudyMateGUI extends JFrame {

    private final JTextField goalField =
            new JTextField("学习 Java 基础", 20);

    private final JSpinner daysSpinner =
            new JSpinner(new SpinnerNumberModel(7, 1, 60, 1));

    private final JSpinner minutesSpinner =
            new JSpinner(new SpinnerNumberModel(60, 1, 720, 15));

    private final JTextField taskIdField =
            new JTextField(5);

    private final JTextArea resultArea =
            new JTextArea();

    // 学习进度条
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    // 任务卡片容器
    private final JPanel taskPanel = new JPanel();

    // 进度说明文字
    private final JLabel progressLabel = new JLabel("学习进度：0%");

    private final JButton createButton =
            new JButton("生成学习计划");

    private final JButton tasksButton =
            new JButton("查看任务");

    private final JButton completeButton =
            new JButton("标记完成");

    private final JButton progressButton =
            new JButton("查看进度");

    private final JButton replanButton =
            new JButton("AI 重新规划");

    // 选择背景图片的按钮
    private final JButton backgroundButton =
            new JButton("选择背景水印");

    // 保存用户设置的图片路径
    private final Preferences preferences =
            Preferences.userNodeForPackage(StudyMateGUI.class);

    // 保存图片路径时使用的键名
    private static final String BACKGROUND_PATH_KEY =
            "backgroundImagePath";

    private final JButton[] actionButtons = {
            createButton, tasksButton, completeButton,
            progressButton, replanButton
    };

    private final StudyAgent agent;

    private WatermarkPanel watermarkPanel;

    @FunctionalInterface
    private interface IOAction {
        void run() throws Exception;
    }

    public StudyMateGUI() throws IOException {
        setTitle("StudyMate - AI 学习规划助手");
        // 竖向窗口：宽度较窄，高度较高
        setSize(460, 760);
        setMinimumSize(new Dimension(400, 600));

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLocationRelativeTo(null);

        buildUI();

        // 将原本输出到控制台的内容显示到窗口
        redirectOutput();

        // 使用现有的学习智能体和本地存档
        agent = new StudyAgent();

        resultArea.append("\nStudyMate 已启动，可以开始使用。\n");

        refreshTaskCards();
    }

    private void buildUI() {
        watermarkPanel = new WatermarkPanel();
        watermarkPanel.setLayout(new BorderLayout());

// 恢复上次保存的背景图片
        String savedPath = preferences.get(
                BACKGROUND_PATH_KEY, ""
        );
        watermarkPanel.setBackgroundImage(savedPath);

        watermarkPanel.setBorder(
                BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JLabel title = new JLabel(
                "StudyMate · AI 学习规划助手",
                SwingConstants.CENTER);

        title.setFont(new Font("微软雅黑", Font.BOLD, 24));

        JPanel inputPanel =
                new JPanel(new GridLayout(4, 2, 10, 10));
        inputPanel.setOpaque(false);

        inputPanel.add(new JLabel("学习目标："));
        inputPanel.add(goalField);

        inputPanel.add(new JLabel("计划天数（1～60）："));
        inputPanel.add(daysSpinner);

        inputPanel.add(new JLabel("每日学习分钟数："));
        inputPanel.add(minutesSpinner);

        inputPanel.add(new JLabel("要完成的任务编号："));
        inputPanel.add(taskIdField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.setOpaque(false);

        buttonPanel.add(createButton);
        buttonPanel.add(tasksButton);
        buttonPanel.add(completeButton);
        buttonPanel.add(progressButton);
        buttonPanel.add(replanButton);

        backgroundButton.addActionListener(
                e -> chooseBackgroundImage()
        );

        buttonPanel.add(backgroundButton);

        JPanel topPanel =
                new JPanel(new BorderLayout(10, 10));
        topPanel.setOpaque(false);

        // 进度条区域
        progressBar.setStringPainted(true);
        progressBar.setValue(0);
        progressBar.setString("0%");

        JPanel progressPanel = new JPanel(new BorderLayout(8, 5));
        progressPanel.setOpaque(false);
        progressPanel.setBorder(
                BorderFactory.createEmptyBorder(8, 0, 8, 0));

        progressPanel.add(progressLabel, BorderLayout.NORTH);
        progressPanel.add(progressBar, BorderLayout.CENTER);

        topPanel.add(title, BorderLayout.NORTH);
        topPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel controlsPanel = new JPanel(new BorderLayout());
        controlsPanel.setOpaque(false);
        controlsPanel.add(buttonPanel, BorderLayout.NORTH);
        controlsPanel.add(progressPanel, BorderLayout.SOUTH);

        topPanel.add(controlsPanel, BorderLayout.SOUTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);

        // 任务卡片容器
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setOpaque(false);
        taskPanel.setBorder(
                BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane taskScrollPane = new JScrollPane(taskPanel);
        taskScrollPane.setOpaque(false);
        taskScrollPane.getViewport().setOpaque(false);
        taskScrollPane.getViewport().setBackground(
                new Color(250, 247, 240, 80)
        );
        taskScrollPane.setBorder(
                BorderFactory.createTitledBorder("我的学习任务"));



        JTabbedPane tabs = new JTabbedPane();
        tabs.setOpaque(false);
        tabs.setBackground(new Color(250, 247, 240));

// 取消标签页内容区域的默认背景填充
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void paintContentBorder(
                    Graphics g,
                    int tabPlacement,
                    int selectedIndex) {
                // 不绘制默认的内容区底色和边框
            }
        });

        tabs.addTab("任务卡片", taskScrollPane);


        JScrollPane logScrollPane = new JScrollPane(resultArea);
        logScrollPane.setOpaque(false);
        logScrollPane.getViewport().setOpaque(false);

        tabs.addTab("操作日志", logScrollPane);


        watermarkPanel.add(topPanel, BorderLayout.NORTH);
        watermarkPanel.add(tabs, BorderLayout.CENTER);

        setContentPane(watermarkPanel);

        // 生成 AI 学习计划
        createButton.addActionListener(e -> {
            String goal = goalField.getText().trim();
            int days = (Integer) daysSpinner.getValue();
            int minutes = (Integer) minutesSpinner.getValue();

            if (goal.isBlank()) {
                JOptionPane.showMessageDialog(
                        this, "请输入学习目标。");
                return;
            }

            runAction("正在请求 DeepSeek，生成学习计划……",
                    () -> agent.createPlan(goal, days, minutes));
        });

        // 查看任务
        tasksButton.addActionListener(e ->
                runAction("正在读取学习任务……",
                        agent::showTasks));

        // 标记任务完成
        completeButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(
                        taskIdField.getText().trim());

                runAction("正在更新任务状态……",
                        () -> agent.completeTask(id));

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(
                        this, "请输入有效的任务编号，例如 1。");
            }
        });

        // 查看进度
        progressButton.addActionListener(e ->
                runAction("正在统计学习进度……",
                        agent::showProgress));

        // AI 动态重规划
        replanButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                    this,
                    "让 AI 重新安排所有未完成的任务吗？",
                    "确认重新规划",
                    JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                runAction("正在请求 DeepSeek，重新规划任务……",
                        agent::replan);
            }
        });


    }



    private class TransparentCardPanel extends JPanel {

        private final Color cardColor;

        public TransparentCardPanel(Color cardColor) {
            this.cardColor = cardColor;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            // 不绘制默认的不透明背景
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                    RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON
            );

            // 半透明绘制：0.45 表示 45% 不透明度
            g2.setComposite(AlphaComposite.SrcOver);

            g2.setColor(new Color(
                    cardColor.getRed(),
                    cardColor.getGreen(),
                    cardColor.getBlue(),
                    115
            ));

            g2.fillRoundRect(
                    0, 0,
                    getWidth(), getHeight(),
                    18, 18
            );

            g2.dispose();
        }
    }



    private class WatermarkPanel extends JPanel {

        private Image backgroundImage;

        public WatermarkPanel() {
            setOpaque(true);
            setBackground(new Color(250, 247, 240));
        }

        public void setBackgroundImage(String path) {
            if (path == null || path.isEmpty()) {
                backgroundImage = null;
            } else {
                File file = new File(path);

                if (file.exists()) {
                    backgroundImage = new ImageIcon(path).getImage();
                } else {
                    backgroundImage = null;
                }
            }

            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (backgroundImage == null) {
                return;
            }

            Graphics2D g2 = (Graphics2D) g.create();

            // 平滑缩放背景图片
            g2.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
            );

            // 背景图透明度：0.25 表示 25% 不透明
            g2.setComposite(
                    AlphaComposite.getInstance(
                            AlphaComposite.SRC_OVER, 0.25f
                    )
            );

            // 图片铺满整个窗口背景
            g2.drawImage(
                    backgroundImage,
                    0, 0,
                    getWidth(), getHeight(),
                    this
            );

            g2.dispose();
        }

    }

    private void chooseBackgroundImage() {
        JFileChooser chooser = new JFileChooser();

        // 只显示常见图片格式
        FileNameExtensionFilter filter =
                new FileNameExtensionFilter(
                        "图片文件（JPG、PNG、GIF）",
                        "jpg", "jpeg", "png", "gif"
                );

        chooser.setFileFilter(filter);
        chooser.setDialogTitle("选择背景水印图片");

        // 如果之前选择过图片，优先打开上次的文件夹
        String savedPath = preferences.get(
                BACKGROUND_PATH_KEY, "");

        if (!savedPath.isEmpty()) {
            File savedFile = new File(savedPath);

            if (savedFile.exists()) {
                chooser.setCurrentDirectory(
                        savedFile.getParentFile());
            }
        }

        // 打开文件选择窗口
        int result = chooser.showOpenDialog(this);

        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            // 保存完整路径，不需要写死路径
            String imagePath = selectedFile.getAbsolutePath();

            preferences.put(BACKGROUND_PATH_KEY, imagePath);

            if (watermarkPanel != null) {
                watermarkPanel.setBackgroundImage(imagePath);
            }

            resultArea.append(
                    "\n已选择背景图片：" + imagePath + "\n"
            );
        }
    }

    // 根据当前学习计划刷新卡片和进度
    private void refreshTaskCards() {
        StudyPlan plan = agent.getPlan();

        taskPanel.removeAll();

        if (plan == null || plan.getTasks().isEmpty()) {
            taskPanel.add(new JLabel("暂无学习任务，请先生成学习计划。"));

            progressBar.setValue(0);
            progressBar.setString("0%");
            progressLabel.setText("学习进度：0%");

            taskPanel.revalidate();
            taskPanel.repaint();
            return;
        }

        int total = plan.getTasks().size();

        int completed = (int) plan.getTasks().stream()
                .filter(StudyTask::isCompleted)
                .count();

        int percent = (int) Math.round(completed * 100.0 / total);

        progressBar.setValue(percent);
        progressBar.setString(percent + "%");
        progressLabel.setText(
                "学习进度：" + completed + " / " + total + " 项已完成");

        for (StudyTask task : plan.getTasks()) {
            boolean done = task.isCompleted();

            JPanel card = new TransparentCardPanel(
                    done
                            ? new Color(180, 230, 190)
                            : new Color(235, 240, 250)
            );

            card.setLayout(new BorderLayout(10, 5));
            card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(4, 4, 4, 4),
                    BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(
                                    done ? new Color(105, 190, 130)
                                            : new Color(200, 210, 225)),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)
                    )
            ));

            String status = done ? "✓ 已完成" : "○ 未完成";

            JLabel nameLabel = new JLabel(
                    "任务 " + task.getId() + "：" + task.getTitle());

            nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
            nameLabel.setForeground(
                    done ? new Color(40, 125, 65)
                            : new Color(45, 55, 70));

            JLabel detailLabel = new JLabel(
                    "日期：" + task.getDate()
                            + "    预计：" + task.getMinutes() + " 分钟"
                            + "    状态：" + status);

            detailLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            detailLabel.setForeground(
                    done ? new Color(40, 125, 65)
                            : new Color(100, 110, 125));

            JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
            textPanel.setOpaque(false);
            textPanel.add(nameLabel);
            textPanel.add(detailLabel);


            card.add(textPanel, BorderLayout.CENTER);

// 为未完成任务添加“标记完成”按钮
            if (!done) {
                JButton completeButton = new JButton("标记完成");
                completeButton.setFocusPainted(false);

                completeButton.setBackground(new Color(220, 240, 225));
                completeButton.setForeground(new Color(40, 125, 65));

                completeButton.addActionListener(e -> {
                    int taskId = task.getId();

                    runAction(
                            "正在标记任务 " + taskId + " 为已完成……",
                            () -> agent.completeTask(taskId)
                    );
                });

                card.add(completeButton, BorderLayout.EAST);
            }


            taskPanel.add(card);
            taskPanel.add(Box.createVerticalStrut(5));
        }

        taskPanel.revalidate();
        taskPanel.repaint();
    }

    // 将控制台输出转发到窗口文本区域
    private void redirectOutput() {
        OutputStream outputStream = new OutputStream() {
            @Override
            public void write(int b) {
                appendText(String.valueOf((char) (b & 0xff)));
            }

            @Override
            public void write(byte[] bytes, int offset, int length) {
                String text = new String(
                        bytes, offset, length,
                        StandardCharsets.UTF_8);

                appendText(text);
            }
        };

        System.setOut(new PrintStream(
                outputStream, true, StandardCharsets.UTF_8));
    }

    private void appendText(String text) {
        SwingUtilities.invokeLater(() -> {
            resultArea.append(text);
            resultArea.setCaretPosition(
                    resultArea.getDocument().getLength());
        });
    }

    // 将耗时操作放在后台线程，避免窗口卡住
    private void runAction(String message, IOAction action) {
        for (JButton button : actionButtons) {
            button.setEnabled(false);
        }

        resultArea.setText(message + "\n\n");

        SwingWorker<Void, Void> worker =
                new SwingWorker<>() {

                    @Override
                    protected Void doInBackground() throws Exception {
                        action.run();
                        return null;
                    }

                    @Override
                    protected void done() {
                        for (JButton button : actionButtons) {
                            button.setEnabled(true);
                        }

                        try {
                            get();
                            refreshTaskCards();
                            resultArea.append("\n操作完成。\n");
                        } catch (Exception ex) {
                            Throwable cause = ex.getCause();

                            String error = cause != null
                                    ? cause.getMessage()
                                    : ex.getMessage();

                            resultArea.append(
                                    "\n操作失败：" + error + "\n");

                            JOptionPane.showMessageDialog(
                                    StudyMateGUI.this,
                                    "操作失败，请查看窗口中的错误信息。",
                                    "提示",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                };

        worker.execute();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                StudyMateGUI gui = new StudyMateGUI();
                gui.setVisible(true);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(
                        null,
                        "StudyMate 启动失败：" + e.getMessage(),
                        "启动错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
