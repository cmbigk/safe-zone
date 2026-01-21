# SonarQube Bonus Features Guide
## Slack Notifications & IDE Integration

---

## Table of Contents
1. [Slack Integration](#1-slack-integration)
2. [Email Notifications](#2-email-notifications)
3. [VS Code IDE Integration](#3-vs-code-ide-integration)
4. [IntelliJ IDEA Integration](#4-intellij-idea-integration)
5. [SonarQube Webhooks](#5-sonarqube-webhooks)

---

## 1. Slack Integration

### 1.1 Setup Slack Workspace

#### Create Slack Workspace (if needed)
1. Go to https://slack.com/create
2. Create workspace: "ecommerce-devops"
3. Create channels:
   - `#sonarqube-alerts` - Quality gate notifications
   - `#code-quality` - General quality discussions
   - `#deployments` - Deployment notifications

### 1.2 Create Slack App

1. Go to https://api.slack.com/apps
2. Click **Create New App**
3. Choose **From scratch**
4. App Name: `SonarQube Notifier`
5. Workspace: Select your workspace
6. Click **Create App**

### 1.3 Configure Incoming Webhooks

1. In app settings, click **Incoming Webhooks**
2. Toggle **Activate Incoming Webhooks** to ON
3. Click **Add New Webhook to Workspace**
4. Select channel: `#sonarqube-alerts`
5. Click **Allow**
6. Copy webhook URL: `https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX`
7. **Important**: Save this securely!

### 1.4 Test Webhook

```bash
# Test Slack webhook
curl -X POST \
  -H 'Content-type: application/json' \
  --data '{
    "text": "🧪 SonarQube test notification",
    "blocks": [
      {
        "type": "section",
        "text": {
          "type": "mrkdwn",
          "text": "*SonarQube Test*\nWebhook is working! ✅"
        }
      }
    ]
  }' \
  YOUR_WEBHOOK_URL
```

### 1.5 Jenkins Slack Integration

#### Install Slack Plugin

1. Jenkins → **Manage Jenkins** → **Manage Plugins**
2. **Available** tab
3. Search: `Slack Notification Plugin`
4. Install and restart Jenkins

#### Configure Slack in Jenkins

1. **Manage Jenkins** → **Configure System**
2. Scroll to **Slack** section
3. Configure:
   - **Workspace**: ecommerce-devops (or your workspace name)
   - **Credential**: Add credential
     - Kind: **Secret text**
     - Secret: (paste webhook URL)
     - ID: `slack-webhook`
     - Description: `Slack Webhook for SonarQube`
   - **Default channel**: `#sonarqube-alerts`
4. **Test Connection**
5. Save

### 1.6 Update Jenkinsfile with Slack Notifications

Add to `Jenkinsfile`:

```groovy
pipeline {
    agent any
    
    environment {
        SLACK_CHANNEL = '#sonarqube-alerts'
        SONAR_PROJECT_KEY = 'ecommerce-api-gateway'
    }
    
    stages {
        // ... existing stages ...
        
        stage('SonarQube Analysis') {
            steps {
                script {
                    // Send notification that analysis started
                    slackSend(
                        channel: env.SLACK_CHANNEL,
                        color: '#439FE0',
                        message: """
                            🔍 *SonarQube Analysis Started*
                            Project: ${env.SONAR_PROJECT_KEY}
                            Build: #${env.BUILD_NUMBER}
                            Branch: ${env.GIT_BRANCH}
                            Commit: ${env.GIT_COMMIT_SHORT}
                        """
                    )
                }
                
                // Run analysis
                withSonarQubeEnv('SonarQube-Local') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    script {
                        def qg = waitForQualityGate()
                        env.QG_STATUS = qg.status
                    }
                }
            }
        }
    }
    
    post {
        success {
            script {
                slackSend(
                    channel: env.SLACK_CHANNEL,
                    color: 'good',
                    message: """
                        ✅ *Quality Gate PASSED*
                        Project: ${env.SONAR_PROJECT_KEY}
                        Build: <${env.BUILD_URL}|#${env.BUILD_NUMBER}>
                        Quality Gate: ${env.QG_STATUS}
                        
                        📊 <http://localhost:9000/dashboard?id=${env.SONAR_PROJECT_KEY}|View in SonarQube>
                    """,
                    blocks: [
                        [
                            type: 'section',
                            text: [
                                type: 'mrkdwn',
                                text: """*✅ Quality Gate PASSED*
                                
                                *Project:* ${env.SONAR_PROJECT_KEY}
                                *Build:* <${env.BUILD_URL}|#${env.BUILD_NUMBER}>
                                *Commit:* ${env.GIT_COMMIT_SHORT}
                                """
                            ]
                        ],
                        [
                            type: 'actions',
                            elements: [
                                [
                                    type: 'button',
                                    text: [
                                        type: 'plain_text',
                                        text: 'View in SonarQube'
                                    ],
                                    url: "http://localhost:9000/dashboard?id=${env.SONAR_PROJECT_KEY}"
                                ],
                                [
                                    type: 'button',
                                    text: [
                                        type: 'plain_text',
                                        text: 'View Build'
                                    ],
                                    url: env.BUILD_URL
                                ]
                            ]
                        ]
                    ]
                )
            }
        }
        
        failure {
            script {
                def failureReason = ''
                if (env.QG_STATUS && env.QG_STATUS != 'OK') {
                    failureReason = "Quality Gate Status: ${env.QG_STATUS}"
                } else {
                    failureReason = "Build or Test Failure"
                }
                
                slackSend(
                    channel: env.SLACK_CHANNEL,
                    color: 'danger',
                    message: """
                        ❌ *Quality Gate FAILED*
                        Project: ${env.SONAR_PROJECT_KEY}
                        Build: <${env.BUILD_URL}console|#${env.BUILD_NUMBER}>
                        Reason: ${failureReason}
                        Commit: ${env.GIT_COMMIT_SHORT}
                        
                        🔧 <http://localhost:9000/dashboard?id=${env.SONAR_PROJECT_KEY}|Fix Issues in SonarQube>
                    """,
                    blocks: [
                        [
                            type: 'section',
                            text: [
                                type: 'mrkdwn',
                                text: """*❌ Quality Gate FAILED*
                                
                                *Project:* ${env.SONAR_PROJECT_KEY}
                                *Build:* <${env.BUILD_URL}console|#${env.BUILD_NUMBER}>
                                *Reason:* ${failureReason}
                                *Author:* ${env.GIT_AUTHOR_NAME}
                                """
                            ]
                        ],
                        [
                            type: 'section',
                            text: [
                                type: 'mrkdwn',
                                text: '⚠️ *Action Required:* Review and fix issues before merging.'
                            ]
                        ],
                        [
                            type: 'actions',
                            elements: [
                                [
                                    type: 'button',
                                    text: [
                                        type: 'plain_text',
                                        text: 'View Issues'
                                    ],
                                    url: "http://localhost:9000/project/issues?id=${env.SONAR_PROJECT_KEY}",
                                    style: 'danger'
                                ],
                                [
                                    type: 'button',
                                    text: [
                                        type: 'plain_text',
                                        text: 'View Console'
                                    ],
                                    url: "${env.BUILD_URL}console"
                                ]
                            ]
                        ]
                    ]
                )
            }
        }
    }
}
```

### 1.7 Slack Message Examples

#### Success Message
```
✅ Quality Gate PASSED
Project: ecommerce-api-gateway
Build: #42
Quality Gate: OK

📊 View in SonarQube
```

#### Failure Message
```
❌ Quality Gate FAILED
Project: ecommerce-api-gateway
Build: #43
Reason: Quality Gate Status: ERROR
Commit: a1b2c3d

⚠️ Action Required: Review and fix issues before merging.

Issues Detected:
- 2 Bugs
- 1 Vulnerability
- Coverage: 65% (threshold: 80%)

🔧 Fix Issues in SonarQube
```

---

## 2. Email Notifications

### 2.1 Configure SMTP in SonarQube

**Navigate to**: Administration → Configuration → General Settings → Email

#### Gmail Configuration

```
Email prefix: [SONARQUBE]
From address: sonarqube@yourdomain.com
From name: SonarQube Code Quality
SMTP host: smtp.gmail.com
SMTP port: 587
SMTP username: your-email@gmail.com
SMTP password: (app password)
Secure connection: STARTTLS
```

#### Gmail App Password Setup

1. Go to Google Account settings
2. Security → 2-Step Verification
3. App passwords
4. Generate password for "SonarQube"
5. Use this password in SonarQube SMTP settings

### 2.2 Test Email Configuration

1. After configuring SMTP
2. Scroll down and enter test email
3. Click **Test configuration**
4. Check inbox for test email

### 2.3 Project-Level Notifications

**For each project**:

1. Navigate to project
2. **Project Settings** → **Notifications**
3. Click **Add notification**
4. Configure:
   - **Event**: Quality gate status changed
   - **Type**: Email
   - **Recipients**: dev-team@example.com

#### Available Events

- **Quality gate status changed** - When quality gate passes/fails
- **New issues** - When new issues are detected
- **Issues changed** - When issue severity/status changes
- **New security hotspots** - When security hotspots detected

### 2.4 User-Level Notifications

Users can configure personal notifications:

1. Profile → **My Account**
2. **Notifications** tab
3. Configure per project:
   - New issues assigned to me
   - Issues changed on issues assigned to me
   - Quality gate status changes

### 2.5 Email Template Example

**Subject**: [SONARQUBE] Quality Gate Failed - ecommerce-api-gateway

**Body**:
```
Quality Gate Status: FAILED

Project: ecommerce-api-gateway
Analysis Date: 2026-01-21 14:30:00

Issues:
- Bugs: 2 (threshold: 0)
- Vulnerabilities: 1 (threshold: 0)
- Code Coverage: 65% (threshold: 80%)

View details: http://localhost:9000/dashboard?id=ecommerce-api-gateway

Please fix the issues before merging your changes.
```

---

## 3. VS Code IDE Integration

### 3.1 Install SonarLint Extension

1. Open VS Code
2. Extensions (Ctrl+Shift+X / Cmd+Shift+X)
3. Search: `SonarLint`
4. Install: **SonarLint by SonarSource**
5. Reload VS Code

### 3.2 Connect to SonarQube Server

#### Generate User Token

1. Open SonarQube: http://localhost:9000
2. Profile icon → **My Account**
3. **Security** tab
4. **Generate Token**:
   - Name: `vscode-integration`
   - Type: User Token
   - Expires: No expiration (or 90 days)
5. Click **Generate**
6. Copy token immediately

#### Configure in VS Code

**Option 1: Via Settings UI**

1. File → Preferences → Settings (Ctrl+,)
2. Search: `SonarLint`
3. Click **Edit in settings.json**

**Option 2: Direct settings.json**

Create/edit `.vscode/settings.json` in project root:

```json
{
  "sonarlint.connectedMode.connections.sonarqube": [
    {
      "serverUrl": "http://localhost:9000",
      "token": "squ_your_token_here"
    }
  ],
  "sonarlint.connectedMode.project": {
    "projectKey": "ecommerce-api-gateway"
  },
  "sonarlint.rules": {
    "java:S1192": {
      "level": "on"
    },
    "java:S2068": {
      "level": "on"
    }
  }
}
```

**Important**: Add `.vscode/settings.json` to `.gitignore` if it contains token!

### 3.3 Configure for Each Service

Create workspace settings (recommended over user settings):

**For API Gateway**:
```json
{
  "sonarlint.connectedMode.connections.sonarqube": [
    {
      "serverUrl": "http://localhost:9000",
      "token": "squ_your_token_here"
    }
  ],
  "sonarlint.connectedMode.project": {
    "projectKey": "ecommerce-api-gateway"
  }
}
```

**For Frontend**:
```json
{
  "sonarlint.connectedMode.connections.sonarqube": [
    {
      "serverUrl": "http://localhost:9000",
      "token": "squ_your_token_here"
    }
  ],
  "sonarlint.connectedMode.project": {
    "projectKey": "ecommerce-frontend"
  }
}
```

### 3.4 Using SonarLint in VS Code

#### Real-Time Analysis

1. Open any Java/TypeScript file
2. Issues highlighted with squiggly lines
3. Hover over issue to see:
   - Rule description
   - Why it's a problem
   - How to fix

#### Issue Severity Colors

- 🔴 **Red**: Blocker/Critical (Bugs, Vulnerabilities)
- 🟡 **Yellow**: Major/Minor (Code Smells)
- 🔵 **Blue**: Info

#### View All Issues

1. Open **Problems** panel (Ctrl+Shift+M / Cmd+Shift+M)
2. Filter by SonarLint
3. Click issue to jump to location

#### Quick Fixes

1. Click on issue
2. Lightbulb icon appears
3. Click for suggested fixes
4. Apply fix automatically (when available)

### 3.5 SonarLint Commands

Open Command Palette (Ctrl+Shift+P / Cmd+Shift+P):

- `SonarLint: Update bindings to SonarQube` - Sync rules
- `SonarLint: Show SonarLint output` - View logs
- `SonarLint: Clear SonarLint issues` - Clear cache

### 3.6 Exclude Files from Analysis

In `.vscode/settings.json`:

```json
{
  "sonarlint.pathToCompileCommands": "${workspaceFolder}/compile_commands.json",
  "sonarlint.disableTelemetry": true,
  "sonarlint.output.showAnalyzerLogs": false,
  "files.exclude": {
    "**/node_modules": true,
    "**/dist": true,
    "**/target": true
  }
}
```

---

## 4. IntelliJ IDEA Integration

### 4.1 Install SonarLint Plugin

1. **File** → **Settings** (Ctrl+Alt+S)
2. **Plugins**
3. **Marketplace** tab
4. Search: `SonarLint`
5. Install **SonarLint by SonarSource**
6. Restart IntelliJ IDEA

### 4.2 Connect to SonarQube

1. **File** → **Settings** → **Tools** → **SonarLint**
2. Click **+** (Add Connection)
3. Select **SonarQube**
4. Configure:
   - **Name**: Local SonarQube
   - **Server URL**: http://localhost:9000
5. Click **Next**

#### Authentication

1. Select **Token**
2. Click **Create token**
3. Opens SonarQube in browser
4. Generate token: `intellij-integration`
5. Copy token
6. Paste in IntelliJ
7. Click **Next**

#### Bind Projects

1. Select projects to bind
2. Map to SonarQube projects:
   - `api-gateway` → `ecommerce-api-gateway`
   - `user-service` → `ecommerce-user-service`
   - etc.
3. Click **Finish**

### 4.3 Using SonarLint in IntelliJ

#### Real-Time Analysis

- Issues shown with colored underlines
- Gutter icons indicate severity
- Hover for description

#### SonarLint Tool Window

1. **View** → **Tool Windows** → **SonarLint**
2. Shows:
   - Current file issues
   - Rule details
   - Quick fixes

#### Analyze on Demand

- Right-click file/folder
- **Analyze** → **Analyze with SonarLint**

### 4.4 Configure Rule Severity

1. **Settings** → **Tools** → **SonarLint** → **Rules**
2. Search for rule (e.g., S2068)
3. Change severity
4. Apply

---

## 5. SonarQube Webhooks

### 5.1 What Are Webhooks?

Webhooks notify external systems when analysis completes.

**Use cases**:
- Custom notification systems
- Integration with other tools
- Automated workflows
- Metrics dashboards

### 5.2 Configure Webhook in SonarQube

**Navigate to**: Administration → Configuration → Webhooks

1. Click **Create**
2. Configure:
   - **Name**: Jenkins Callback
   - **URL**: http://jenkins:8080/sonarqube-webhook/
   - **Secret**: (optional but recommended)
3. Click **Create**

### 5.3 Webhook Payload

When analysis completes, SonarQube sends POST request:

```json
{
  "serverUrl": "http://localhost:9000",
  "taskId": "AWi3xL-1d4x",
  "status": "SUCCESS",
  "analysedAt": "2026-01-21T14:30:00+0000",
  "project": {
    "key": "ecommerce-api-gateway",
    "name": "E-Commerce API Gateway",
    "url": "http://localhost:9000/dashboard?id=ecommerce-api-gateway"
  },
  "properties": {},
  "qualityGate": {
    "name": "E-Commerce-Quality-Gate",
    "status": "OK",
    "conditions": [
      {
        "metric": "new_coverage",
        "operator": "LESS_THAN",
        "value": "85",
        "status": "OK",
        "errorThreshold": "80"
      }
    ]
  }
}
```

### 5.4 Custom Webhook Receiver

Create a simple webhook receiver to test:

```python
# webhook_receiver.py
from flask import Flask, request, jsonify

app = Flask(__name__)

@app.route('/sonarqube-webhook', methods=['POST'])
def sonarqube_webhook():
    data = request.json
    
    project = data['project']['name']
    status = data['qualityGate']['status']
    
    print(f"Received webhook for {project}")
    print(f"Quality Gate: {status}")
    
    if status == 'OK':
        print("✅ Quality gate passed!")
        # Send to Slack, update dashboard, etc.
    else:
        print("❌ Quality gate failed!")
        # Alert team
    
    return jsonify({"status": "received"}), 200

if __name__ == '__main__':
    app.run(port=5000)
```

Run:
```bash
pip install flask
python webhook_receiver.py
```

---

## 6. Advanced Notifications

### 6.1 Microsoft Teams Integration

Similar to Slack, but for Teams:

1. Create incoming webhook in Teams channel
2. Use similar message format
3. Update Jenkinsfile with Teams webhook

### 6.2 Discord Integration

For Discord communities:

1. Server Settings → Integrations → Webhooks
2. Create webhook
3. Use webhook URL in notifications

### 6.3 Custom Dashboard

Create a dashboard that shows all projects:

```html
<!DOCTYPE html>
<html>
<head>
    <title>SonarQube Dashboard</title>
    <script>
        async function fetchMetrics() {
            const projects = [
                'ecommerce-api-gateway',
                'ecommerce-user-service',
                'ecommerce-product-service',
                'ecommerce-media-service',
                'ecommerce-frontend'
            ];
            
            for (const project of projects) {
                const response = await fetch(
                    `http://localhost:9000/api/measures/component?component=${project}&metricKeys=bugs,vulnerabilities,code_smells,coverage`
                );
                const data = await response.json();
                // Display metrics
            }
        }
    </script>
</head>
<body onload="fetchMetrics()">
    <h1>E-Commerce Code Quality Dashboard</h1>
    <div id="metrics"></div>
</body>
</html>
```

---

## 7. Testing Your Integration

### 7.1 Test Checklist

- [ ] Slack notification on success
- [ ] Slack notification on failure
- [ ] Email notification configured
- [ ] VS Code shows issues in real-time
- [ ] IntelliJ shows issues in real-time
- [ ] Webhook triggers correctly

### 7.2 Test Slack Notifications

```bash
# Trigger successful build
git commit --allow-empty -m "test: Slack success notification"
git push

# Trigger failed build (add intentional issue)
echo 'String password = "test123";' >> api-gateway/src/main/java/Test.java
git add .
git commit -m "test: Slack failure notification"
git push
```

Check Slack channel for notifications.

### 7.3 Test IDE Integration

1. Open file in VS Code/IntelliJ
2. Add code with issue:
   ```java
   String password = "hardcoded"; // Should be detected
   ```
3. Verify issue appears immediately
4. Hover to see description
5. Remove code

---

## 8. Screenshots for Audit

### Required Screenshots

1. **Slack Integration**:
   - Slack workspace showing #sonarqube-alerts channel
   - Success notification message
   - Failure notification message
   - Jenkinsfile showing slackSend code

2. **Email Integration**:
   - SMTP configuration in SonarQube
   - Test email in inbox
   - Notification settings for project

3. **VS Code Integration**:
   - SonarLint extension installed
   - Code with issues highlighted
   - Hover showing rule description
   - Problems panel showing SonarLint issues
   - settings.json with configuration

4. **IntelliJ Integration** (if applicable):
   - SonarLint plugin installed
   - Connection to SonarQube configured
   - Real-time issue detection

---

## 9. Troubleshooting

### Slack Notifications Not Appearing

```bash
# Check webhook URL is correct
curl -X POST YOUR_WEBHOOK_URL \
  -H 'Content-type: application/json' \
  -d '{"text":"Test"}'

# Check Jenkins Slack plugin installed
# Check Slack credentials in Jenkins

# Check Jenkinsfile has slackSend steps
grep -r "slackSend" Jenkinsfile
```

### VS Code Not Showing Issues

```bash
# Check SonarLint output
# Command Palette: "SonarLint: Show SonarLint output"

# Verify connection
# settings.json should have correct serverUrl and token

# Update bindings
# Command Palette: "SonarLint: Update bindings to SonarQube"
```

### Email Not Sending

```bash
# Test SMTP settings in SonarQube
# Administration → Email → Test configuration

# Check firewall/network allows SMTP port 587
# Check Gmail app password is correct
```

---

## Summary

This bonus features guide provides:
✅ Slack integration for real-time notifications  
✅ Email notifications for team alerts  
✅ VS Code integration for real-time code analysis  
✅ IntelliJ IDEA integration  
✅ Webhook configuration for custom integrations  

**For Audit**: Demonstrate at least one of these integrations with screenshots and live demo.

**Recommended**: Focus on Slack + VS Code for maximum impact in presentation.
