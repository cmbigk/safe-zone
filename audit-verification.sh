#!/bin/bash

# 🎯 SonarQube Audit Verification Script
# Quick validation of all audit requirements
# Usage: ./audit-verification.sh

echo "╔══════════════════════════════════════════════════════════════╗"
echo "║   SonarQube Audit Verification Script                       ║"
echo "║   E-Commerce Microservices Project                          ║"
echo "╚══════════════════════════════════════════════════════════════╝"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

PASS=0
FAIL=0
WARN=0

# Function to print test result
print_result() {
    local status=$1
    local message=$2
    
    if [ "$status" == "PASS" ]; then
        echo -e "${GREEN}✅ PASS${NC} - $message"
        ((PASS++))
    elif [ "$status" == "FAIL" ]; then
        echo -e "${RED}❌ FAIL${NC} - $message"
        ((FAIL++))
    elif [ "$status" == "WARN" ]; then
        echo -e "${YELLOW}⚠️  WARN${NC} - $message"
        ((WARN++))
    else
        echo -e "${BLUE}ℹ️  INFO${NC} - $message"
    fi
}

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "1. REQUIREMENT: SonarQube Web Interface Accessible"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check if docker is installed
if command -v docker &> /dev/null; then
    print_result "PASS" "Docker is installed"
else
    print_result "FAIL" "Docker is not installed"
fi

# Check if SonarQube container is running
if docker ps | grep -q sonarqube; then
    print_result "PASS" "SonarQube container is running"
    docker ps | grep sonarqube | awk '{print "   Container: " $NF " | Status: " $5 " " $6 " " $7}'
else
    print_result "FAIL" "SonarQube container is not running"
    echo "   💡 Run: cd deployment && ./start-sonarqube.sh"
fi

# Check if SonarQube database container is running
if docker ps | grep -q sonarqube-db; then
    print_result "PASS" "SonarQube database container is running"
else
    print_result "FAIL" "SonarQube database container is not running"
fi

# Check if SonarQube is accessible
if curl -s -o /dev/null -w "%{http_code}" http://localhost:9000 | grep -q "200\|401\|302"; then
    print_result "PASS" "SonarQube web interface is accessible at http://localhost:9000"
else
    print_result "FAIL" "SonarQube web interface is not accessible"
fi

# Check Docker Compose file exists
if [ -f "deployment/docker-compose.sonarqube.yml" ]; then
    print_result "PASS" "Docker Compose configuration exists"
else
    print_result "FAIL" "Docker Compose configuration not found"
fi

# Check sonar-project.properties files
echo ""
echo "Checking project configurations..."
PROJECTS=("api-gateway" "user-service" "product-service" "media-service" "frontend")
for project in "${PROJECTS[@]}"; do
    if [ -f "$project/sonar-project.properties" ]; then
        print_result "PASS" "$project has sonar-project.properties"
    else
        print_result "WARN" "$project missing sonar-project.properties"
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "2. REQUIREMENT: GitHub Integration with Auto-Trigger"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check GitHub Actions workflows
if [ -f ".github/workflows/sonarqube-java.yml" ]; then
    print_result "PASS" "GitHub Actions workflow for Java services exists"
else
    print_result "FAIL" "GitHub Actions workflow for Java services not found"
fi

if [ -f ".github/workflows/sonarqube-frontend.yml" ]; then
    print_result "PASS" "GitHub Actions workflow for Frontend exists"
else
    print_result "FAIL" "GitHub Actions workflow for Frontend not found"
fi

# Check if workflows have on: push trigger
if grep -q "on:" .github/workflows/sonarqube-java.yml && grep -q "push:" .github/workflows/sonarqube-java.yml; then
    print_result "PASS" "Java workflow configured to trigger on push"
else
    print_result "WARN" "Java workflow may not trigger automatically on push"
fi

if grep -q "on:" .github/workflows/sonarqube-frontend.yml && grep -q "push:" .github/workflows/sonarqube-frontend.yml; then
    print_result "PASS" "Frontend workflow configured to trigger on push"
else
    print_result "WARN" "Frontend workflow may not trigger automatically on push"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "3. REQUIREMENT: Docker-Based SonarQube Configuration"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check Docker volumes
if docker volume ls | grep -q sonarqube_data; then
    print_result "PASS" "SonarQube data volume exists"
else
    print_result "WARN" "SonarQube data volume not found"
fi

if docker volume ls | grep -q postgresql_data; then
    print_result "PASS" "PostgreSQL data volume exists"
else
    print_result "WARN" "PostgreSQL data volume not found"
fi

