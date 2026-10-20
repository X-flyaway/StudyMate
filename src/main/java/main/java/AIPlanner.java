package main.java;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Comparator;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AIPlanner {

    private static final String API_URL =
            "https://api.deepseek.com/chat/completions";

    private static final String MODEL = "deepseek-flash";

    private final ObjectMapper mapper;
    private final HttpClient client;

    public AIPlanner() {
        this.mapper = new ObjectMapper();

        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
    }

    /**
     * 调用 DeepSeek 生成学习计划。
     */
    public StudyPlan createPlan(
            String goal,
            int days,
            int minutesPerDay) throws IOException {

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

        String apiKey = System.getenv("DEEPSEEK_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "未读取到 DEEPSEEK_API_KEY，请检查 IDEA 运行配置。");
        }

        System.out.println("正在请求 DeepSeek，为你制定学习计划...");

        // 1. 构造 API 请求
        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.7);
        requestBody.put("max_tokens", 4096);

        // 要求模型返回 JSON
        ObjectNode responseFormat = requestBody.putObject("response_format");
        responseFormat.put("type", "json_object");

        ArrayNode messages = requestBody.putArray("messages");

        ObjectNode systemMessage = messages.addObject();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                "你是专业的学习规划助手。"
                        + "请根据用户的学习目标制定具体、循序渐进、可执行的学习安排。"
                        + "必须只返回合法 JSON，不要使用 Markdown 代码块。"
                        + "JSON 格式必须为："
                        + "{\"tasks\":[{\"day\":1,\"title\":\"任务标题\"}]}"
                        + "tasks 数组必须包含用户要求的准确天数。"
                        + "day 从 1 开始，依次递增到指定天数。"
                        + "title 必须是具体的学习任务，不能为空。");

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");
        userMessage.put("content",
                "请为我制定一份学习计划。\n"
                        + "学习目标：" + goal + "\n"
                        + "计划天数：" + days + " 天\n"
                        + "每日可学习时间：" + minutesPerDay + " 分钟\n"
                        + "请将目标拆解成每天可以完成的学习任务。"
                        + "考虑循序渐进，并安排适当的练习或复习。"
                        + "必须恰好生成 " + days + " 个任务。");

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        // 2. 发送请求
        HttpResponse<String> response;

        try {
            response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("DeepSeek 请求被中断。", e);
        }

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IOException(
                    "DeepSeek API 请求失败，HTTP 状态码："
                            + response.statusCode()
                            + "\n返回信息：" + response.body());
        }

        // 3. 解析 API 响应，提取 AI 生成的 JSON 文本
        JsonNode root = mapper.readTree(response.body());
        JsonNode contentNode =
                root.at("/choices/0/message/content");

        if (contentNode.isMissingNode()
                || contentNode.isNull()
                || !contentNode.isTextual()) {
            throw new IOException(
                    "DeepSeek 响应中没有找到有效的计划内容。");
        }

        String aiContent = contentNode.asText();

        JsonNode planJson;

        try {
            planJson = mapper.readTree(aiContent);
        } catch (Exception e) {
            throw new IOException(
                    "DeepSeek 返回的内容不是合法 JSON。", e);
        }

        JsonNode tasksNode = planJson.get("tasks");

        if (tasksNode == null || !tasksNode.isArray()) {
            throw new IOException(
                    "AI 返回的数据缺少 tasks 数组。");
        }

        if (tasksNode.size() != days) {
            throw new IOException(
                    "AI 返回了 " + tasksNode.size()
                            + " 个任务，但要求 " + days
                            + " 个。请重新创建计划。");
        }

        // 4. 先校验所有任务，再创建 StudyPlan
        String[] titles = new String[days];

        for (int i = 0; i < days; i++) {
            JsonNode taskNode = tasksNode.get(i);

            JsonNode titleNode = taskNode.get("title");

            if (titleNode == null || !titleNode.isTextual()
                    || titleNode.asText().isBlank()) {
                throw new IOException(
                        "第 " + (i + 1) + " 天的任务标题无效。");
            }

            titles[i] = titleNode.asText().trim();
        }

        // 5. 将 AI 任务转换成现有的 StudyTask 对象
        StudyPlan plan = new StudyPlan(goal);
        LocalDate startDate = LocalDate.now();

        for (int i = 0; i < days; i++) {
            StudyTask task = new StudyTask(
                    plan.getNextTaskId(),
                    startDate.plusDays(i),
                    titles[i],
                    minutesPerDay
            );

            plan.addTask(task);
        }

        System.out.println("DeepSeek 已成功生成学习计划！");
        return plan;
    }

    /**
     * 调用 DeepSeek 重新规划未完成的学习任务。
     * 只生成新任务数据，不修改传入的原始任务。
     */
    public List<StudyTask> replanTasks(
            String goal,
            List<StudyTask> unfinishedTasks) throws IOException {

        if (unfinishedTasks == null || unfinishedTasks.isEmpty()) {
            return new ArrayList<>();
        }

        String apiKey = System.getenv("DEEPSEEK_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "未读取到 DEEPSEEK_API_KEY，请检查 IDEA 运行配置。");
        }

        LocalDate today = LocalDate.now();

        ObjectNode requestBody = mapper.createObjectNode();
        requestBody.put("model", MODEL);
        requestBody.put("stream", false);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 4096);
        requestBody.putObject("response_format")
                .put("type", "json_object");

        ArrayNode messages = requestBody.putArray("messages");

        messages.addObject()
                .put("role", "system")
                .put("content",
                        "你是专业的 AI 学习规划助手。"
                                + "请根据学习目标重新规划未完成的学习任务。"
                                + "只返回合法 JSON，不要使用 Markdown。"
                                + "格式为："
                                + "{\"tasks\":[{\"id\":1,"
                                + "\"title\":\"学习任务\","
                                + "\"minutes\":60}]}"
                                + "必须返回与输入完全相同的任务数量。"
                                + "每个任务必须保留原来的 id，不得重复或遗漏。"
                                + "title 必须具体、可执行且不能为空。"
                                + "minutes 必须是 1 到 720 之间的整数。"
                                + "请合理安排学习顺序，包含必要的练习与复习。");

        ObjectNode userMessage = messages.addObject();
        userMessage.put("role", "user");

        ObjectNode input = mapper.createObjectNode();
        input.put("goal", goal);
        input.put("today", today.toString());

        ArrayNode inputTasks = input.putArray("unfinishedTasks");

        for (StudyTask task : unfinishedTasks) {
            ObjectNode item = inputTasks.addObject();
            item.put("id", task.getId());
            item.put("oldTitle", task.getTitle());
            item.put("oldMinutes", task.getMinutes());
            item.put("oldDate", task.getDate().toString());
        }

        userMessage.put("content",
                "请重新安排以下未完成任务。"
                        + "从今天开始安排，每天最多安排一项任务。"
                        + "保持学习目标不变，可以优化任务标题、顺序和时长。"
                        + "不要生成输入中不存在的新任务编号。"
                        + "\n输入数据："
                        + input.toString());

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(120))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        System.out.println("正在请求 DeepSeek，重新规划未完成任务...");

        HttpResponse<String> response;

        try {
            response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("DeepSeek 重规划请求被中断。", e);
        }

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IOException(
                    "DeepSeek 重规划失败，HTTP 状态码："
                            + response.statusCode()
                            + "\n返回信息：" + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        JsonNode contentNode = root.at("/choices/0/message/content");

        if (!contentNode.isTextual()) {
            throw new IOException("DeepSeek 没有返回有效的重规划内容。");
        }

        JsonNode resultJson;

        try {
            resultJson = mapper.readTree(contentNode.asText());
        } catch (Exception e) {
            throw new IOException("AI 重规划结果不是合法 JSON。", e);
        }

        JsonNode tasksNode = resultJson.get("tasks");

        if (tasksNode == null || !tasksNode.isArray()
                || tasksNode.size() != unfinishedTasks.size()) {
            throw new IOException(
                    "AI 返回的任务数量不正确，原计划尚未修改。");
        }

        Set<Integer> expectedIds = new HashSet<>();
        for (StudyTask task : unfinishedTasks) {
            expectedIds.add(task.getId());
        }

        Set<Integer> returnedIds = new HashSet<>();
        List<StudyTask> replacements = new ArrayList<>();

        for (int i = 0; i < tasksNode.size(); i++) {
            JsonNode item = tasksNode.get(i);
            JsonNode idNode = item.get("id");
            JsonNode titleNode = item.get("title");
            JsonNode minutesNode = item.get("minutes");

            if (idNode == null || !idNode.canConvertToInt()
                    || titleNode == null || !titleNode.isTextual()
                    || titleNode.asText().isBlank()
                    || minutesNode == null
                    || !minutesNode.canConvertToInt()) {
                throw new IOException(
                        "AI 返回的第 " + (i + 1)
                                + " 项任务格式无效，原计划尚未修改。");
            }

            int id = idNode.asInt();
            int minutes = minutesNode.asInt();
            String title = titleNode.asText().trim();

            if (!expectedIds.contains(id)
                    || !returnedIds.add(id)) {
                throw new IOException(
                        "AI 返回了错误或重复的任务编号，原计划尚未修改。");
            }

            if (minutes < 1 || minutes > 720) {
                throw new IOException(
                        "AI 返回的学习时长超出范围，原计划尚未修改。");
            }

            replacements.add(new StudyTask(
                    id,
                    today.plusDays(i),
                    title,
                    minutes
            ));
        }

        if (!returnedIds.equals(expectedIds)) {
            throw new IOException(
                    "AI 遗漏了部分任务编号，原计划尚未修改。");
        }

        // 按原任务列表顺序返回，避免 AI 返回顺序影响任务对应关系。
        replacements.sort(Comparator.comparingInt(StudyTask::getId));

        System.out.println("DeepSeek 已生成新的学习安排。");
        return replacements;
    }

}