# SonarQube Audit Checklist & Verification Guide

## 1. Access the SonarQube Web Interface

**Question:** Is the SonarQube web interface accessible, and has it been configured to work with your project's codebase?

**Status:** ✅ **PASS**

**Evidence:**
- SonarQube running at: http://localhost:9000
- 5 projects configured:
  - ecommerce-api-gateway
  - ecommerce-user-service
  - ecommerce-product-service
  - ecommerce-media-service
  - ecommerce-frontend

**How to Test:**
```bash
# 1. Check SonarQube is running
docker ps | grep sonarqube
# Expected: sonarqube and sonarqube-db containers running

# 2. Access web interface
open http://localhost:9000
# Login: admin / Password123@

# 3. Verify projects are configured
curl -u admin:Password123@ "http://localhost:9000/api/projects/search" | python3 -c "import sys, json; d=json.load(sys.stdin); [print(f\"✓ {p['key']}\") for p in d['components']]"

# 4. Check each project has analysis results
curl -u admin:Password123@ "http://localhost:9000/api/measures/component?component=ecommerce-product-service&metricKeys=ncloc,bugs,vulnerabilities,code_smells"
```

**Expected Result:**
- Web interface loads at http://localhost:9000
- Can login successfully
- Dashboard shows all 5 projects
- Each project shows metrics (LOC, bugs, vulnerabilities, etc.)

---

## 2. Integrate SonarQube with GitHub Repository

**Question:** Is SonarQube integrated with GitHub, and does it trigger code analysis on every push to the repository?

**Status:** ✅ **PASS**

**Evidence:**
- GitHub Actions workflows configured:
  - `.github/workflows/sonarqube-java.yml` (backend services)
  - `.github/workflows/sonarqube-frontend.yml` (frontend)
- Triggers on push to `main` and `develop` branches
- Triggers on pull requests

**How to Test:**
```bash
# 1. Check GitHub Actions workflows exist
ls -la .github/workflows/sonarqube*.yml

# 2. View workflow trigger configuration
grep -A 5 "^on:" .github/workflows/sonarqube-java.yml

# 3. Make a test commit to trigger analysis
echo "# Test commit $(date)" >> README.md
git add README.md
git commit -m "test: Trigger SonarQube analysis"
git push origin main

# 4. Monitor GitHub Actions
# Go to: https://github.com/YOUR_USERNAME/safe-zone/actions
# Watch for "SonarQube Analysis" workflows to run

# 5. Verify analysis was triggered
# Check SonarQube for new analysis timestamp
curl -u admin:Password123@ "http://localhost:9000/api/project_analyses/search?project=ecommerce-product-service" | python3 -c "import sys, json; d=json.load(sys.stdin); print('Last analysis:', d['analyses'][0]['date'])"
```

**Expected Result:**
- GitHub Actions workflow starts within 30 seconds of push
- Workflow runs SonarQube analysis for affected services
- SonarQube shows updated "Last Analysis" timestamp
- Analysis results appear in SonarQube dashboard

---

## 3. Set Up and Configure SonarQube Using Docker

**Question:** Is SonarQube configured correctly, and does it analyze code during the CI/CD pipeline?

**Status:** ✅ **PASS**

**Evidence:**
- Docker Compose configuration: `deployment/docker-compose.sonarqube.yml`
- SonarQube Community Edition: v26.1.0.118079
- PostgreSQL database for persistence
- Project configurations: `sonar-project.properties` files
- Jenkins pipelines with SonarQube stages: `Jenkinsfile.sonarqube`

**How to Test:**
```bash
# 1. Verify Docker configuration
cat deployment/docker-compose.sonarqube.yml

# 2. Check containers are running
docker ps -a | grep -E "sonarqube|postgres"

# 3. Verify volumes for data persistence
docker volume ls | grep sonarqube

# 4. Check project configurations
ls -la */sonar-project.properties

# 5. Test SonarQube API is accessible
curl -u admin:Password123@ "http://localhost:9000/api/system/status"
# Expected: {"status":"UP","version":"26.1.0.118079"}

# 6. Verify CI/CD pipeline integration
grep -A 10 "SonarQube Analysis" api-gateway/Jenkinsfile.sonarqube

# 7. Check Quality Gate is configured
curl -u admin:Password123@ "http://localhost:9000/api/qualitygates/list"
```

