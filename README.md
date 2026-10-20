# StudyMate — AI 学习规划智能体

StudyMate 是一个基于 Java 开发的 AI 学习规划应用，旨在帮助用户根据学习目标生成个性化学习计划，并通过图形化界面管理每日学习任务、记录完成状态和查看学习进度。

项目使用 Java Swing 构建桌面界面，结合 DeepSeek API 实现 AI 学习计划生成与调整，并使用 Maven 管理项目依赖。

## ✨ 项目功能

- **AI 学习计划生成**：根据学习目标、计划天数和每日学习时长生成学习任务。
- **任务管理**：查看学习任务，并根据任务 ID 标记完成状态。
- **进度查看**：查看学习计划的完成情况。
- **AI 重新规划**：根据当前计划调整尚未完成的学习任务。
- **图形化界面**：通过 Java Swing 界面进行交互。
- **学习数据持久化**：保存学习计划与任务状态，便于后续继续使用。

## 🛠️ 技术栈

| 技术 | 用途 |
|---|---|
| Java | 核心业务逻辑 |
| Java Swing | 桌面图形用户界面 |
| Maven | 项目构建与依赖管理 |
| DeepSeek API | AI 学习计划生成与调整 |
| Java HttpClient | HTTP API 请求 |
| Jackson | JSON 数据处理 |

## 📁 项目结构

```text
StudyMate/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── main/
                └── java/
                    ├── Main.java
                    ├── StudyMateGUI.java
                    ├── StudyAgent.java
                    ├── AIPlanner.java
                    ├── StudyPlan.java
                    ├── StudyTask.java
                    ├── StudyTools.java
                    └── FileStorage.java
```

## ⚙️ 环境要求

- JDK 17 或兼容的更高版本
- Maven（或 IntelliJ IDEA 内置 Maven）
- 可访问 DeepSeek API 的网络环境
- 有效的 DeepSeek API Key

## 🚀 运行方法

### 1. 克隆项目

```bash
git clone https://github.com/X-flyaway/StudyMate.git
cd StudyMate
```

### 2. 配置 API Key

先在 DeepSeek 官方平台获取 API Key，然后将它配置为环境变量 `DEEPSEEK_API_KEY`。

Windows PowerShell 示例：

```powershell
$env:DEEPSEEK_API_KEY="你的_API_Key"
```

该命令仅对当前 PowerShell 会话生效。也可以在 IntelliJ IDEA 的 Run/Debug Configurations 中，为运行配置添加环境变量：

```text
DEEPSEEK_API_KEY=你的_API_Key
```

**安全提醒：请勿将真实 API Key 写入源代码、README 或上传到 GitHub。**

### 3. 使用 IntelliJ IDEA 运行

1. 使用 IntelliJ IDEA 打开项目根目录。
2. 等待 Maven 加载依赖。
3. 确认 JDK 设置正确，并配置 `DEEPSEEK_API_KEY`。
4. 根据 `Main.java` 中的启动逻辑运行程序。

## 📌 项目特点

- 将 AI 能力与学习任务管理结合。
- 通过智能体组织计划生成、任务执行和计划调整流程。
- 采用模块化设计，将界面、计划管理、AI 请求和数据存储分离。

## 🔮 后续改进方向

- 增加学习日历与任务筛选功能。
- 支持历史学习计划查询。
- 优化学习统计和可视化图表。
- 增强 API 异常处理和离线状态提示。
- 完善单元测试与自动化测试。

## 👩‍💻 项目信息

- **项目名称：** StudyMate
- **项目类型：** Java 课程设计 / AI 学习规划智能体
- **代码仓库：** https://github.com/X-flyaway/StudyMate

---

本项目用于学习 Java、桌面应用开发、API 调用和智能体应用设计。
