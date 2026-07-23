#!/bin/bash
# =============================================================================
# post-push 自动创建 PR 脚本
# 在 git push 成功后执行，自动从当前分支向 master 创建 Pull Request。
# 如果当前分支就是 master 或没有远端对应分支，则跳过。
# =============================================================================

set -euo pipefail

# --- 颜色输出 ---
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

# --- 检查 gh CLI 是否可用 ---
if ! command -v gh &> /dev/null; then
    echo -e "${RED}❌ gh CLI 未安装或不可用，无法自动创建 PR。${NC}"
    echo -e "${YELLOW}   安装方法: brew install gh && gh auth login${NC}"
    exit 1
fi

# --- 检查 gh 认证状态 ---
if ! gh auth status &> /dev/null; then
    echo -e "${RED}❌ gh CLI 未认证，请先执行 gh auth login。${NC}"
    exit 1
fi

# --- 获取当前分支名 ---
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || true)

if [ -z "$CURRENT_BRANCH" ]; then
    echo -e "${RED}❌ 无法获取当前分支名（可能处于 detached HEAD 状态）。${NC}"
    exit 1
fi

# --- 如果是 master 分支，跳过 ---
if [ "$CURRENT_BRANCH" = "master" ] || [ "$CURRENT_BRANCH" = "main" ]; then
    echo -e "${YELLOW}⚠️  当前分支为 '$CURRENT_BRANCH'（主干分支），跳过 PR 创建。${NC}"
    exit 0
fi

# --- 获取最近一次 commit message 作为 PR 标题 ---
LAST_COMMIT_MSG=$(git log -1 --pretty=format:"%s" 2>/dev/null || echo "Merge $CURRENT_BRANCH")

# --- 检查是否已经存在同名 PR ---
EXISTING_PR=$(gh pr list --head "$CURRENT_BRANCH" --state open --json number --jq '.[0].number' 2>/dev/null || true)

if [ -n "$EXISTING_PR" ]; then
    echo -e "${YELLOW}⚠️  分支 '$CURRENT_BRANCH' 已有打开的 PR (#${EXISTING_PR})，跳过创建。${NC}"
    echo -e "${CYAN}   PR 链接: https://github.com/AliesYangpai/AlieStore/pull/${EXISTING_PR}${NC}"
    exit 0
fi

# --- 判断分支类型，生成 PR 标题前缀 ---
if [[ "$CURRENT_BRANCH" == feature_* ]]; then
    PR_TITLE="feat: ${LAST_COMMIT_MSG}"
    PR_LABEL="enhancement"
elif [[ "$CURRENT_BRANCH" == bugfix_* ]]; then
    PR_TITLE="fix: ${LAST_COMMIT_MSG}"
    PR_LABEL="bug"
else
    PR_TITLE="${LAST_COMMIT_MSG}"
    PR_LABEL=""
fi

# --- 确定目标分支（优先 master，其次 main） ---
TARGET_BRANCH="master"
if ! git show-ref --verify --quiet "refs/remotes/origin/master"; then
    if git show-ref --verify --quiet "refs/remotes/origin/main"; then
        TARGET_BRANCH="main"
    fi
fi

echo ""
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${CYAN}📋 准备创建 Pull Request${NC}"
echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "  源分支:   ${GREEN}${CURRENT_BRANCH}${NC}"
echo -e "  目标分支: ${GREEN}${TARGET_BRANCH}${NC}"
echo -e "  PR 标题:  ${CYAN}${PR_TITLE}${NC}"
echo ""

# --- 创建 PR ---
if [ -n "$PR_LABEL" ]; then
    PR_URL=$(gh pr create \
        --base "$TARGET_BRANCH" \
        --head "$CURRENT_BRANCH" \
        --title "$PR_TITLE" \
        --body "## 变更说明

$(git log "${TARGET_BRANCH}..${CURRENT_BRANCH}" --pretty=format:'- %s' 2>/dev/null || echo "- ${LAST_COMMIT_MSG}")

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)" \
        --label "$PR_LABEL" \
        --web 2>&1)
else
    PR_URL=$(gh pr create \
        --base "$TARGET_BRANCH" \
        --head "$CURRENT_BRANCH" \
        --title "$PR_TITLE" \
        --body "## 变更说明

$(git log "${TARGET_BRANCH}..${CURRENT_BRANCH}" --pretty=format:'- %s' 2>/dev/null || echo "- ${LAST_COMMIT_MSG}")

---
🤖 Generated with [Claude Code](https://claude.com/claude-code)" \
        --web 2>&1)
fi

# --- 输出结果 ---
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN}╔═══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✅ Pull Request 已创建！                                     ║${NC}"
    echo -e "${GREEN}╚═══════════════════════════════════════════════════════════════╝${NC}"
    echo -e "${CYAN}   ${PR_URL}${NC}"
    echo ""
else
    echo -e "${RED}❌ PR 创建失败: ${PR_URL}${NC}"
    exit 1
fi
