# SonarQube Integration Guide for E-Commerce Microservices
## Comprehensive Setup for CI/CD Pipeline with Jenkins & GitHub

**Author**: DevOps Team  
**Date**: January 21, 2026  
**Purpose**: Audit-Ready SonarQube Integration

---

## Table of Contents
1. [SonarQube Docker Setup](#1-sonarqube-docker-setup)
2. [Initial Configuration](#2-initial-configuration)
3. [Project Setup for Each Service](#3-project-setup-for-each-service)
4. [GitHub Integration](#4-github-integration)
5. [Jenkins Pipeline Integration](#5-jenkins-pipeline-integration)
6. [Quality Gates Configuration](#6-quality-gates-configuration)
7. [Security & Permissions](#7-security--permissions)
8. [Code Review & Approval Process](#8-code-review--approval-process)
9. [Fixing Code Quality Issues](#9-fixing-code-quality-issues)
10. [Bonus Features](#10-bonus-features)
11. [Audit Compliance Checklist](#11-audit-compliance-checklist)

---

## 1. SonarQube Docker Setup

### 1.1 Start SonarQube

```bash
cd deployment
./start-sonarqube.sh
```

### 1.2 Verify Web UI Access

**Audit Point**: ✅ *SonarQube web interface is accessible locally*

1. Wait 1-2 minutes for SonarQube to start
2. Open browser: http://localhost:9000
3. Login with default credentials:
   - Username: `admin`
   - Password: `admin`
4. **IMPORTANT**: Change password immediately when prompted

**What Auditors Look For**:
- Screenshot of SonarQube login page
- Evidence of password change from default
- Accessibility via localhost:9000

### 1.3 Verify Database Connection

```bash
# Check SonarQube logs
docker logs sonarqube

# Look for this message:
# "SonarQube is operational"
```

### 1.4 System Health Check

Navigate to: **Administration** → **System** → **System Info**

Verify:
- ✅ Database connection: PostgreSQL
- ✅ Compute Engine: Running
- ✅ Elasticsearch: Green status
- ✅ Web Server: Running

---

## 2. Initial Configuration

### 2.1 Change Admin Password

**Audit Point**: ✅ *Security - Prevent unauthorized access*

1. Login as admin
2. Click profile icon → **My Account**
3. Go to **Security** tab
4. Change password to a strong password
5. **Document**: Save new password in secure password manager

### 2.2 Configure General Settings

Navigate to: **Administration** → **Configuration** → **General Settings**

#### Server Base URL
- Set to: `http://localhost:9000`
- This is crucial for GitHub webhooks

#### Email Configuration (Optional)
```properties
Email prefix: [SONARQUBE]
From address: sonarqube@yourdomain.com
From name: SonarQube
SMTP host: smtp.gmail.com
SMTP port: 587
SMTP username: your-email@gmail.com
Secure connection: STARTTLS
```

### 2.3 Install Required Plugins

Navigate to: **Administration** → **Marketplace**

Install these plugins:
1. **SonarJava** (for Java microservices) - Usually pre-installed
2. **SonarJS** (for Angular frontend) - Usually pre-installed
3. **SonarHTML** (for HTML analysis)
4. **SonarCSS** (for SCSS analysis)
5. **GitHub Authentication** (optional but recommended)

After installation, **restart SonarQube**:
```bash
docker restart sonarqube
```

---

## 3. Project Setup for Each Service

### 3.1 Create Projects

**Audit Point**: ✅ *SonarQube is configured for the project*

For each microservice, create a project:

#### Projects to Create:
1. `ecommerce-api-gateway`
2. `ecommerce-user-service`
3. `ecommerce-product-service`
4. `ecommerce-media-service`
5. `ecommerce-frontend`

#### Steps for Each Project:

1. Click **Create Project** (+ icon)
2. Select **Manually**
3. Enter Project Key: `ecommerce-api-gateway`
4. Display Name: `E-Commerce API Gateway`
5. Click **Set Up**
6. Choose **Locally** (for now)
7. Generate Token:
   - Token Name: `api-gateway-token`
   - Type: **Project Analysis Token**
   - Expires: 90 days (or No expiration for learning)
   - Click **Generate**
   - **IMPORTANT**: Copy and save token immediately!
8. Select build tool:
   - For Java services: **Maven**
   - For frontend: **Other**

### 3.2 Save All Tokens Securely

Create a secure file (DO NOT commit to Git):

```bash
# Create .sonarqube-tokens file in deployment directory
cat > deployment/.sonarqube-tokens << 'EOF'
# SonarQube Project Tokens - KEEP SECURE!
# Generated: 2026-01-21

SONAR_HOST_URL=http://localhost:9000

# API Gateway
SONAR_TOKEN_API_GATEWAY=squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# User Service
SONAR_TOKEN_USER_SERVICE=squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Product Service
SONAR_TOKEN_PRODUCT_SERVICE=squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Media Service
SONAR_TOKEN_MEDIA_SERVICE=squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx

# Frontend
SONAR_TOKEN_FRONTEND=squ_xxxxxxxxxxxxxxxxxxxxxxxxxxxxx
EOF
```

Add to `.gitignore`:
```bash
echo ".sonarqube-tokens" >> .gitignore
```

### 3.3 Configure Project-Specific Settings

For **Java Projects** (API Gateway, User/Product/Media Services):

Navigate to: Project → **Administration** → **General Settings**

1. **Source File Encoding**: UTF-8
2. **Source Directories**: `src/main/java`
3. **Test Directories**: `src/test/java`
4. **Binary Directories**: `target/classes`

For **Frontend Project**:

1. **Source File Encoding**: UTF-8
2. **Source Directories**: `src`
3. **Exclusions**: 
   ```
   **/node_modules/**
   **/dist/**
   **/test-results/**
   **/*.spec.ts
   ```

---

## 4. GitHub Integration

**Audit Point**: ✅ *SonarQube is integrated with GitHub and triggers analysis on every push*

### 4.1 Method 1: GitHub Actions (Recommended)

Create `.github/workflows/sonarqube-analysis.yml`:

```yaml
name: SonarQube Analysis

on:
  push:
    branches:
      - main
      - develop
  pull_request:
    types: [opened, synchronize, reopened]

jobs:
  sonarqube-java:
    name: Java Services Analysis
    runs-on: ubuntu-latest
    strategy:
      matrix:
        service: [api-gateway, user-service, product-service, media-service]
    
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Full history for better analysis
      
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache SonarQube packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
          restore-keys: ${{ runner.os }}-sonar
      
      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2
      
      - name: Build and analyze
        working-directory: ./${{ matrix.service }}
        env:
          SONAR_TOKEN: ${{ secrets[format('SONAR_TOKEN_{0}', matrix.service)] }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
        run: |
          mvn clean verify sonar:sonar \
            -Dsonar.projectKey=ecommerce-${{ matrix.service }} \
            -Dsonar.host.url=$SONAR_HOST_URL \
            -Dsonar.token=$SONAR_TOKEN

  sonarqube-frontend:
    name: Frontend Analysis
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0
      
      - name: Set up Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        working-directory: ./frontend
        run: npm ci
      
      - name: Run tests with coverage
        working-directory: ./frontend
        run: npm run test:coverage
      
      - name: SonarQube Scan
        uses: sonarsource/sonarqube-scan-action@master
        with:
          projectBaseDir: ./frontend
          args: >
            -Dsonar.projectKey=ecommerce-frontend
            -Dsonar.sources=src
            -Dsonar.tests=src
            -Dsonar.test.inclusions=**/*.spec.ts
            -Dsonar.typescript.lcov.reportPaths=coverage/lcov.info
            -Dsonar.exclusions=**/node_modules/**,**/dist/**
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN_FRONTEND }}
          SONAR_HOST_URL: ${{ secrets.SONAR_HOST_URL }}
```

### 4.2 Configure GitHub Secrets

**In your GitHub repository**:

1. Go to **Settings** → **Secrets and variables** → **Actions**
2. Add these secrets:
   - `SONAR_HOST_URL`: `http://your-public-ip:9000` (or use ngrok for testing)
   - `SONAR_TOKEN_API_GATEWAY`: (paste token)
   - `SONAR_TOKEN_USER_SERVICE`: (paste token)
   - `SONAR_TOKEN_PRODUCT_SERVICE`: (paste token)
   - `SONAR_TOKEN_MEDIA_SERVICE`: (paste token)
   - `SONAR_TOKEN_FRONTEND`: (paste token)

**What Auditors Look For**:
- Screenshot of GitHub Actions workflow
- Evidence of automatic triggers on push
- Build logs showing SonarQube analysis

### 4.3 Method 2: Webhook (Alternative)

If using Jenkins for GitHub integration:

**In SonarQube**:
1. **Administration** → **Configuration** → **Webhooks**
2. Click **Create**
3. Name: `GitHub Webhook`
4. URL: `https://your-jenkins-url/sonarqube-webhook/`
5. Secret: (optional but recommended)

**In GitHub**:
1. Repository **Settings** → **Webhooks**
2. Add webhook: `http://your-sonarqube:9000/api/project_analyses/update_projects`
3. Content type: `application/json`
4. Events: Push events, Pull request events

---

## 5. Jenkins Pipeline Integration

**Audit Point**: ✅ *SonarQube runs via Docker and analyzes code during CI/CD pipeline*

### 5.1 Install SonarQube Scanner in Jenkins

**In Jenkins**:
1. **Manage Jenkins** → **Plugins**
2. Install: **SonarQube Scanner for Jenkins**
3. Restart Jenkins

### 5.2 Configure SonarQube Server in Jenkins

1. **Manage Jenkins** → **Configure System**
2. Scroll to **SonarQube servers**
3. Click **Add SonarQube**
   - Name: `SonarQube-Local`
   - Server URL: `http://sonarqube:9000` (if in Docker network) or `http://host.docker.internal:9000`
   - Server authentication token: Add credential
     - Kind: **Secret text**
     - Secret: (paste admin token)
     - ID: `sonarqube-token`
     - Description: `SonarQube Admin Token`

### 5.3 Configure SonarQube Scanner

1. **Manage Jenkins** → **Global Tool Configuration**
2. Scroll to **SonarQube Scanner**
3. Click **Add SonarQube Scanner**
   - Name: `SonarQube-Scanner`
   - Install automatically: ✅
   - Version: Latest

### 5.4 Updated Jenkinsfile for Java Services

Example for `api-gateway/Jenkinsfile`:

```groovy
pipeline {
    agent any
    
    tools {
        maven 'Maven-3.8'
        jdk 'JDK-17'
    }
    
    environment {
        SONAR_PROJECT_KEY = 'ecommerce-api-gateway'
        SONAR_HOST_URL = 'http://sonarqube:9000'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Build') {
            steps {
                dir('api-gateway') {
                    sh 'mvn clean compile'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                dir('api-gateway') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir('api-gateway') {
                    withSonarQubeEnv('SonarQube-Local') {
                        sh '''
                            mvn sonar:sonar \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.host.url=${SONAR_HOST_URL} \
                              -Dsonar.java.binaries=target/classes
                        '''
                    }
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
        
        stage('Package') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                dir('api-gateway') {
                    sh 'mvn package -DskipTests'
                }
            }
        }
        
        stage('Docker Build') {
            when {
                branch 'main'
            }
            steps {
                dir('api-gateway') {
                    sh 'docker build -t ecommerce-api-gateway:latest .'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
        failure {
            echo 'Pipeline failed! Check SonarQube quality gate.'
        }
        success {
            echo 'Pipeline succeeded! Code quality passed.'
        }
    }
}
```

### 5.5 Updated Jenkinsfile for Frontend

Example for `frontend/Jenkinsfile`:

```groovy
pipeline {
    agent any
    
    tools {
        nodejs 'NodeJS-18'
    }
    
    environment {
        SONAR_PROJECT_KEY = 'ecommerce-frontend'
        SONAR_HOST_URL = 'http://sonarqube:9000'
        SCANNER_HOME = tool 'SonarQube-Scanner'
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Install Dependencies') {
            steps {
                dir('frontend') {
                    sh 'npm ci'
                }
            }
        }
        
        stage('Lint') {
            steps {
                dir('frontend') {
                    sh 'npm run lint || true'
                }
            }
        }
        
        stage('Unit Tests') {
            steps {
                dir('frontend') {
                    sh 'npm run test:coverage'
                }
            }
            post {
                always {
                    publishHTML([
                        reportDir: 'frontend/coverage',
                        reportFiles: 'index.html',
                        reportName: 'Coverage Report'
                    ])
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                dir('frontend') {
                    withSonarQubeEnv('SonarQube-Local') {
                        sh """
                            ${SCANNER_HOME}/bin/sonar-scanner \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.sources=src \
                              -Dsonar.tests=src \
                              -Dsonar.test.inclusions=**/*.spec.ts \
                              -Dsonar.typescript.lcov.reportPaths=coverage/lcov.info \
                              -Dsonar.exclusions=**/node_modules/**,**/dist/**,**/*.spec.ts
                        """
                    }
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
        
        stage('Build') {
            when {
                expression { currentBuild.result == null || currentBuild.result == 'SUCCESS' }
            }
            steps {
                dir('frontend') {
                    sh 'npm run build'
                }
            }
        }
        
        stage('Docker Build') {
            when {
                branch 'main'
            }
            steps {
                dir('frontend') {
                    sh 'docker build -t ecommerce-frontend:latest .'
                }
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}
```

**What Auditors Look For**:
- SonarQube analysis stage in pipeline
- Quality Gate enforcement
- Pipeline failure on quality gate failure
- Build logs showing analysis execution

---

## 6. Quality Gates Configuration

**Audit Point**: ✅ *CI/CD pipeline fails when quality gates or security rules fail*

### 6.1 Default Quality Gate

SonarQube comes with "Sonar way" quality gate. Review it:

Navigate to: **Quality Gates** → **Sonar way**

Default conditions:
- Coverage < 80% → ⚠️ Warning
- Duplicated Lines (%) > 3% → ⚠️ Warning
- Maintainability Rating > A → ⚠️ Warning
- Reliability Rating > A → ⚠️ Warning
- Security Rating > A → ⚠️ Warning
- Security Hotspots Reviewed < 100% → ⚠️ Warning

### 6.2 Create Custom Quality Gate

**For student projects**, create a stricter quality gate:

1. **Quality Gates** → **Create**
2. Name: `E-Commerce-Quality-Gate`
3. Add conditions:

#### For New Code (Recommended for existing projects):
- **Coverage on New Code** < 80% → ERROR
- **Duplicated Lines (%) on New Code** > 3% → ERROR
- **Maintainability Rating on New Code** worse than A → ERROR
- **Reliability Rating on New Code** worse than A → ERROR
- **Security Rating on New Code** worse than A → ERROR
- **Security Hotspots Reviewed on New Code** < 100% → ERROR

#### For Overall Code (Stricter):
- **Blocker Issues** > 0 → ERROR
- **Critical Issues** > 0 → ERROR
- **Major Issues** > 10 → ERROR
- **Code Smells** > 50 → WARNING
- **Technical Debt Ratio** > 5% → ERROR

### 6.3 Assign Quality Gate to Projects

For each project:
1. Go to project (e.g., `ecommerce-api-gateway`)
2. **Project Settings** → **Quality Gate**
3. Select: `E-Commerce-Quality-Gate`
4. Save

### 6.4 Test Quality Gate Failure

**Create a deliberate failure**:

1. Commit code with a security vulnerability (e.g., hardcoded password)
2. Push to GitHub
3. Watch Jenkins pipeline fail at Quality Gate stage
4. **Document**: Take screenshot of failed build

**What Auditors Look For**:
- Screenshot of quality gate configuration
- Evidence of pipeline failure on quality gate violation
- Documentation of quality metrics thresholds

---

## 7. Security & Permissions

**Audit Point**: ✅ *SonarQube permissions and access controls prevent unauthorized access*

### 7.1 Create User Groups

Navigate to: **Administration** → **Security** → **Groups**

Create these groups:
1. **developers** - Can browse and view analysis
2. **code-reviewers** - Can create/edit rules
3. **project-admins** - Full project administration

### 7.2 Create User Accounts

Navigate to: **Administration** → **Security** → **Users**

Create accounts for team members:
1. Click **Create User**
2. Enter details:
   - Login: `john.doe`
   - Name: `John Doe`
   - Email: `john.doe@example.com`
   - Password: (strong password)
3. Add to groups: `developers`

### 7.3 Configure Permissions

Navigate to: **Administration** → **Security** → **Global Permissions**

#### For `developers` group:
- ✅ Browse
- ✅ See Source Code
- ❌ Administer System
- ❌ Administer Quality Gates
- ❌ Administer Quality Profiles

#### For `code-reviewers` group:
- ✅ Browse
- ✅ See Source Code
- ✅ Administer Quality Profiles
- ✅ Administer Quality Gates
- ❌ Administer System

#### For `project-admins` group:
- ✅ All permissions except Administer System

### 7.4 Project-Level Permissions

For each project:
1. Go to **Project Settings** → **Permissions**
2. Configure:
   - `developers`: Browse, See Source Code
   - `code-reviewers`: + Administer Issues
   - `project-admins`: + Administer project

### 7.5 Token Security Best Practices

**Document these practices**:

1. ✅ Use **Project Analysis Tokens** (not user tokens)
2. ✅ Set expiration dates (90 days recommended)
3. ✅ Rotate tokens regularly
4. ✅ Never commit tokens to Git
5. ✅ Store tokens in Jenkins credentials
6. ✅ Use GitHub Secrets for GitHub Actions
7. ✅ Revoke tokens when no longer needed

### 7.6 Force Authentication

Navigate to: **Administration** → **Security** → **General Settings**

Enable:
- ✅ **Force user authentication**: ON
- This prevents anonymous browsing

**What Auditors Look For**:
- User accounts with proper roles
- Permission matrix documentation
- Token management procedures
- Evidence of authentication requirements

---

## 8. Code Review & Approval Process

**Audit Point**: ✅ *A code review and approval process is implemented*

### 8.1 GitHub Branch Protection Rules

**In your GitHub repository**:

1. Go to **Settings** → **Branches**
2. Click **Add rule**
3. Branch name pattern: `main`
4. Enable:
   - ✅ **Require a pull request before merging**
     - Required approvals: **2**
     - ✅ Dismiss stale pull request approvals
     - ✅ Require review from Code Owners
   - ✅ **Require status checks to pass**
     - Add: `SonarQube Analysis / Java Services Analysis`
     - Add: `SonarQube Analysis / Frontend Analysis`
     - ✅ Require branches to be up to date
   - ✅ **Require conversation resolution before merging**
   - ✅ **Include administrators**
5. Create

### 8.2 Create CODEOWNERS File

Create `.github/CODEOWNERS`:

```
# Code Owners for E-Commerce Microservices

# Default owners
* @team-lead @senior-dev

# Java Services
/api-gateway/ @backend-team @team-lead
/user-service/ @backend-team @security-lead
/product-service/ @backend-team
/media-service/ @backend-team

# Frontend
/frontend/ @frontend-team @ux-lead

# Infrastructure
/deployment/ @devops-team @team-lead
docker-compose*.yml @devops-team

# CI/CD
Jenkinsfile @devops-team
.github/workflows/ @devops-team
```

### 8.3 Pull Request Template

Create `.github/PULL_REQUEST_TEMPLATE.md`:

```markdown
## Description
<!-- Describe your changes -->

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Checklist
- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests pass locally
- [ ] SonarQube analysis passed
- [ ] No new security vulnerabilities
- [ ] No decrease in code coverage

## SonarQube Results
<!-- Paste link to SonarQube analysis -->
- Quality Gate: PASSED/FAILED
- Coverage: X%
- Bugs: X
- Vulnerabilities: X
- Code Smells: X

## Screenshots (if applicable)

## Related Issues
Closes #(issue number)
```

### 8.4 Code Review Checklist

Create `docs/CODE_REVIEW_CHECKLIST.md`:

```markdown
# Code Review Checklist

## Before Submitting PR
- [ ] Code builds successfully
- [ ] All tests pass
- [ ] SonarQube analysis run locally
- [ ] No new warnings or errors
- [ ] Code is self-documented
- [ ] Branch is up to date with main

## Reviewer Checklist

### Functionality
- [ ] Code does what it's supposed to do
- [ ] Edge cases are handled
- [ ] Error handling is appropriate

### Code Quality
- [ ] Code is readable and maintainable
- [ ] No code duplication
- [ ] Functions are small and focused
- [ ] Naming is clear and consistent
- [ ] No magic numbers or strings

### Security
- [ ] No hardcoded credentials
- [ ] Input validation is present
- [ ] SQL injection prevented
- [ ] XSS vulnerabilities addressed
- [ ] Authentication/authorization correct

### Performance
- [ ] No obvious performance issues
- [ ] Database queries optimized
- [ ] Appropriate caching used

### Testing
- [ ] Unit tests cover new code
- [ ] Tests are meaningful
- [ ] Test names are descriptive

### SonarQube
- [ ] Quality gate passed
- [ ] No new critical/blocker issues
- [ ] Code coverage maintained/improved
- [ ] Technical debt acceptable

### Documentation
- [ ] README updated if needed
- [ ] API documentation updated
- [ ] Comments explain "why" not "what"
```

### 8.5 Workflow Example

**Typical workflow**:

1. Developer creates feature branch
2. Makes changes, commits locally
3. Runs SonarQube analysis locally (optional but recommended)
4. Pushes to GitHub
5. GitHub Actions runs SonarQube analysis
6. Developer creates PR
7. SonarQube analysis shown in PR
8. 2 reviewers must approve
9. All status checks must pass (including SonarQube)
10. Merge allowed only if quality gate passes

**What Auditors Look For**:
- Branch protection rules screenshot
- Example PR showing review process
- Evidence of blocked merges on failed checks
- CODEOWNERS file

---

## 9. Fixing Code Quality Issues

**Audit Point**: ✅ *Code quality issues identified by SonarQube are fixed and committed*

### 9.1 Reading SonarQube Reports

Navigate to project dashboard (e.g., `ecommerce-api-gateway`):

#### Key Metrics:
- **Bugs**: Logic errors that will cause problems
- **Vulnerabilities**: Security issues
- **Code Smells**: Maintainability issues
- **Coverage**: Test coverage percentage
- **Duplications**: Duplicate code blocks
- **Security Hotspots**: Security-sensitive code requiring review

### 9.2 Common Issues and Fixes

#### Issue 1: Hardcoded Credentials (Blocker - Security)

**SonarQube Finding**:
```java
String password = "admin123"; // Hardcoded password
```

**Fix**:
```java
// Use environment variables
String password = System.getenv("DB_PASSWORD");
if (password == null) {
    throw new IllegalStateException("DB_PASSWORD not set");
}
```

**Commit Message**:
```
fix(security): Remove hardcoded password, use environment variable

- Replaced hardcoded password with environment variable
- Added validation for missing configuration
- Resolves SonarQube security vulnerability S2068

Fixes: Blocker - "Credentials should not be hard-coded"
```

#### Issue 2: Resource Not Closed (Critical - Bug)

**SonarQube Finding**:
```java
FileInputStream fis = new FileInputStream("file.txt");
// Stream never closed - resource leak
```

**Fix**:
```java
try (FileInputStream fis = new FileInputStream("file.txt")) {
    // Use stream
} // Automatically closed
```

#### Issue 3: Cognitive Complexity (Major - Code Smell)

**SonarQube Finding**:
```java
public void complexMethod() {
    // Method with cognitive complexity of 25 (threshold: 15)
    if (condition1) {
        for (int i = 0; i < 10; i++) {
            if (condition2) {
                while (condition3) {
                    // nested logic
                }
            }
        }
    }
}
```

**Fix**: Refactor into smaller methods
```java
public void complexMethod() {
    if (condition1) {
        processItems();
    }
}

private void processItems() {
    for (int i = 0; i < 10; i++) {
        handleItem(i);
    }
}

private void handleItem(int index) {
    if (condition2) {
        executeWhileCondition();
    }
}
```

#### Issue 4: SQL Injection (Blocker - Vulnerability)

**SonarQube Finding**:
```java
String query = "SELECT * FROM users WHERE id = " + userId;
```

**Fix**:
```java
String query = "SELECT * FROM users WHERE id = ?";
PreparedStatement stmt = connection.prepareStatement(query);
stmt.setInt(1, userId);
```

### 9.3 Creating a Fix Branch

**Workflow**:

```bash
# Create fix branch
git checkout -b fix/sonarqube-security-issues

# Make fixes
# ... edit files ...

# Run analysis locally
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=ecommerce-api-gateway \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN

# Verify fixes in SonarQube UI

# Commit with descriptive message
git add .
git commit -m "fix(security): Address SonarQube security vulnerabilities

- Fixed hardcoded password in UserService
- Resolved SQL injection in ProductRepository
- Added resource management with try-with-resources

SonarQube Results:
- Blocker issues: 3 → 0
- Critical issues: 2 → 0
- Security Rating: E → A"

# Push and create PR
git push origin fix/sonarqube-security-issues
```

### 9.4 Document All Fixes

Create `docs/SONARQUBE_FIX_LOG.md`:

```markdown
# SonarQube Issue Resolution Log

## Analysis Date: 2026-01-21

### Project: ecommerce-api-gateway

#### Fixed Issues

| Issue ID | Type | Severity | Description | Fix | Commit |
|----------|------|----------|-------------|-----|--------|
| S2068 | Vulnerability | Blocker | Hardcoded password | Moved to env var | abc123 |
| S2095 | Bug | Critical | Resource not closed | Added try-with-resources | abc123 |
| S1192 | Code Smell | Major | String literal duplication | Extracted constant | def456 |
| S3776 | Code Smell | Major | High cognitive complexity | Refactored method | def456 |

#### Before/After Metrics

| Metric | Before | After | Change |
|--------|--------|-------|--------|
| Bugs | 5 | 0 | ✅ -5 |
| Vulnerabilities | 3 | 0 | ✅ -3 |
| Code Smells | 47 | 12 | ✅ -35 |
| Coverage | 65% | 78% | ✅ +13% |
| Duplications | 8.2% | 2.1% | ✅ -6.1% |
| Security Rating | E | A | ✅ |
| Maintainability | C | A | ✅ |
```

**What Auditors Look For**:
- Before/after screenshots of SonarQube dashboard
- Git commit history showing fixes
- Documentation of issues and resolutions
- Evidence that code quality improved

---

## 10. Bonus Features

### 10.1 Slack Notifications

#### Step 1: Install Slack Plugin

In Jenkins:
1. **Manage Jenkins** → **Plugins**
2. Install **Slack Notification Plugin**

#### Step 2: Configure Slack Workspace

1. Go to Slack API: https://api.slack.com/apps
2. Create new app: "SonarQube Notifications"
3. Add features: **Incoming Webhooks**
4. Activate webhooks
5. Add webhook to workspace
6. Copy webhook URL

#### Step 3: Configure Jenkins

1. **Manage Jenkins** → **Configure System**
2. Scroll to **Slack**
3. Workspace: Your workspace name
4. Credential: Add webhook URL as secret text
5. Default channel: `#sonarqube-alerts`
6. Test connection

#### Step 4: Update Jenkinsfile

Add to post section:

```groovy
post {
    success {
        slackSend(
            color: 'good',
            message: """
                ✅ Quality Gate PASSED: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                Project: ${env.SONAR_PROJECT_KEY}
                View: ${env.SONAR_HOST_URL}/dashboard?id=${env.SONAR_PROJECT_KEY}
            """,
            channel: '#sonarqube-alerts'
        )
    }
    failure {
        slackSend(
            color: 'danger',
            message: """
                ❌ Quality Gate FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}
                Project: ${env.SONAR_PROJECT_KEY}
                Check: ${env.BUILD_URL}console
            """,
            channel: '#sonarqube-alerts'
        )
    }
}
```

### 10.2 Email Notifications

#### Configure in SonarQube:

1. **Administration** → **Configuration** → **General** → **Email**
2. Configure SMTP (as shown in section 2.2)
3. Test email

#### Set Project Notifications:

1. Go to project
2. **Project Settings** → **Notifications**
3. Add subscribers for:
   - Quality gate status changes
   - New issues
   - New vulnerabilities

### 10.3 IDE Integration (VS Code)

#### Install SonarLint Extension:

1. Open VS Code
2. Extensions: Search "SonarLint"
3. Install: **SonarLint by SonarSource**

#### Configure SonarLint:

Create `.vscode/settings.json`:

```json
{
  "sonarlint.connectedMode.connections.sonarqube": [
    {
      "serverUrl": "http://localhost:9000",
      "token": "YOUR_USER_TOKEN"
    }
  ],
  "sonarlint.connectedMode.project": {
    "projectKey": "ecommerce-api-gateway"
  }
}
```

#### Generate User Token for IDE:

1. In SonarQube: **My Account** → **Security** → **Generate**
2. Name: `vscode-integration`
3. Type: User Token
4. Copy token
5. Paste in VS Code settings

#### Usage:

- Real-time analysis as you type
- Issues highlighted in editor
- Rule descriptions on hover
- Quick fixes suggested

### 10.4 IDE Integration (IntelliJ IDEA)

#### Install Plugin:

1. **File** → **Settings** → **Plugins**
2. Search: "SonarLint"
3. Install and restart

#### Connect to SonarQube:

1. **File** → **Settings** → **Tools** → **SonarLint**
2. Click **+** to add SonarQube connection
3. URL: `http://localhost:9000`
4. Token: (paste user token)
5. Test connection
6. Bind to project: Select `ecommerce-api-gateway`

---

## 11. Audit Compliance Checklist

### ✅ Requirement 1: SonarQube Web Interface Accessible

**Evidence to Provide**:
- [ ] Screenshot of SonarQube login page (http://localhost:9000)
- [ ] Screenshot of SonarQube dashboard showing projects
- [ ] Docker container running: `docker ps | grep sonarqube`
- [ ] Access logs showing successful connection

**How to Demonstrate**:
```bash
# Show SonarQube is running
docker ps | grep sonarqube

# Show logs
docker logs sonarqube | grep "SonarQube is operational"

# Access UI
open http://localhost:9000
```

---

### ✅ Requirement 2: GitHub Integration with Auto-Trigger

**Evidence to Provide**:
- [ ] GitHub Actions workflow file (`.github/workflows/sonarqube-analysis.yml`)
- [ ] Screenshot of workflow run triggered by push
- [ ] Build log showing SonarQube analysis execution
- [ ] Screenshot of SonarQube showing analysis results from GitHub

**How to Demonstrate**:
```bash
# Show workflow file
cat .github/workflows/sonarqube-analysis.yml

# Push to trigger
git commit --allow-empty -m "test: Trigger SonarQube analysis"
git push origin main

# Show in GitHub Actions tab
```

---

### ✅ Requirement 3: Docker-Based Analysis in CI/CD

**Evidence to Provide**:
- [ ] Docker Compose file for SonarQube
- [ ] Jenkins pipeline showing SonarQube stage
- [ ] Build log showing Maven/Gradle executing sonar:sonar
- [ ] Screenshot of SonarQube dashboard after Jenkins run

**How to Demonstrate**:
```bash
# Show SonarQube Docker setup
cat deployment/docker-compose.sonarqube.yml

# Show Jenkins pipeline
cat api-gateway/Jenkinsfile | grep -A 10 "SonarQube Analysis"

# Trigger Jenkins build
# (manual or via webhook)
```

---

### ✅ Requirement 4: Pipeline Fails on Quality Gate Failure

**Evidence to Provide**:
- [ ] Quality gate configuration screenshot
- [ ] Jenkins pipeline with `abortPipeline: true`
- [ ] Screenshot of failed Jenkins build due to quality gate
- [ ] Git commit that caused failure (test case)

**How to Demonstrate**:
```bash
# Create deliberate failure
# Add code with hardcoded password
cat >> api-gateway/src/main/java/com/ecommerce/Test.java << EOF
public class Test {
    private String password = "admin123"; // This will fail
}
EOF

# Commit and push
git add .
git commit -m "test: Intentional quality gate failure"
git push origin test-quality-gate

# Watch Jenkins fail at Quality Gate stage
# Take screenshot of failure
```

---

### ✅ Requirement 5: Code Review & Approval Process

**Evidence to Provide**:
- [ ] Branch protection rules screenshot
- [ ] CODEOWNERS file
- [ ] Pull request template
- [ ] Example PR showing approval workflow
- [ ] Screenshot of blocked merge due to failed checks

**How to Demonstrate**:
```bash
# Show branch protection
# (GitHub UI screenshot)

# Show CODEOWNERS
cat .github/CODEOWNERS

# Show PR template
cat .github/PULL_REQUEST_TEMPLATE.md

# Create example PR
git checkout -b example/pr-workflow
git commit --allow-empty -m "docs: Example PR for audit"
git push origin example/pr-workflow
# Create PR in GitHub UI
```

---

### ✅ Requirement 6: Permissions & Access Control

**Evidence to Provide**:
- [ ] User accounts list screenshot
- [ ] Groups and permissions matrix
- [ ] Force authentication enabled
- [ ] Token security documentation
- [ ] Example of access denied for unauthorized user

**How to Demonstrate**:
1. Navigate to **Administration** → **Security** → **Users**
2. Take screenshot showing multiple users
3. Navigate to **Groups**
4. Take screenshot of permission matrix
5. Try accessing SonarQube in private browser (should redirect to login)

---

### ✅ Requirement 7: Rules Detect Issues

**Evidence to Provide**:
- [ ] Quality profile configuration screenshot
- [ ] Example of detected bug
- [ ] Example of detected vulnerability
- [ ] Example of detected code smell
- [ ] Rule descriptions and severity levels

**How to Demonstrate**:
1. Navigate to **Rules**
2. Show activated rules for Java/TypeScript
3. Navigate to project **Issues**
4. Take screenshot showing different issue types
5. Click on issue to show rule description

---

### ✅ Requirement 8: Issues Fixed and Committed

**Evidence to Provide**:
- [ ] "Before" screenshot of SonarQube dashboard
- [ ] "After" screenshot showing improvements
- [ ] Git commit log showing fixes
- [ ] Fix documentation (SONARQUBE_FIX_LOG.md)
- [ ] Code diff showing actual fixes

**How to Demonstrate**:
```bash
# Show git log of fixes
git log --oneline --grep="fix.*sonarqube"

# Show specific fix diff
git show COMMIT_HASH

# Show fix documentation
cat docs/SONARQUBE_FIX_LOG.md

# Show before/after in SonarQube UI
```

---

### ✅ Bonus: Notifications Configured

**Evidence to Provide**:
- [ ] Slack webhook configuration
- [ ] Example Slack notification screenshot
- [ ] Email SMTP configuration
- [ ] Example email notification

**How to Demonstrate**:
- Show Slack channel with notifications
- Show Jenkins configuration for Slack
- Trigger build and show notification appears

---

### ✅ Bonus: IDE Integration

**Evidence to Provide**:
- [ ] VS Code with SonarLint installed
- [ ] Screenshot of real-time issue detection
- [ ] Configuration file (.vscode/settings.json)
- [ ] Example of quick fix in IDE

**How to Demonstrate**:
1. Open VS Code
2. Open Java/TypeScript file
3. Introduce issue (e.g., unused variable)
4. Show SonarLint highlighting issue in real-time
5. Take screenshot

---

## Quick Start Commands

### Start Everything

```bash
# Start SonarQube
cd deployment
./start-sonarqube.sh

# Wait 2 minutes, then access
open http://localhost:9000

# Login: admin / admin (change password)

# Start Jenkins (if separate)
./start-jenkins.sh
```

### Run Local Analysis

**Java Service**:
```bash
cd api-gateway
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=ecommerce-api-gateway \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN
```

**Frontend**:
```bash
cd frontend
npm run test:coverage

sonar-scanner \
  -Dsonar.projectKey=ecommerce-frontend \
  -Dsonar.sources=src \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN
```

### Stop Everything

```bash
cd deployment
./stop-sonarqube.sh
./stop-jenkins.sh
```

---

## Troubleshooting

### SonarQube Won't Start

```bash
# Check logs
docker logs sonarqube

# Common issues:
# 1. Insufficient memory
docker update --memory=4g sonarqube

# 2. Permission issues
sudo chown -R 1000:1000 deployment/sonarqube-*

# 3. Port already in use
lsof -i :9000
```

### Quality Gate Always Passes/Fails

```bash
# Check quality gate configuration
# Navigate to: Quality Gates → Your Gate → Conditions

# Verify webhook is working
curl -X POST http://localhost:9000/api/webhooks/list \
  -u admin:YOUR_PASSWORD
```

### Jenkins Can't Connect to SonarQube

```bash
# If SonarQube is in Docker and Jenkins too:
# Use: http://sonarqube:9000 (container name)

# If Jenkins is on host:
# Use: http://host.docker.internal:9000

# Test connectivity from Jenkins
docker exec jenkins curl http://sonarqube:9000/api/system/status
```

---

## Summary

This guide provides a complete, audit-ready SonarQube integration for your e-commerce microservices project. Each section addresses specific audit requirements with concrete examples and evidence.

**Key Takeaways**:
1. ✅ SonarQube runs locally via Docker
2. ✅ Automatically triggered on every push
3. ✅ Pipeline fails on quality/security violations
4. ✅ Code review process enforced
5. ✅ Security and access controls implemented
6. ✅ Real issues detected and fixed
7. ✅ Complete audit trail maintained

**Next Steps**:
1. Follow this guide step-by-step
2. Take screenshots at each stage
3. Document all configurations
4. Create example PR demonstrating workflow
5. Prepare presentation showing live demo

Good luck with your audit! 🎯