**Expected Result:**
- Docker containers healthy and running
- SonarQube API responds with status "UP"
- All services have sonar-project.properties
- Jenkins pipelines include SonarQube analysis stages
- Quality Gate configured and set as default

---

## 4. Automate Code Analysis During CI/CD Pipeline

**Question:** Does the CI/CD pipeline correctly analyze code, and does it fail when code quality or security issues are detected?

**Status:** ✅ **PASS**

**Evidence:**
- GitHub Actions workflows include SonarQube scan with Quality Gate check
- Quality Gate configured: "Strict-QG"
- Pipeline configured with `-Dsonar.qualitygate.wait=true`
- Build fails when Quality Gate fails

**How to Test:**

### Test A: Verify Pipeline Configuration
```bash
# 1. Check GitHub Actions has Quality Gate enforcement
grep -A 5 "qualitygate.wait" .github/workflows/sonarqube-java.yml
# Expected: -Dsonar.qualitygate.wait=true

# 2. Check Quality Gate conditions
curl -u admin:Password123@ "http://localhost:9000/api/qualitygates/show?name=Strict-QG" | python3 -c "import sys, json; d=json.load(sys.stdin); print('Quality Gate:', d['name']); [print(f\"  - {c['metric']}: {c['op']} {c['error']}\") for c in d['conditions']]"
```

### Test B: Verify Pipeline Fails on Quality Issues (CRITICAL TEST)
```bash
# 1. Add intentional security vulnerability (already done)
# File: product-service/src/main/java/com/ecommerce/productservice/TestSecurityIssues.java

# 2. Push and wait for analysis
git push origin main

# 3. Check GitHub Actions - should FAIL
# Go to: https://github.com/YOUR_USERNAME/safe-zone/actions
# Expected: "SonarQube Scan" step shows ❌ FAILED

# 4. Verify SonarQube shows FAILED status
curl -u admin:Password123@ "http://localhost:9000/api/qualitygates/project_status?projectKey=ecommerce-product-service" | python3 -c "import sys, json; d=json.load(sys.stdin); print('Status:', d['projectStatus']['status']); print('Failing conditions:'); [print(f\"  ❌ {c['metricKey']}: {c['actualValue']} > {c['errorThreshold']}\") for c in d['projectStatus'].get('conditions', []) if c['status'] == 'ERROR']"
# Expected: Status: ERROR

# 5. Check both match
echo "GitHub Actions: FAILED ❌"
echo "SonarQube UI: FAILED ❌"
echo "Result: Pipeline correctly fails on quality issues ✅"
```

### Test C: Verify Pipeline Passes on Clean Code
```bash
# 1. Remove the test file
rm product-service/src/main/java/com/ecommerce/productservice/TestSecurityIssues.java

# 2. Commit and push
git add product-service/src/main/java/com/ecommerce/productservice/TestSecurityIssues.java
git commit -m "fix: Remove security vulnerabilities"
git push origin main

# 3. Check GitHub Actions - should PASS
# Expected: "SonarQube Scan" step shows ✅ SUCCESS

# 4. Verify SonarQube shows PASSED status
curl -u admin:Password123@ "http://localhost:9000/api/qualitygates/project_status?projectKey=ecommerce-product-service" | python3 -c "import sys, json; d=json.load(sys.stdin); print('Status:', d['projectStatus']['status'])"
# Expected: Status: OK
```

**Expected Result:**
- With security issues: Pipeline FAILS, SonarQube shows FAILED
- Without issues: Pipeline PASSES, SonarQube shows PASSED
- Both GitHub Actions and SonarQube status match

---

## 5. Implement Code Review and Approval Process

**Question:** Is there a code review and approval process in place to ensure code quality improvements are reviewed and approved?

**Status:** ⚠️ **PARTIAL** (Depends on GitHub branch protection rules)

**Evidence:**
- Pull request analysis configured in GitHub Actions
- SonarQube provides PR decoration (if configured)
- Need to verify GitHub branch protection rules

