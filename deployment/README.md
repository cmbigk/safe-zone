# Jenkins CI/CD Pipeline Setup Guide

## Overview
This document provides comprehensive instructions for setting up a Jenkins CI/CD pipeline for the e-commerce microservices platform.

## 📋 Table of Contents
1. [Prerequisites](#prerequisites)
2. [Initial Setup](#initial-setup)
3. [Jenkins Configuration](#jenkins-configuration)
4. [Pipeline Creation](#pipeline-creation)
5. [Testing Setup](#testing-setup)
6. [Deployment Strategies](#deployment-strategies)
7. [Notifications](#notifications)
8. [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software
- Docker & Docker Compose
- Git
- Java 21
- Maven 3.9+
- Node.js 20+
- Angular CLI

### System Requirements
- **RAM:** 8GB minimum (16GB recommended)
- **Disk Space:** 20GB free
- **OS:** macOS, Linux, or Windows with WSL2

---

## Initial Setup

### 1. Start Jenkins

```bash
cd deployment
chmod +x start-jenkins.sh stop-jenkins.sh
chmod +x scripts/*.sh
./start-jenkins.sh
```

Jenkins will be available at: **http://localhost:8090**

### 2. Get Initial Admin Password

```bash
docker exec jenkins-ci cat /var/jenkins_home/secrets/initialAdminPassword
```

### 3. Complete Jenkins Setup Wizard

1. Open http://localhost:8090
2. blah
3. Install suggested plugins
4. Create admin user (or use default: admin/admin123)
5. Set Jenkins URL: `http://localhost:8090`

### 4. Install Additional Plugins

Go to **Manage Jenkins → Plugin Manager → Available** and install:

- ✅ Git Plugin
- ✅ GitHub Plugin
- ✅ Docker Pipeline
- ✅ Maven Integration
- ✅ NodeJS Plugin
- ✅ JUnit Plugin
- ✅ JaCoCo Plugin
- ✅ Email Extension
- ✅ Slack Notification
- ✅ Blue Ocean
- ✅ Pipeline Stage View

---

## Jenkins Configuration

### Configure Tools

#### 1. Configure Java (JDK 21)
**Manage Jenkins → Global Tool Configuration → JDK**

- Name: `JDK-21`
- Install automatically: ✅
- Version: OpenJDK 21

#### 2. Configure Maven
**Manage Jenkins → Global Tool Configuration → Maven**

- Name: `Maven-3.9`
- Install automatically: ✅
- Version: 3.9.5

#### 3. Configure Node.js
**Manage Jenkins → Global Tool Configuration → NodeJS**

- Name: `NodeJS-20`
- Install automatically: ✅
- Version: 20.x
- Global packages: `@angular/cli`

#### 4. Configure Git
**Manage Jenkins → Global Tool Configuration → Git**

- Name: `Default`
- Path to Git: `git`

### Configure Credentials

**Manage Jenkins → Manage Credentials → (global) → Add Credentials**

#### GitHub Credentials
- Kind: Username with password
- ID: `github-credentials`
- Username: [Your GitHub username]
- Password: [Your GitHub Personal Access Token]
- Description: GitHub Access Token

#### Docker Hub Credentials (Optional)
- Kind: Username with password
- ID: `dockerhub-credentials`
- Username: [Your Docker Hub username]
- Password: [Your Docker Hub password]

#### Slack Token (for notifications)
- Kind: Secret text
- ID: `slack-token`
- Secret: [Your Slack webhook URL]

---

## Pipeline Creation

### Method 1: Create Individual Service Pipelines

#### Create User Service Pipeline

1. **New Item** → Enter name: `user-service-pipeline`
2. Select **Pipeline** → Click OK
3. Configure:
   - Description: "CI/CD pipeline for User Service"
   - Build Triggers: ✅ Poll SCM: `H/5 * * * *` (every 5 minutes)
   - Pipeline Definition: **Pipeline script from SCM**
     - SCM: Git
     - Repository URL: [Your Git repository URL]
     - Credentials: `github-credentials`
     - Branch: `*/main` or `*/master`
     - Script Path: `user-service/Jenkinsfile`

4. Click **Save**

#### Repeat for Other Services
- `product-service-pipeline` → Script Path: `product-service/Jenkinsfile`
- `media-service-pipeline` → Script Path: `media-service/Jenkinsfile`
- `api-gateway-pipeline` → Script Path: `api-gateway/Jenkinsfile`
- `frontend-pipeline` → Script Path: `frontend/Jenkinsfile`

### Method 2: Create Full Stack Pipeline

1. **New Item** → Enter name: `fullstack-deployment`
2. Select **Pipeline** → Click OK
3. Configure:
   - Pipeline Definition: **Pipeline script from SCM**
   - Script Path: `deployment/Jenkinsfile.fullstack`

This pipeline will build and deploy all services in parallel.

---

## Testing Setup

### Backend Testing (Java/Spring Boot)

#### Add Test Dependencies to pom.xml

```xml
<dependencies>
    <!-- JUnit 5 -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Mockito -->
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <scope>test</scope>
    </dependency>
    
    <!-- Spring Boot Test -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- JaCoCo for code coverage -->
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### Frontend Testing (Angular)

#### Update package.json

```json
{
  "scripts": {
    "test": "ng test",
    "test:ci": "ng test --watch=false --code-coverage --browsers=ChromeHeadless",
    "lint": "ng lint",
    "e2e": "ng e2e"
  },
  "devDependencies": {
    "karma": "^6.4.0",
    "karma-chrome-launcher": "^3.1.0",
    "karma-coverage": "^2.2.0",
    "karma-jasmine": "^5.1.0",
    "karma-junit-reporter": "^2.0.1"
  }
}
```

#### Configure Karma for CI

Create/update `frontend/karma.conf.js`:

```javascript
module.exports = function(config) {
  config.set({
    browsers: ['ChromeHeadless'],
    singleRun: true,
    reporters: ['progress', 'junit', 'coverage'],
    junitReporter: {
      outputDir: 'test-results',
      outputFile: 'test-results.xml'
    },
    coverageReporter: {
      dir: 'coverage',
      reporters: [
        { type: 'html' },
        { type: 'lcov' }
      ]
    }
  });
};
```

---

## Deployment Strategies

### 1. Blue-Green Deployment

The pipelines implement blue-green deployment:

1. New version deployed as "green" instance
2. Health checks performed on green
3. If healthy, traffic switched from blue to green
4. Old blue instance kept for rollback

### 2. Rollback Strategy

If deployment fails:

```bash
# Manual rollback
cd deployment/scripts
./rollback.sh user-service 42  # Rollback to build #42
```

### 3. Automated Rollback

The Jenkinsfile includes automated rollback on health check failure:

```groovy
stage('Deploy') {
    steps {
        script {
            try {
                // Deploy new version
                sh "docker run -d --name ${SERVICE_NAME}-green ..."
                
                // Health check
                sh "curl -f http://localhost:8081/actuator/health"
                
                // Switch traffic
                sh "docker stop ${SERVICE_NAME}"
                sh "docker rename ${SERVICE_NAME}-green ${SERVICE_NAME}"
            } catch (Exception e) {
                // Rollback on failure
                sh "docker stop ${SERVICE_NAME}-green || true"
                sh "docker rm ${SERVICE_NAME}-green || true"
                error("Deployment failed - rolled back")
            }
        }
    }
}
```

---

## Notifications

### Email Notifications

#### Configure Email Server

**Manage Jenkins → Configure System → Extended E-mail Notification**

- SMTP server: `smtp.gmail.com`
- SMTP Port: `465`
- Use SSL: ✅
- Credentials: Add Gmail app password
- Default recipients: `your-email@example.com`

#### Gmail Setup
1. Enable 2-factor authentication
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Use app password in Jenkins credentials

### Slack Notifications

#### Setup Slack Integration

1. Go to your Slack workspace
2. Add Jenkins CI app: https://slack.com/apps/A0F7VRFKN-jenkins-ci
3. Choose a channel (e.g., `#deployments`)
4. Copy the Webhook URL

#### Configure in Jenkins

**Manage Jenkins → Configure System → Slack**

- Workspace: [Your workspace name]
- Credential: Add webhook URL as Secret text
- Default channel: `#deployments`
- Test Connection

---

## Pipeline Parameters

Each pipeline supports the following parameters:

### ENVIRONMENT
- `dev` - Development environment
- `staging` - Staging environment  
- `production` - Production environment

### SKIP_TESTS
- `false` - Run all tests (default)
- `true` - Skip tests (for hotfixes)

### DEPLOY
- `true` - Deploy after successful build (default)
- `false` - Build only, no deployment

### Usage Example

When triggering a build manually:
1. Click "Build with Parameters"
2. Select ENVIRONMENT: `staging`
3. SKIP_TESTS: `false`
4. DEPLOY: `true`
5. Click "Build"

---

## Monitoring & Logs

### View Build Logs

```bash
# Jenkins container logs
docker logs -f jenkins-ci

# Service logs
docker logs -f user-service
docker logs -f product-service
docker logs -f frontend
```

### Health Checks

```bash
# Run health check script
cd deployment/scripts
./health-check.sh
```

### Smoke Tests

```bash
# Run smoke tests
cd deployment/scripts
./smoke-tests.sh
```

---

## Distributed Builds (Bonus)

### Configure Jenkins Agent

The setup includes a Jenkins agent for distributed builds:

1. **Manage Jenkins → Manage Nodes and Clouds**
2. Click **New Node**
3. Configure:
   - Node name: `docker-agent`
   - Remote root directory: `/home/jenkins/agent`
   - Labels: `docker maven nodejs`
   - Launch method: Launch agent via SSH

### Update Jenkinsfile to Use Specific Agent

```groovy
pipeline {
    agent {
        label 'docker'
    }
    // ... rest of pipeline
}
```

---

## Parameterized Builds (Bonus)

### Example: Dynamic Version Tagging

```groovy
parameters {
    string(name: 'VERSION_TAG', defaultValue: 'latest', description: 'Docker image version tag')
    choice(name: 'BUILD_TYPE', choices: ['snapshot', 'release'], description: 'Build type')
}

stages {
    stage('Docker Build') {
        steps {
            script {
                def imageTag = params.VERSION_TAG
                if (params.BUILD_TYPE == 'snapshot') {
                    imageTag = "${VERSION_TAG}-SNAPSHOT-${BUILD_NUMBER}"
                }
                docker.build("${DOCKER_IMAGE}:${imageTag}")
            }
        }
    }
}
```

---

## Best Practices

### 1. Version Control
- ✅ All Jenkinsfiles in Git repository
- ✅ Use Git tags for release versions
- ✅ Branch-based deployments

### 2. Testing
- ✅ Fail fast - run unit tests first
- ✅ Code coverage > 80%
- ✅ Integration tests before deployment

### 3. Security
- ✅ Use Jenkins credentials manager
- ✅ Never hardcode passwords
- ✅ Scan Docker images for vulnerabilities

### 4. Performance
- ✅ Use parallel stages for independent tasks
- ✅ Cache Maven/npm dependencies
- ✅ Clean workspace after builds

---

## Troubleshooting

### Issue: Pipeline fails with "permission denied"

**Solution:**
```bash
# Fix script permissions
chmod +x deployment/scripts/*.sh
git add deployment/scripts/*.sh
git commit -m "Fix script permissions"
```

### Issue: Docker command not found in Jenkins

**Solution:**
```bash
# Verify Docker socket is mounted
docker exec jenkins-ci docker ps
```

### Issue: Tests fail in Jenkins but pass locally

**Solution:**
```bash
# Run tests with same environment
docker exec jenkins-ci bash -c "cd /workspace/user-service && mvn test"
```

### Issue: Build takes too long

**Solution:**
- Enable Maven dependency caching
- Use parallel stages
- Skip tests for non-critical builds

---

## Quick Commands Reference

```bash
# Start Jenkins
./deployment/start-jenkins.sh

# Stop Jenkins
./deployment/stop-jenkins.sh

# View Jenkins logs
docker logs -f jenkins-ci

# Get admin password
docker exec jenkins-ci cat /var/jenkins_home/secrets/initialAdminPassword

# Restart Jenkins
docker restart jenkins-ci

# Run health checks
./deployment/scripts/health-check.sh

# Manual rollback
./deployment/scripts/rollback.sh user-service 42

# Deploy specific version
./deployment/scripts/deploy.sh user-service 1.0.0 production
```

---

## Assessment Checklist

Your CI/CD setup will be assessed on:

- ✅ **Automated Code Fetching**: Pipeline fetches latest code from Git
- ✅ **Build Triggers**: Builds triggered automatically on commits
- ✅ **Automated Testing**: JUnit tests for backend, Jasmine/Karma for frontend
- ✅ **Test Failure Handling**: Pipeline fails when tests fail
- ✅ **Automated Deployment**: Successful builds deployed automatically
- ✅ **Rollback Strategy**: Automated rollback on deployment failure
- ✅ **Email Notifications**: Build status sent via email
- ✅ **Slack Notifications**: Real-time notifications to Slack
- ✅ **Parameterized Builds** (Bonus): Environment and options selectable
- ✅ **Distributed Builds** (Bonus): Jenkins agent configured

---

## Additional Resources

- [Jenkins Documentation](https://www.jenkins.io/doc/)
- [Jenkins Pipeline Syntax](https://www.jenkins.io/doc/book/pipeline/syntax/)
- [Blue Ocean User Guide](https://www.jenkins.io/doc/book/blueocean/)
- [Docker Pipeline Plugin](https://plugins.jenkins.io/docker-workflow/)

---

## Support

For issues or questions:
1. Check Jenkins console output: http://localhost:8090/job/[pipeline-name]/console
2. View logs: `docker logs jenkins-ci`
3. Review this documentation
4. Check official Jenkins documentation

---

**Happy CI/CD! 🚀**
