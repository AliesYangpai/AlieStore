# CLAUDE.md

本文件为 Claude Code (claude.ai/code) 在此仓库中工作时提供指导。

## 构建 / 运行

```bash
# 构建项目
./gradlew assembleDebug

# 使用指定 JDK 构建（在 gradle.properties 中配置）
./gradlew assembleDebug

# 运行 lint 检查
./gradlew lint

# 运行所有检查（lint + 编译）
./gradlew check
```

项目中尚未创建单元测试和仪器化测试 —— `app/src/test/` 和 `app/src/androidTest/` 目录不存在。

## 分支管理规范

所有代码改动必须在从 `master` 拉出的功能分支上进行，禁止直接在 `master` 分支上修改代码。

**分支命名规范：**

- **需求开发**：`feature_xxx`，例如 `feature_login`、`feature_download_manager`
- **Bug 修复**：`bugfix_xxx`，例如 `bugfix_download_crash`、`bugfix_null_pointer`

**工作流程：**

```bash
# 1. 确保本地 master 最新
git checkout master
git pull origin master

# 2. 基于 master 创建功能/修复分支
git checkout -b feature_xxx   # 需求
git checkout -b bugfix_xxx    # bug 修复

# 3. 在分支上进行开发...

# 4. 提交并推送到远端
git add .
git commit -m "描述改动内容"
git push origin feature_xxx   # 或 bugfix_xxx

# 5. 合并回 master
git checkout master
git merge feature_xxx   # 或 bugfix_xxx
git push origin master
```

```

## JDK 17 + KAPT JVM 参数

本项目使用 JDK 17（`gradle.properties` 中的 `org.gradle.java.home`）并配合 KAPT（用于 Hilt 注解处理）。JDK 17+ 封装了 KAPT 需要的内部 Javac API。如果出现 `IllegalAccessError: cannot access class com.sun.tools.javac.main.JavaCompiler` 错误，请在 `gradle.properties` 中添加以下 JVM 参数：

```
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8 \
  --add-opens jdk.compiler/com.sun.tools.javac.main=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.processing=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.comp=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.tree=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED \
  --add-opens jdk.compiler/com.sun.tools.javac.parser=ALL-UNNAMED
```

## 架构

**AlieStore** 是一个 Android 应用商店客户端。采用 **MVVM** 架构，使用 **Hilt** 依赖注入、**Retrofit** 网络请求、**Kotlin Flow** 响应式数据流。

### 数据分层（自上而下）

```
UI（Activity/ViewModel）
  ↓  Flow<UiState<T>>，在 lifecycleScope 中通过 repeatOnLifecycle 消费
Repository
  ↓  封装 DataSource，将 SourceData 转换为 UiState，错误通过 catch 处理
DataSource（接口：AppInfoDataSourceWork）
  ↓  实现类调用 Retrofit API
API（Retrofit 接口：AppInfoNetApi）
  ↓  向后台发起 HTTP 请求
```

### 依赖注入配置

- `NetApiProvider`（`di/`）：`@Provides` — 创建 `OkHttpClient` → `Retrofit` → `AppInfoNetApi`（均为 `@Singleton`）
- `DataSourceWorkBind`（`di/`）：`@Binds` — 将 `AppInfoDataSource`（实现类）绑定到 `AppInfoDataSourceWork`（接口）
- `AlieStoreApp` 继承 `Application`，使用 `@HiltAndroidApp` 注解
- `MainViewModel` 使用 `@HiltViewModel` 注解，构造器使用 `@Inject`
- `MainActivity` 使用 `@AndroidEntryPoint` 注解

### 通用数据模型层级（`data/GlobalDataModel.kt`）

| 层级 | 类型 | 用途 |
|-------|------|---------|
| API 响应 | `NetRspData<T>`（继承 `ApiData<T>`） | `code`、`msg`、`data: T?` —— 原始网络响应包装 |
| DataSource → Repo | `SourceData<T>` | `ret: Boolean`、`msg`、`apiData: T?` —— 成功/失败标记，供 Repo 使用 |
| ViewModel → UI | `UiState<T>`（继承 `UiViewState`） | `isSuccess`、`msg`、`data: T?`，外加 `isVisible`、`isEnable` |

### 关键包结构

| 包 | 职责 |
|---------|------|
| `ui/` | `MainActivity`（XML + ViewBinding）、`MainViewModel`（StateFlow） |
| `ui/data/` | `TipUiState` —— 主界面的具体 UI 状态 |
| `repo/` | `AppInfoRepository` —— 转换数据源结果，处理带进度的 APK 下载 |
| `source/` | `AppInfoDataSourceWork` 接口、`AppInfoDataSource` 实现 |
| `api/` | `AppInfoNetApi` —— Retrofit 接口，包含 `@GET`、`@Streaming`、`@Header Range`（断点续传） |
| `di/` | Hilt 模块 |
| `data/` | 领域模型：`AppInfo`、`DownloadInfo`、`RspAppInfo`，以及通用数据包装类 |
| `constant/` | `ConstNet` —— 基础 URL 和 API 路径常量 |
| `test/` | Flow 操作符（`combine` 等）的练习/学习代码 —— 未在主应用中使用 |

### APK 下载（两种模式）

1. **完整下载**（`downloadApk`）：通过 `ResponseBody.byteStream()` 流式下载整个文件，持续发送 `SourceData<DownloadInfo>` 进度更新
2. **断点续传**（`downloadApkInRange`）：使用 HTTP `Range` 请求头（`bytes=$startPos-`），通过 `RandomAccessFile` 追加写入已有文件

### ViewBinding

已在 `app/build.gradle` 中启用（`viewBinding { enabled = true }`）。布局 XML 文件位于 `app/src/main/res/layout/`。`activity_main.xml` 对应的绑定类为 `ActivityMainBinding`。
