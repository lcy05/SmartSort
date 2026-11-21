# 贡献指南

## 开发环境

| 项目 | 版本 |
| --- | --- |
| JDK | 17 |
| Android SDK Platform | 34 |
| Gradle | 8.9（使用仓库自带 wrapper，不要求本机安装） |

首次构建前配置 SDK 路径，二者择一：

- 在仓库根目录创建 `local.properties`，写入 `sdk.dir=/path/to/android-sdk`；
- 或导出环境变量 `ANDROID_HOME`。

`local.properties` 已列入 `.gitignore`，请勿提交。

## 构建与验证

```bash
./gradlew :app:testDebugUnitTest :app:assembleDebug     # 日常验证
./gradlew :app:testDebugUnitTest :app:assembleRelease   # 确认 R8 与资源裁剪后仍可运行
```

以上命令均在仓库根目录执行。提交前需保证单元测试与两项构建均通过。改动涉及网络、协议或设置持久化时，请在真机上完成一次完整流程验证：连接 → 开盖 → 倒计时 → 保持打开 → 全开 → 全关 → 断开重连。

发布签名密钥通过 `SMARTSORT_*` 环境变量注入，严禁写入源码或提交 keystore 文件。

## 代码风格

- Kotlin 官方风格，4 空格缩进，单行不超过 120 列；
- UI 全部使用 Jetpack Compose，不引入 XML 布局；
- 状态集中在 `MainViewModel`，组件保持无状态并只接收数据与回调；
- 协议字符串不得手写，统一经 `BinProtocol` 构造；
- 新增文案使用简体中文，与现有术语保持一致（「开盖中」「已关闭」「可投放」「已满溢」）。

格式细节见 `.editorconfig`。

## 协议兼容性约束

设备固件按固定偏移解析 JSON，以下规则不可协商：

1. 冒号后必须恰好一个空格，例如 `{"mode": 0}`；
2. 连接建立后必须立即下发 `{"mode": 0}`；
3. 桶位键名固定为 `gaik` / `gaiy` / `gaiq`；
4. 不得改变报文字段语义，不得引入固件不识别的必填字段。

任何涉及协议的改动都需在说明中给出真机验证结果。

## 提交信息

采用 Conventional Commits：

```text
<type>(<scope>): <subject>

<body 可选>
```

| type | 用途 |
| --- | --- |
| `feat` | 新功能 |
| `fix` | 缺陷修复 |
| `refactor` | 不改变外部行为的重构 |
| `perf` | 性能或体积优化 |
| `docs` | 文档 |
| `build` | 构建配置 |
| `chore` | 其他杂项 |

示例：`fix(protocol): 修正全开指令缺失 mode 字段`

## 分支与合并

- 从 `main` 切出功能分支，命名为 `feat/xxx`、`fix/xxx`；
- 一个分支对应一件事，避免混入格式化等无关改动；
- 合并前需通过构建，并在 PR 描述中说明改动范围与验证方式。

## 提交 PR 前的检查清单

- [ ] `assembleDebug` 与 `assembleRelease` 均构建成功
- [ ] 未提交 `local.properties`、`build/`、`.gradle/`、签名文件
- [ ] 涉及协议改动时，已按第 3 节核对报文格式并在真机验证
- [ ] 新增界面文案与术语一致，未出现客户端推断为确定事实的表述
- [ ] 体积变化超过 50 KB 时，在 PR 中说明原因

## 问题反馈

提交 issue 时请包含：

- 手机型号与 Android 版本；
- 设备固件版本或提交号；
- 复现步骤与期望结果；
- 相关日志（`adb logcat`）与界面截图。