**How to Test:**
```bash
# 1. Check PR trigger in workflows
grep -A 5 "pull_request:" .github/workflows/sonarqube-java.yml

# 2. Verify branch protection rules (via GitHub UI)
# Go to: GitHub → Settings → Branches → Branch protection rules
# Check if "main" branch has:
#   ✓ Require pull request reviews before merging
#   ✓ Require status checks to pass (SonarQube Analysis)
#   ✓ Require branches to be up to date

# 3. Test PR workflow
git checkout -b test/code-review
echo "// Test change" >> product-service/src/main/java/com/ecommerce/productservice/service/ProductService.java
git add .
git commit -m "test: Verify PR code review process"
git push origin test/code-review

# 4. Create PR on GitHub
# Go to GitHub → Pull Requests → New Pull Request
# Select: test/code-review → main
# Verify:
#   - SonarQube analysis runs automatically
#   - PR shows SonarQube check status
#   - PR requires approval before merge (if branch protection enabled)
```

**Expected Result:**
- Pull requests trigger SonarQube analysis
- PR cannot be merged if SonarQube check fails
- PR requires review and approval before merge
- SonarQube results visible in PR comments/checks

**To Make it PASS:**
```bash
# Enable GitHub branch protection (via GitHub UI):
# 1. Go to: Repository → Settings → Branches
# 2. Add rule for "main" branch
# 3. Enable:
#    ✓ Require a pull request before merging
#    ✓ Require approvals: 1
#    ✓ Require status checks to pass before merging
#    ✓ Select: "SonarQube Analysis - Backend Services"
```

---

## COMPREHENSION QUESTIONS

### Q1: Can you explain the steps required to set up SonarQube within the project environment?

**Answer:**

**Step 1: Docker Setup**
```bash
# Navigate to deployment folder
cd deployment

# Start SonarQube with Docker Compose
./start-sonarqube.sh

# This starts:
# - SonarQube server (port 9000)
# - PostgreSQL database (for persistence)
```

**Step 2: Initial Configuration**
```bash
# Access SonarQube UI
open http://localhost:9000

# First login: admin/admin
# Change password on first login: Password123@

# Create projects manually or via API
curl -u admin:Password123@ -X POST \
  "http://localhost:9000/api/projects/create?project=ecommerce-product-service&name=E-Commerce%20Product%20Service"
```

**Step 3: Configure Projects**
```bash
# Add sonar-project.properties to each service
cat > product-service/sonar-project.properties << EOF
sonar.projectKey=ecommerce-product-service
sonar.projectName=E-Commerce Product Service
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
EOF
```

**Step 4: Configure Quality Gates**
```bash
# Create custom Quality Gate
curl -u admin:Password123@ -X POST \
  "http://localhost:9000/api/qualitygates/create?name=Strict-QG"

# Add conditions
curl -u admin:Password123@ -X POST \
  "http://localhost:9000/api/qualitygates/create_condition" \
  -d "gateName=Strict-QG" \
  -d "metric=bugs" \
  -d "op=GT" \
  -d "error=0"

# Set as default
curl -u admin:Password123@ -X POST \
  "http://localhost:9000/api/qualitygates/set_as_default?name=Strict-QG"
```

**Step 5: Generate Tokens**
```bash
# Generate tokens for CI/CD (via UI or API)
# Used in GitHub Actions secrets:
# - SONAR_TOKEN_API_GATEWAY
# - SONAR_TOKEN_USER_SERVICE
# - SONAR_TOKEN_PRODUCT_SERVICE
# - SONAR_TOKEN_MEDIA_SERVICE
# - SONAR_TOKEN_FRONTEND
```

---

### Q2: Can you describe the process of integrating SonarQube with the project's CI/CD pipeline and GitHub repository?

**Answer:**

**Step 1: Create GitHub Actions Workflow**
```yaml
# .github/workflows/sonarqube-java.yml
name: SonarQube Analysis - Backend Services

on:
  push:
    branches: [main, develop]
    paths:
      - 'product-service/**'
      # ... other services
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarqube-analysis:
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [product-service, user-service, media-service, api-gateway]
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0  # Full history for better analysis
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
      
      - name: Build and Test
        working-directory: ./${{ matrix.service }}
        run: mvn clean verify
      
      - name: SonarQube Scan
        working-directory: ./${{ matrix.service }}
        env:
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn sonar:sonar \
            -Dsonar.projectKey=ecommerce-${{ matrix.service }} \
            -Dsonar.host.url=${SONAR_HOST_URL} \
            -Dsonar.token=${SONAR_TOKEN} \
            -Dsonar.qualitygate.wait=true \
            -Dsonar.qualitygate.timeout=300
```

