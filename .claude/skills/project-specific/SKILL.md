---
name: project-specific
description: Use when working on this project - provides project-specific conventions, Git workflow hooks, and automated PR creation
---

# 项目自动化 Hook

项目在 `.claude/settings.json` 中配置了两个 Git 工作流 Hook，会在对应操作时自动触发。

## Hook 概览

| Hook | 触发时机 | 脚本 | 说明 |
|------|----------|------|------|
| 提交前敏感信息检查 | `git commit` 执行前（PreToolUse） | `.claude/hooks/pre-commit-sensitive-check.sh` | 扫描暂存区 diff，检测到敏感数据则阻断提交 |
| 推送后自动创建 PR | `git push` 成功后（PostToolUse） | `.claude/hooks/post-push-create-pr.sh` | 自动向 master 创建 Pull Request，已存在 PR 则跳过 |

## 提交前敏感信息检查

### 阻断项

| 检查项 | 检测内容 | 阻断条件 |
|--------|----------|----------|
| API 密钥 / Token | `api_key`、`apiKey`、`secret`、`token`、`password`、`access_key`、`private_key` 等硬编码赋值 | 任何明文硬编码的密钥/令牌 |
| 平台 Token 格式 | GitHub（`ghp_`）、OpenAI（`sk-`）、AWS、JWT 等常见 Token 格式 | 任何匹配的 Token 文本 |
| 硬编码密码 | 变量名含 `password`、`passwd`、`pwd`、`pass` 且赋值为非空字符串 | 任何明文密码赋值 |
| 数据库连接串 | `jdbc:`、`mongodb://`、`redis://` 等含认证信息的连接字符串 | 包含用户名密码的连接串 |
| 内网 IP 地址 | `192.168.x.x`、`10.x.x.x`、`172.16-31.x.x` | 警告（不阻断） |
| 私钥 / 证书内容 | `-----BEGIN PRIVATE KEY-----`、`-----BEGIN CERTIFICATE-----` 等 | 任何私钥/证书文本 |
| 敏感文件 | `.pem`、`.p12`、`.pfx`、`.jks`、`.keystore`、`.key`、`.der`、`.cer`、`.crt` | 任何被暂存的敏感文件 |

### 执行流程

```
git commit 执行前：

1. 读取 git diff --cached（暂存区变更）
2. 提取新增/修改行（以 + 开头的行）
3. 逐规则匹配
   - ERROR 级 → 阻断提交，输出问题行
   - WARN 级 → 输出警告，不阻断
4. 全部通过 → 允许提交
   任何 ERROR → 退出码 1，提交被阻止
```

## 推送后自动创建 PR

### 执行规则

```
git push 成功后：

1. 检查 gh CLI 是否可用且已认证
2. 获取当前分支名
   - 是 master/main → 跳过
3. 检查是否已有打开的 PR
   - 已存在 → 跳过，输出已有 PR 链接
4. 判断分支类型
   - feature_* → 标题前缀 feat:，标签 enhancement
   - bugfix_* → 标题前缀 fix:，标签 bug
   - 其他 → 使用最近一次 commit message 作为标题
5. 目标分支优先 master，其次 main
6. PR 描述自动附上当前分支所有 commit 摘要
7. 创建 PR（--web 不在终端打开浏览器）
```

### 注意事项

- 需要 `gh` CLI 已安装并完成 `gh auth login` 认证
- 每次 push 都会尝试创建 PR，但已存在同名 PR 时会跳过
- 直接推送到 master/main 分支不会触发 PR 创建
