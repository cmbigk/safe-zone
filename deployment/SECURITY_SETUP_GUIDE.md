# Jenkins Security Setup Guide

**Priority: CRITICAL** - Complete these steps before production deployment

---

## 1. Change Admin Password (IMMEDIATE)

### Method 1: Via Jenkins UI (Recommended)
```bash
1. Navigate to: http://localhost:8090
2. Login with: admin / admin123
3. Click: admin (top right) → Configure
4. Scroll to: Password section
5. Enter new password (minimum 12 characters, mix of upper/lower/numbers/symbols)
6. Click: Save
```

### Method 2: Via Script
```bash
# Run this script from deployment directory
cd /Users/chan.myint/Desktop/jenkins/deployment
./scripts/change-admin-password.sh
```

**Recommended Password Requirements:**
- Minimum 16 characters
- Mix of uppercase, lowercase, numbers, special characters
- Not containing "admin", "jenkins", or common words
- Use a password manager

---

## 2. Configure Secrets Management

### Step 1: Add GitHub Credentials

```bash
# Navigate to Jenkins
http://localhost:8090

# Go to: Manage Jenkins → Manage Credentials → (global) → Add Credentials

# Fill in:
Kind: Username with password
Scope: Global
Username: your-github-username
Password: your-github-personal-access-token
ID: github-credentials
Description: GitHub credentials for repository access
```

**Generate GitHub Token:**
1. Go to: https://github.com/settings/tokens
2. Click: Generate new token (classic)
3. Select scopes: `repo`, `workflow`
4. Copy token and use as password above

### Step 2: Add Slack Token (if using Slack notifications)

```bash
# In Jenkins: Manage Jenkins → Manage Credentials → Add Credentials

Kind: Secret text
Scope: Global
Secret: xoxb-your-slack-bot-token
ID: slack-token
Description: Slack webhook token for notifications
```

**Get Slack Token:**
1. Go to: https://api.slack.com/apps
2. Create app or select existing
3. Go to: OAuth & Permissions
4. Copy: Bot User OAuth Token
5. Add bot to #deployments channel

### Step 3: Add Database Credentials (when needed)

```bash
# For each service that needs database access

Kind: Username with password
Scope: Global
Username: db_user
Password: secure_database_password
ID: database-password
Description: Production database credentials
```

### Step 4: Update Jenkinsfile to Use Credentials

The Jenkinsfile has been updated with placeholders. Uncomment and configure:

```groovy
environment {
    // Uncomment when credentials are added:
    // GITHUB_CREDS = credentials('github-credentials')
    // DB_PASSWORD = credentials('database-password')
    // API_KEY = credentials('api-key-secret')
}

steps {
    // Use credentials in commands:
    sh '''
        git config credential.helper store
        echo "https://${GITHUB_CREDS_USR}:${GITHUB_CREDS_PSW}@github.com" > ~/.git-credentials
    '''
    
    // Pass to containers:
    sh """
        docker run -d --name service \
            -e DB_PASSWORD=${DB_PASSWORD} \
            -e API_KEY=${API_KEY} \
            ecommerce/service:${BUILD_NUMBER}
    """
}
```

---

## 3. Configure Email Notifications

### Step 1: Configure SMTP Server

```bash
# Navigate to: Manage Jenkins → Configure System
# Scroll to: Extended E-mail Notification

SMTP server: smtp.gmail.com
SMTP port: 587
Use SMTP Authentication: ✓
User Name: your-email@gmail.com
Password: your-app-password
Use SSL: ✓
Charset: UTF-8
```

**Gmail Setup:**
1. Enable 2-factor authentication on your Google account
2. Generate App Password: https://myaccount.google.com/apppasswords
3. Use the generated 16-character password above

**Alternative SMTP Providers:**
- **AWS SES:** smtp.us-east-1.amazonaws.com:587
- **SendGrid:** smtp.sendgrid.net:587
- **Mailgun:** smtp.mailgun.org:587

### Step 2: Set Default Email Recipients

```bash
# In Jenkins → Configure System → Extended E-mail Notification
Default Recipients: team@example.com, devops@example.com
```

### Step 3: Configure Environment Variable

```bash
# In Jenkins → Manage Jenkins → Configure System → Global properties
# Check: Environment variables
# Add:
Name: BUILD_NOTIFICATION_EMAIL
Value: team@example.com
```

---

## 4. Configure Slack Notifications

### Step 1: Install Slack Plugin

```bash
# Navigate to: Manage Jenkins → Manage Plugins → Available
# Search: "Slack Notification Plugin"
# Check box → Install without restart
```

### Step 2: Configure Slack Integration

```bash
# Navigate to: Manage Jenkins → Configure System → Slack

Workspace: your-workspace-name
Credential: Select "slack-token" (created in Step 2.2 above)
Default channel: #deployments
Custom message: (optional)
Test Connection: Click to verify
```

### Step 3: Set Environment Variables

```bash
# In Jenkins → Manage Jenkins → Configure System → Global properties
# Add:
Name: SLACK_CHANNEL
Value: #deployments

Name: SLACK_TEAM
Value: your-team-domain
```

---

## 5. Test Notifications