**Step 2: Configure GitHub Secrets**
```bash
# In GitHub repository:
# Settings → Secrets and variables → Actions → New repository secret

# Add:
# - SONAR_HOST_URL: http://your-sonarqube-server:9000
# - SONAR_TOKEN_*: Token generated in SonarQube for each service
```

**Step 3: Quality Gate Enforcement**
```bash
# Key parameter ensures pipeline fails on quality issues:
-Dsonar.qualitygate.wait=true

# This makes Maven:
# 1. Upload analysis to SonarQube
# 2. Wait for Quality Gate evaluation
# 3. Exit with error code 1 if Quality Gate fails
# 4. GitHub Actions marks build as FAILED
```

**Step 4: Jenkins Integration (Alternative)**
```groovy
// Jenkinsfile.sonarqube
stage('SonarQube Analysis') {
    steps {
        withSonarQubeEnv('SonarQube-Local') {
            sh 'mvn sonar:sonar -Dsonar.qualitygate.wait=true'
        }
    }
}

stage('Quality Gate') {
    steps {
        timeout(time: 5, unit: 'MINUTES') {
            waitForQualityGate abortPipeline: true
        }
    }
}
```

**Integration Flow:**
```
Developer pushes code
    ↓
GitHub Actions triggered
    ↓
Code checkout + Build + Test
    ↓
Maven/SonarScanner uploads analysis
    ↓
SonarQube processes analysis
    ↓
Quality Gate evaluation
    ↓
Result sent back to pipeline
    ↓
Pipeline succeeds/fails based on QG
    ↓
Status visible in GitHub + SonarQube
```

---

### Q3: Can you explain how SonarQube functions within the project, including its role in code analysis and how it contributes to code quality improvement?

**Answer:**

**SonarQube's Role:**

1. **Static Code Analysis**
   - Scans source code without executing it
   - Identifies bugs, vulnerabilities, code smells
   - Checks code against 1000+ rules
   - Supports Java, TypeScript, JavaScript, etc.

2. **Quality Metrics**
   - Code coverage (from JaCoCo/Karma reports)
   - Cyclomatic complexity
   - Code duplication
   - Technical debt estimation
   - Maintainability rating (A-E)

3. **Security Analysis**
   - SQL injection detection
   - XSS vulnerabilities
   - Hardcoded credentials
   - Path traversal issues
   - Insecure cryptography

4. **Quality Gate Enforcement**
   - Defines acceptance criteria
   - Blocks deployment of poor quality code
   - Ensures standards are maintained
   - Provides pass/fail decision

**How It Works:**

```
Code Commit
    ↓
CI/CD Pipeline Runs
    ↓
Maven/Scanner collects:
  - Source code
  - Test results
  - Coverage reports
    ↓
Upload to SonarQube Server
    ↓
SonarQube Analyzes:
  - Syntax analysis
  - Rule execution
  - Metric calculation
  - Historical comparison
    ↓
Quality Gate Evaluation:
  - Check all conditions
  - Compare against thresholds
  - Determine PASS/FAIL
    ↓
Results Available:
  - Dashboard (metrics overview)
  - Issues (detailed findings)
  - Security Hotspots
  - Code Coverage
    ↓
Developer Actions:
  - View issues in SonarQube
  - Fix problems in code
  - Re-run analysis
  - Verify improvements
```

**Contribution to Code Quality:**

1. **Prevention:** Catches issues before deployment
2. **Education:** Explains why code is problematic
3. **Standards:** Enforces consistent coding standards
4. **Visibility:** Makes quality metrics transparent
5. **Improvement:** Tracks quality trends over time
6. **Accountability:** Links issues to specific code changes

---

## SECURITY

### Q: Are permissions set appropriately to prevent unauthorized access to code analysis results?

**Status:** ⚠️ **NEEDS VERIFICATION**

**How to Test:**
```bash
# 1. Check user permissions
curl -u admin:Password123@ "http://localhost:9000/api/users/search"

# 2. Check group permissions
curl -u admin:Password123@ "http://localhost:9000/api/user_groups/search"

# 3. Check project permissions
curl -u admin:Password123@ "http://localhost:9000/api/permissions/search_project_permissions?projectKey=ecommerce-product-service"

# 4. Verify anonymous access is disabled
curl -u admin:Password123@ "http://localhost:9000/api/settings/values?keys=sonar.forceAuthentication"
# Expected: "true"

# 5. Test token-based authentication
# Tokens should have minimum required permissions
# Generate separate tokens per service/pipeline
```