# Check start/stop scripts
if [ -f "deployment/start-sonarqube.sh" ]; then
    print_result "PASS" "Start script exists"
else
    print_result "FAIL" "Start script not found"
fi

if [ -f "deployment/stop-sonarqube.sh" ]; then
    print_result "PASS" "Stop script exists"
else
    print_result "FAIL" "Stop script not found"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "4. REQUIREMENT: Automated CI/CD Analysis"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check Jenkinsfiles
JENKINSFILES_FOUND=0
for project in "${PROJECTS[@]}"; do
    if [ -f "$project/Jenkinsfile.sonarqube" ]; then
        ((JENKINSFILES_FOUND++))
        
        # Check for SonarQube stage
        if grep -q "SonarQube Analysis" "$project/Jenkinsfile.sonarqube"; then
            print_result "PASS" "$project Jenkinsfile has SonarQube Analysis stage"
        else
            print_result "WARN" "$project Jenkinsfile missing SonarQube Analysis stage"
        fi
        
        # Check for Quality Gate stage
        if grep -q "Quality Gate" "$project/Jenkinsfile.sonarqube"; then
            print_result "PASS" "$project Jenkinsfile has Quality Gate stage"
        else
            print_result "WARN" "$project Jenkinsfile missing Quality Gate stage"
        fi
    else
        print_result "WARN" "$project Jenkinsfile.sonarqube not found"
    fi
done

if [ $JENKINSFILES_FOUND -gt 0 ]; then
    echo "   📊 Found $JENKINSFILES_FOUND Jenkinsfiles with SonarQube integration"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "5. REQUIREMENT: Code Review and Approval Process"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check PR template
if [ -f ".github/PULL_REQUEST_TEMPLATE.md" ]; then
    print_result "PASS" "Pull Request template exists"
    
    # Check for SonarQube section in PR template
    if grep -q "SonarQube" .github/PULL_REQUEST_TEMPLATE.md; then
        print_result "PASS" "PR template includes SonarQube section"
    else
        print_result "WARN" "PR template may not include SonarQube section"
    fi
else
    print_result "FAIL" "Pull Request template not found"
fi

# Check CODEOWNERS
if [ -f ".github/CODEOWNERS" ]; then
    print_result "PASS" "CODEOWNERS file exists"
else
    print_result "FAIL" "CODEOWNERS file not found"
fi

# Check code review checklist
if [ -f "docs/CODE_REVIEW_CHECKLIST.md" ]; then
    print_result "PASS" "Code Review Checklist exists"
else
    print_result "FAIL" "Code Review Checklist not found"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "6. SECURITY: Permissions and Access Controls"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check security documentation
if [ -f "docs/SECURITY_PERMISSIONS_GUIDE.md" ]; then
    print_result "PASS" "Security & Permissions Guide exists"
else
    print_result "FAIL" "Security & Permissions Guide not found"
fi

# Check if anonymous access is disabled (requires SonarQube to be running)
if curl -s http://localhost:9000/api/authentication/validate | grep -q "valid"; then
    print_result "INFO" "SonarQube authentication endpoint is accessible"
else
    print_result "INFO" "Cannot verify authentication settings (SonarQube may not be running)"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "7. CODE QUALITY: Rules and Analysis Reports"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check for build artifacts (evidence of analysis)
BUILD_ARTIFACTS=0
for project in "${PROJECTS[@]}"; do
    if [ -d "$project/target/classes" ] || [ -d "$project/dist" ]; then
        ((BUILD_ARTIFACTS++))
        print_result "PASS" "$project has been built (artifacts found)"
    else
        print_result "INFO" "$project has not been built yet"
    fi
    
    # Check for SonarQube cache
    if [ -d "$project/target/sonar" ] || [ -d "$project/.scannerwork" ]; then
        print_result "PASS" "$project has SonarQube analysis cache"
    fi
    
    # Check for test reports
    if [ -d "$project/target/surefire-reports" ] || [ -d "$project/test-results" ]; then
        print_result "PASS" "$project has test reports"
    fi
    
    # Check for coverage reports
    if [ -d "$project/target/site/jacoco" ] || [ -d "$project/coverage" ]; then
        print_result "PASS" "$project has code coverage reports"
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "8. CODE QUALITY: Improvements Committed"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check for git repository
if [ -d ".git" ]; then
    print_result "PASS" "Project is a Git repository"
    
    # Check for recent commits
    COMMIT_COUNT=$(git log --oneline | wc -l | tr -d ' ')
    if [ $COMMIT_COUNT -gt 0 ]; then
        print_result "PASS" "Repository has $COMMIT_COUNT commits"
        
        # Check for quality-related commits
        QUALITY_COMMITS=$(git log --all --grep="fix:\|refactor:\|test:\|SonarQube" --oneline 2>/dev/null | wc -l | tr -d ' ')
        if [ $QUALITY_COMMITS -gt 0 ]; then
            print_result "PASS" "Found $QUALITY_COMMITS quality improvement commits"
        else
            print_result "INFO" "No specific quality improvement commits found (check git log)"
        fi
    else
        print_result "INFO" "Repository has no commits yet"
    fi
