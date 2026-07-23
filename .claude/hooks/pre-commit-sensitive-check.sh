#!/bin/bash
# =============================================================================
# pre-commit 敏感信息检查脚本
# 在 git commit 之前执行，扫描暂存区中的敏感数据。
# 如果检测到敏感信息，阻止提交并输出问题详情。
# =============================================================================

set -euo pipefail

# --- 颜色输出 ---
RED='\033[0;31m'
YELLOW='\033[1;33m'
GREEN='\033[0;32m'
NC='\033[0m' # No Color

# --- 检查标记 ---
FOUND_SENSITIVE=false
VIOLATIONS=()

# --- 获取暂存区 diff（新增/修改的内容） ---
STAGED_DIFF=$(git diff --cached --unified=0 2>/dev/null || true)

if [ -z "$STAGED_DIFF" ]; then
    echo -e "${GREEN}✅ 暂存区无变更，跳过敏感信息检查。${NC}"
    exit 0
fi

# --- 提取新增/修改的行（以 + 开头，排除 +++ 文件头） ---
ADDED_LINES=$(echo "$STAGED_DIFF" | grep '^+' | grep -v '^+++' || true)

if [ -z "$ADDED_LINES" ]; then
    echo -e "${GREEN}✅ 无新增行，跳过敏感信息检查。${NC}"
    exit 0
fi

# =============================================================================
# 检查规则定义（按优先级排列）
# =============================================================================

check_pattern() {
    local pattern="$1"
    local description="$2"
    local severity="$3"  # ERROR: 阻断, WARN: 警告

    local matches=$(echo "$ADDED_LINES" | grep -E "$pattern" || true)

    if [ -n "$matches" ]; then
        FOUND_SENSITIVE=true
        echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${RED}[${severity}] ${description}${NC}"
        echo -e "${YELLOW}匹配行:${NC}"
        echo "$matches" | while IFS= read -r line; do
            echo -e "  ${RED}→${NC} $(echo "$line" | head -c 200)"
        done
        echo ""
    fi
}

echo ""
echo -e "${YELLOW}🔍 正在检查暂存区敏感信息...${NC}"
echo ""

# --- 规则 1: API Key / Token / Secret 硬编码 ---
check_pattern \
    '(api[_-]?key|apiKey|secret[_-]?key|secretKey|access[_-]?key|accessKey|private[_-]?key|privateKey|auth[_-]?token|authToken|bearer[_-]?token|bearerToken)\s*[:=]\s*['\''"][A-Za-z0-9_\-\.]{16,}['\''"]' \
    "检测到可能硬编码的 API 密钥 / Token / Secret" \
    "ERROR"

# --- 规则 2: 常见平台 Token 格式 ---
check_pattern \
    '(ghp_|github_pat_|gho_|ghu_|ghs_|ghr_|gpt_|sk-|sk-ant-|AIza|ya29\.|eyJ[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+\.[A-Za-z0-9_\-]+)' \
    "检测到常见平台 Token 格式（GitHub/AWS/GCP/OpenAI/JWT 等）" \
    "ERROR"

# --- 规则 3: 明文密码赋值 ---
check_pattern \
    '(password|passwd|pwd|pass)\s*[:=]\s*['\''"][^'\''"\s]{1,}['\''"]' \
    "检测到可能硬编码的密码" \
    "ERROR"

# --- 规则 4: 数据库连接串（含认证信息） ---
check_pattern \
    '(jdbc:[a-z]+://[^:]+:[^@]+@|mongodb://[^:]+:[^@]+@|redis://[^:]+:[^@]+@|mysql://[^:]+:[^@]+@|postgres(ql)?://[^:]+:[^@]+@)' \
    "检测到包含认证信息的数据库连接串" \
    "ERROR"

# --- 规则 5: 内网 IP 地址（非 localhost） ---
check_pattern \
    '(192\.168\.\d{1,3}\.\d{1,3}|10\.\d{1,3}\.\d{1,3}\.\d{1,3}|172\.(1[6-9]|2[0-9]|3[01])\.\d{1,3}\.\d{1,3})' \
    "检测到内网 IP 地址，请确认是否应暴露在代码中" \
    "WARN"

# --- 规则 6: 私钥文件内容标记 ---
check_pattern \
    '(-----BEGIN (RSA |EC |DSA |OPENSSH )?PRIVATE KEY-----|-----BEGIN CERTIFICATE-----)' \
    "检测到私钥或证书内容" \
    "ERROR"

# --- 规则 7: .pem/.p12/.jks/.keystore 文件 ---
STAGED_FILES=$(git diff --cached --name-only 2>/dev/null || true)
if [ -n "$STAGED_FILES" ]; then
    SENSITIVE_FILES=$(echo "$STAGED_FILES" | grep -E '\.(pem|p12|pfx|jks|keystore|key|der|cer|crt)$' 2>/dev/null || true)
    if [ -n "$SENSITIVE_FILES" ]; then
        FOUND_SENSITIVE=true
        echo -e "${RED}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
        echo -e "${RED}[ERROR] 检测到私钥/证书文件被暂存:${NC}"
        echo "$SENSITIVE_FILES" | while IFS= read -r line; do
            echo -e "  ${RED}→${NC} $line"
        done
        echo ""
    fi
fi

# =============================================================================
# 结果判定
# =============================================================================

echo -e "${YELLOW}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"

if [ "$FOUND_SENSITIVE" = true ]; then
    echo ""
    echo -e "${RED}╔═══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  ❌ 敏感信息检查未通过！                                     ║${NC}"
    echo -e "${RED}║  请移除上述敏感数据后重新提交。                             ║${NC}"
    echo -e "${RED}║                                                               ║${NC}"
    echo -e "${RED}║  敏感数据应存放在:                                           ║${NC}"
    echo -e "${RED}║  · local.properties（已 gitignore）                         ║${NC}"
    echo -e "${RED}║  · 环境变量                                                   ║${NC}"
    echo -e "${RED}║  · Gradle 构建脚本动态读取                                    ║${NC}"
    echo -e "${RED}╚═══════════════════════════════════════════════════════════════╝${NC}"
    echo ""
    exit 1
else
    echo -e "${GREEN}✅ 敏感信息检查通过，允许提交。${NC}"
    echo ""
    exit 0
fi