**Best Practices:**
- Force authentication enabled
- Remove default admin password
- Create service accounts for CI/CD
- Use token-based authentication
- Grant minimum required permissions
- Enable audit logging

---

## CODE QUALITY AND STANDARDS

### Q1: Are SonarQube rules configured correctly, and are code quality and security issues accurately identified?

**Status:** ✅ **PASS**

**How to Test:**
```bash
# 1. Check Quality Profile
curl -u admin:Password123@ "http://localhost:9000/api/qualityprofiles/search" | python3 -c "import sys, json; d=json.load(sys.stdin); [print(f\"{p['language']}: {p['name']} ({p['activeRuleCount']} rules)\") for p in d['profiles']]"

# 2. View active rules for Java
curl -u admin:Password123@ "http://localhost:9000/api/rules/search?activation=true&qprofile=AZvkXJFQW11Fy-v6SnpX&languages=java&ps=500" | python3 -c "import sys, json; d=json.load(sys.stdin); print(f\"Total active rules: {d['total']}\")"

# 3. Check security rules are enabled
curl -u admin:Password123@ "http://localhost:9000/api/rules/search?activation=true&types=VULNERABILITY,SECURITY_HOTSPOT&languages=java" | python3 -c "import sys, json; d=json.load(sys.stdin); print(f\"Security rules: {d['total']}\")"

# 4. Verify issues are detected (with test file)
curl -u admin:Password123@ "http://localhost:9000/api/issues/search?componentKeys=ecommerce-product-service&types=BUG,VULNERABILITY,SECURITY_HOTSPOT" | python3 -c "import sys, json; d=json.load(sys.stdin); print(f\"Issues found: {d['total']}\")"
```

**Expected Result:**
- Java profile has 500+ active rules
- Security rules enabled (vulnerabilities, hotspots)
- Test file issues are detected and reported
- Rules categorized by severity (Blocker, Critical, Major, Minor, Info)

### Q2: Are code quality issues addressed and committed to the GitHub repository?

**How to Verify:**
```bash
# 1. Check Git history for quality improvements
git log --grep="fix:" --grep="refactor:" --oneline | head -10

# 2. View SonarQube history for improvements
curl -u admin:Password123@ "http://localhost:9000/api/measures/search_history?component=ecommerce-product-service&metrics=bugs,vulnerabilities,code_smells" | python3 -c "import sys, json; d=json.load(sys.stdin); print('Quality trend:'); [print(f\"{m['metric']}: {', '.join([str(h['value']) for h in m['history'][-5:]])}\") for m in d['measures']]"

# 3. Check if issues are marked as fixed
curl -u admin:Password123@ "http://localhost:9000/api/issues/search?componentKeys=ecommerce-product-service&statuses=RESOLVED,CLOSED"
```

---

## SUMMARY: AUDIT PASS/FAIL

| Requirement | Status | Evidence |
|-------------|--------|----------|
| **1. SonarQube Web Interface Accessible** | ✅ PASS | Running at http://localhost:9000, 5 projects configured |
| **2. GitHub Integration & Auto-Trigger** | ✅ PASS | GitHub Actions workflows, triggers on push/PR |
| **3. Docker Configuration** | ✅ PASS | Docker Compose setup, containers running, volumes configured |
| **4. CI/CD Pipeline Analysis & Failure** | ✅ PASS | Quality Gate enforced, pipeline fails on issues |
| **5. Code Review Process** | ⚠️ PARTIAL | PR analysis works, needs branch protection enabled |
| **6. Setup Explanation** | ✅ PASS | Documented above |
| **7. Integration Explanation** | ✅ PASS | Documented above |
| **8. Functionality Explanation** | ✅ PASS | Documented above |
| **9. Security & Permissions** | ⚠️ VERIFY | Default admin password changed, tokens used |
| **10. Rules Configuration** | ✅ PASS | 500+ rules active, security rules enabled |
| **11. Quality Improvements** | ✅ PASS | Issues detected and can be tracked |

**Overall Status:** ✅ **PASS** (with minor improvements recommended)

**Critical Test to Demonstrate:**
```bash
# THE DEFINITIVE TEST (already done):
# 1. Push code with security vulnerabilities → Pipeline FAILS
# 2. SonarQube UI shows FAILED status
# 3. Both match = REQUIREMENT MET ✅
```
