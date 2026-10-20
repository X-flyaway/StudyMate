# 📚 StudyMate — AI 学习规划智能体

**让学习计划更清晰，让每一步进步都看得见。**

StudyMate 是一个基于 Java Swing 与 DeepSeek API 开发的 AI 学习规划桌面应用。用户可以输入学习目标、学习周期和每日可用时间，由 AI 辅助生成个性化学习计划，并通过任务管理与进度追踪持续推进学习。

<p align="center">
  <img src="https://img.shields.io/badge/Java-17%2B-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java" />
  <img src="https://img.shields.io/badge/UI-Java%20Swing-blue?style=for-the-badge" alt="Java Swing" />
  <img src="https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Maven" />
  <img src="https://img.shields.io/badge/AI-DeepSeek-5B6CFF?style=for-the-badge" alt="DeepSeek" />
</p>

---

## ✨ 项目亮点

| 功能 | 说明 |
|:--|:--|
| 🤖 **AI 学习规划** | 根据学习目标、天数和每日学习时长生成学习计划 |
| 📝 **任务管理** | 查看学习任务，并根据任务 ID 更新完成状态 |
| 📊 **进度可视化** | 使用进度条和任务状态展示学习进展 |
| 🔄 **AI 计划调整** | 根据学习情况辅助重新规划任务 |
| 🎨 **桌面交互界面** | 使用 Java Swing 构建桌面界面，支持任务卡片与状态颜色 |
| 💾 **学习数据保存** | 保存学习计划，方便后续继续使用 |

## 🖥️ 界面展示

StudyMate 采用桌面应用形式，将 AI 规划、任务列表和进度反馈整合在同一界面中。

> 📸 **项目截图即将补充**
>
> <img width="1580" height="1131" alt="image" src="https://github.com/user-attachments/assets/591b1b3b-7a6f-4cd0-afd8-5bc7f78cdbbe" />


## 🛠️ 技术栈

- **开发语言：** Java
- **图形界面：** Java Swing
- **项目构建：** Apache Maven
- **AI 服务：** DeepSeek API
- **网络请求：** Java HttpClient
- **JSON 处理：** Jackson Databind
- **开发环境：** IntelliJ IDEA

## 📂 项目结构

```text
StudyMate/
├── pom.xml
├── README.md
├── .gitignore
└── src/
    └── main/
        └── java/
            └── main/
                ├── Main.java
                ├── StudyMateGUI.java
                ├── StudyAgent.java
                ├── AIPlanner.java
                ├── StudyPlan.java
                ├── StudyTask.java
                ├── StudyTools.java
                └── FileStorage.java
```

| 文件 | 主要职责 |
|:--|:--|
| `Main.java` | 程序启动入口 |
| `StudyMateGUI.java` | Swing 图形界面与用户交互 |
| `StudyAgent.java` | 学习规划智能体的协调逻辑 |
| `AIPlanner.java` | AI 学习计划生成与调整 |
| `StudyPlan.java` | 学习计划的数据模型 |
| `StudyTask.java` | 学习任务的数据模型 |
| `StudyTools.java` | 学习任务相关操作 |
| `FileStorage.java` | 学习数据的保存与读取 |

## 🚀 快速开始

### 1. 环境准备

运行项目之前，请准备：

- JDK 17 或更高版本
- IntelliJ IDEA
- Maven
- 可访问的 DeepSeek API 服务及有效 API Key

> 请以 `pom.xml` 中的 Java 编译配置为准。

### 2. 克隆项目

```bash
git clone https://github.com/X-flyaway/StudyMate.git
cd StudyMate
```

### 3. 配置 API Key

项目需要通过环境变量 `DEEPSEEK_API_KEY` 读取 API 密钥。

在 IntelliJ IDEA 中：

1. 打开 **Run → Edit Configurations**。
2. 选择运行 `Main.java` 的配置。
3. 找到 **Environment variables**。
4. 添加以下变量：

```text
DEEPSEEK_API_KEY=你的真实API密钥
```

5. 保存配置。

⚠️ **安全提醒：** 请勿将真实 API Key 写入 Java 源码、README 或提交到 GitHub。API Key 应只保存在本地运行配置或安全的环境变量中。

### 4. 运行程序

在 IntelliJ IDEA 中找到：

```text
src/main/java/main/java/Main.java
```

打开文件，点击运行按钮即可启动应用。

如果依赖尚未下载完成，可以先在 IDEA 的 Maven 面板中执行 `Reload All Maven Projects`。

## 🎯 项目目标

StudyMate 希望将生成式 AI 与日常学习管理结合起来，探索 AI 在个性化学习规划中的应用。

通过本项目，实践了：

- Java 面向对象程序设计
- Swing 图形界面开发
- Maven 项目依赖管理
- HTTP API 调用与 JSON 数据处理
- 智能体业务逻辑设计
- 学习任务状态管理与进度可视化
- Git 与 GitHub 版本控制

## 🔮 后续改进方向

- [ ] 增加学习数据统计与历史记录
- [ ] 优化学习计划编辑与任务调整体验
- [ ] 增加每日学习提醒
- [ ] 改善异常处理与 API 请求反馈
- [ ] 增加更丰富的学习进度图表
- [ ] 持续优化界面布局与视觉效果

## 👩‍💻 项目信息

- **项目名称：** StudyMate
- **项目类型：** AI 学习规划桌面应用
- **开发语言：** Java
- **代码仓库：** [X-flyaway/StudyMate](https://github.com/X-flyaway/StudyMate)

---

<p align="center">
  <strong>StudyMate · Plan smarter. Learn better.</strong>
  <br />
  <sub>让每一个学习目标，都有清晰的前进方向。</sub>
</p>