### Test Email:
```bash
# Trigger a build manually
# Check console output for:
✅ Email notification sent successfully
OR
⚠️ Email notification failed: [error details]

# If failed, check:
1. SMTP configuration correct
2. Credentials valid
3. Firewall allows port 587
```

### Test Slack:
```bash
# Trigger a build manually
# Check console output for:
✅ Slack notification sent successfully
OR
⚠️ Slack notification failed: [error details]

# If failed, check:
1. Bot token valid
2. Bot added to channel
3. Channel name correct (include #)
```

---

## 6. Verify Frontend Test Reports

### Test Report Generation:

```bash
# Run frontend tests locally
cd /Users/chan.myint/Desktop/jenkins/frontend
npm run test:ci

# Check for XML output
ls -la test-results/
# Should see: test-results.xml

# If not created:
cat test-results.xml
# Should contain: <testsuite> and <testcase> elements
```

### Verify in Jenkins:

```bash
# After next build completes:
1. Go to: Build #XX → Test Result
2. Should see: Frontend Unit Tests
3. Click to expand: Should show 13 tests
4. Verify all tests listed with pass/fail status
```

---

## 7. Security Checklist

Before considering Jenkins production-ready, verify:

- [ ] **Admin password changed** from default "admin123"
- [ ] **GitHub credentials** added to Jenkins Credentials Store
- [ ] **Slack token** added (if using Slack)
- [ ] **Database passwords** added (when needed)
- [ ] **SMTP configured** for email notifications
- [ ] **Test email** sent successfully
- [ ] **Test Slack message** sent successfully
- [ ] **Jenkinsfile updated** to use credentials() function
- [ ] **No plaintext passwords** in any configuration files
- [ ] **SSL/TLS enabled** for Jenkins (production only)
- [ ] **Firewall rules** configured (production only)
- [ ] **Backup strategy** documented and tested
- [ ] **Audit logging** enabled

---

## 8. Additional Security Hardening (Recommended)

### Enable HTTPS for Jenkins:

```bash
# Edit docker-compose.jenkins.yml
services:
  jenkins:
    environment:
      - JENKINS_OPTS=--httpPort=-1 --httpsPort=8443
    ports:
      - "8443:8443"
    volumes:
      - ./ssl/jenkins.jks:/var/jenkins_home/jenkins.jks
```

### Enable Audit Trail:

```bash
# Install Audit Trail Plugin
# Configure: Manage Jenkins → Configure System → Audit Trail
Log location: /var/jenkins_home/logs/audit.log
Log file count: 10
Log file size: 10 MB
```

### Regular Security Updates:

```bash
# Check for updates weekly
Manage Jenkins → Manage Plugins → Updates

# Update Jenkins core
Manage Jenkins → System → Preparation for Shutdown
# Wait for jobs to finish
# Restart Jenkins
```

---

## 9. Monitoring and Alerts

### Set Up Monitoring:

```bash
# Monitor Jenkins health
http://localhost:8090/monitoring

# Key metrics to track:
- Build queue length
- Executor utilization
- Disk space
- Memory usage
- Failed builds per day
```

### Alert Thresholds:

```groovy
// Add to pipeline for critical failures
post {
    failure {
        script {
            if (env.ENVIRONMENT == 'production') {
                // Send urgent notification
                slackSend(
                    color: 'danger',
                    message: "@here PRODUCTION BUILD FAILED! Immediate attention required.",
                    channel: '#incidents'
                )
            }
        }
    }
}
```

---

## Quick Reference Commands

```bash
# View Jenkins logs
docker logs jenkins-ci -f

# Restart Jenkins
docker restart jenkins-ci

# Backup Jenkins configuration
docker exec jenkins-ci tar czf /tmp/jenkins-backup.tar.gz /var/jenkins_home
docker cp jenkins-ci:/tmp/jenkins-backup.tar.gz ./backups/

# Restore Jenkins configuration
docker cp ./backups/jenkins-backup.tar.gz jenkins-ci:/tmp/
docker exec jenkins-ci tar xzf /tmp/jenkins-backup.tar.gz -C /

# Check Jenkins version
docker exec jenkins-ci cat /var/jenkins_home/jenkins.version

# List installed plugins
docker exec jenkins-ci ls /var/jenkins_home/plugins/
```

---

## Support and Troubleshooting

### Common Issues:

**Email not sending:**
- Check SMTP settings and credentials
- Verify firewall allows outbound port 587
- Test with telnet: `telnet smtp.gmail.com 587`

**Slack not working:**
- Verify bot token in credentials
- Check bot is added to channel
- Test webhook manually

**Frontend tests not reported:**
- Check karma.conf.js configuration
- Verify test-results directory created
- Check junit-reporter installed

### Getting Help:

- Jenkins Documentation: https://www.jenkins.io/doc/
- Security Best Practices: https://www.jenkins.io/doc/book/security/
- Community Forum: https://community.jenkins.io/

---

## Completion

Once all steps above are completed:

1. ✅ Run security checklist verification
2. ✅ Test all notification channels
3. ✅ Trigger test build and verify all stages
4. ✅ Document credentials location (password manager)
5. ✅ Share documentation with team

**Your Jenkins instance is now production-ready!** 🚀