else
    print_result "WARN" "Not a Git repository or .git directory not found"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "9. BONUS: Notifications (Slack/Email)"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Check bonus features documentation
if [ -f "docs/BONUS_FEATURES_GUIDE.md" ]; then
    print_result "PASS" "Bonus Features Guide exists"
    
    # Check for Slack documentation
    if grep -q -i "slack" docs/BONUS_FEATURES_GUIDE.md; then
        print_result "PASS" "Slack integration documented"
    else
        print_result "WARN" "Slack integration not documented"
    fi
    
    # Check for Email documentation
    if grep -q -i "email\|smtp" docs/BONUS_FEATURES_GUIDE.md; then
        print_result "PASS" "Email notification documented"
    else
        print_result "WARN" "Email notification not documented"
    fi
    
    # Check for IDE integration documentation
    if grep -q -i "SonarLint\|IDE" docs/BONUS_FEATURES_GUIDE.md; then
        print_result "PASS" "IDE integration documented"
    else
        print_result "WARN" "IDE integration not documented"
    fi
else
    print_result "FAIL" "Bonus Features Guide not found"
fi

# Check if Jenkinsfiles have notification code
SLACK_FOUND=0
for project in "${PROJECTS[@]}"; do
    if [ -f "$project/Jenkinsfile.sonarqube" ]; then
        if grep -q "slackSend\|emailext" "$project/Jenkinsfile.sonarqube"; then
            ((SLACK_FOUND++))
        fi
    fi
done

if [ $SLACK_FOUND -gt 0 ]; then
    print_result "PASS" "Found notification code in $SLACK_FOUND Jenkinsfile(s)"
else
    print_result "INFO" "No notification code found in Jenkinsfiles (check documentation)"
fi

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "10. DOCUMENTATION"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

DOCS=(
    "SONARQUBE_README.md"
    "docs/SONARQUBE_INTEGRATION_GUIDE.md"
    "docs/AUDIT_COMPLIANCE_CHECKLIST.md"
    "docs/SECURITY_PERMISSIONS_GUIDE.md"
    "docs/CODE_REVIEW_CHECKLIST.md"
    "docs/BONUS_FEATURES_GUIDE.md"
    "AUDIT_COMPLIANCE_REPORT.md"
)

for doc in "${DOCS[@]}"; do
    if [ -f "$doc" ]; then
        print_result "PASS" "$(basename $doc) exists"
    else
        print_result "WARN" "$(basename $doc) not found"
    fi
done

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "📊 SUMMARY"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo -e "${GREEN}✅ PASSED:${NC} $PASS"
echo -e "${RED}❌ FAILED:${NC} $FAIL"
echo -e "${YELLOW}⚠️  WARNINGS:${NC} $WARN"
echo ""

TOTAL=$((PASS + FAIL + WARN))
if [ $TOTAL -gt 0 ]; then
    PASS_RATE=$((PASS * 100 / TOTAL))
    echo "Pass Rate: $PASS_RATE%"
fi

echo ""
if [ $FAIL -eq 0 ]; then
    echo -e "${GREEN}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${GREEN}║  ✅ AUDIT READY - All critical checks passed!               ║${NC}"
    echo -e "${GREEN}╚══════════════════════════════════════════════════════════════╝${NC}"
else
    echo -e "${RED}╔══════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${RED}║  ❌ ATTENTION REQUIRED - Some checks failed                  ║${NC}"
    echo -e "${RED}╚══════════════════════════════════════════════════════════════╝${NC}"
fi

echo ""
echo "📖 For detailed audit compliance information, see:"
echo "   - AUDIT_COMPLIANCE_REPORT.md"
echo "   - docs/AUDIT_COMPLIANCE_CHECKLIST.md"
echo ""
echo "🚀 Next Steps:"
echo "   1. If SonarQube is not running: cd deployment && ./start-sonarqube.sh"
echo "   2. Access SonarQube: open http://localhost:9000"
echo "   3. Review AUDIT_COMPLIANCE_REPORT.md for complete documentation"
echo ""
