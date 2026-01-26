# 🎯 Step-by-Step Guide: Complete Your Audit Requirements

**Current Status**: ✅ SonarQube is running, projects are analyzed  
**What's Missing**: GitHub auto-trigger, Slack notifications (bonus)  
**Time Required**: 30-45 minutes

---

## 📋 What You Have vs What You Need

### ✅ Already Done
- [x] SonarQube running in Docker (localhost:9000)
- [x] All 5 services analyzed in SonarQube
- [x] Complete documentation created
- [x] GitHub Actions workflow files exist
- [x] Jenkinsfiles with SonarQube stages exist
- [x] PR template, CODEOWNERS files exist

### ❌ Still Need To Do
- [ ] **CRITICAL**: Set up GitHub Actions auto-trigger (Required for audit)
- [ ] **BONUS**: Set up Slack notifications (Optional but impressive)
- [ ] **VERIFY**: Test the complete flow

---

## 🚀 STEP 1: Set Up GitHub Actions Auto-Trigger (REQUIRED)

**Why**: Audit requires code analysis to trigger automatically on every Git push

### Step 1.1: Generate SonarQube Tokens

1. **Open SonarQube**: http://localhost:9000
2. **Login** with your credentials (admin/your-password)
3. **Generate tokens for each project**:

   **For API Gateway**:
   - Click profile icon (top-right) → **My Account**
   - Click **Security** tab
   - In "Generate Tokens" section:
     - **Name**: `github-actions-api-gateway`
     - **Type**: Project Analysis Token
     - **Project**: ecommerce-api-gateway
     - **Expires in**: 90 days (or No expiration)
   - Click **Generate**
   - **COPY THE TOKEN IMMEDIATELY** (you can't see it again!)
   - Example: `squ_abc123def456...`

   **Repeat for other services**:
   - `github-actions-user-service` → ecommerce-user-service
   - `github-actions-product-service` → ecommerce-product-service
   - `github-actions-media-service` → ecommerce-media-service
   - `github-actions-frontend` → ecommerce-frontend

   **Save all 5 tokens in a text file temporarily** (you'll need them in the next step)

### Step 1.2: Expose SonarQube to GitHub (Using ngrok)

**Problem**: GitHub Actions runs in the cloud but your SonarQube is on localhost:9000

**Solution**: Use ngrok to create a temporary public URL

1. **Install ngrok** (if not already installed):
   ```bash
   # macOS
   brew install ngrok
   
   # Or download from: https://ngrok.com/download
   ```

2. **Create ngrok account** (free):
   - Go to: https://dashboard.ngrok.com/signup
   - Sign up with email or GitHub
   - Copy your auth token from: https://dashboard.ngrok.com/get-started/your-authtoken

3. **Configure ngrok**:
   ```bash
   ngrok config add-authtoken YOUR_AUTH_TOKEN_HERE
   ```

4. **Start ngrok tunnel**:
   ```bash
   ngrok http 9000
   ```

5. **Copy the HTTPS URL** from ngrok output:
   ```
   Forwarding    https://abc123def456.ngrok-free.app -> http://localhost:9000
                 ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
                 COPY THIS URL!
   ```

   **Important**: Keep this terminal window open! The tunnel will stay active.

### Step 1.3: Configure GitHub Secrets

1. **Go to your GitHub repository**:
   - https://github.com/cmbigk/safe-zone

2. **Navigate to Settings**:
   - Click **Settings** tab
   - Click **Secrets and variables** → **Actions**
   - Click **New repository secret**

3. **Add these secrets one by one**:

   | Secret Name | Value |
   |------------|-------|
   | `SONAR_HOST_URL` | The ngrok URL (e.g., `https://abc123.ngrok-free.app`) |
   | `SONAR_TOKEN_API_GATEWAY` | Token for api-gateway (from Step 1.1) |
   | `SONAR_TOKEN_USER_SERVICE` | Token for user-service (from Step 1.1) |
   | `SONAR_TOKEN_PRODUCT_SERVICE` | Token for product-service (from Step 1.1) |
   | `SONAR_TOKEN_MEDIA_SERVICE` | Token for media-service (from Step 1.1) |
   | `SONAR_TOKEN_FRONTEND` | Token for frontend (from Step 1.1) |

   **How to add each secret**:
   - Click "New repository secret"
   - Name: (copy from table above)
   - Value: (paste the token)
   - Click "Add secret"

### Step 1.4: Test GitHub Actions Auto-Trigger

1. **Make a small change to trigger the workflow**:
   ```bash
   cd /Users/chan.myint/Desktop/safe-zone
   
   # Add a comment to README
   echo "" >> README.md
   echo "<!-- Audit test - $(date) -->" >> README.md
   
   # Commit and push
   git add README.md
   git commit -m "test: Trigger GitHub Actions for audit demo"
   git push origin main
   ```

2. **Watch GitHub Actions**:
   - Go to: https://github.com/cmbigk/safe-zone/actions
   - You should see workflows running:
     - "SonarQube Analysis - Java Services"
     - "SonarQube Analysis - Frontend"

3. **Verify in SonarQube**:
   - Go to: http://localhost:9000
   - Click on any project
   - Click "Activity" tab
   - You should see a new analysis with the commit hash

**Expected Result**: ✅ Workflows run automatically, SonarQube shows new analysis

---

## 🎁 STEP 2: Set Up Slack Notifications (BONUS - Optional)

**Why**: Shows extra effort, impressive for audit, but not required

### Step 2.1: Create Slack Workspace (5 minutes)

1. **Go to**: https://slack.com/create
2. **Create workspace**:
   - Name: `ecommerce-devops` (or your choice)
   - Skip inviting team members for now
3. **Create channel**:
   - Click ➕ next to "Channels"
   - Name: `sonarqube-alerts`
   - Make it public
   - Click "Create"

### Step 2.2: Create Slack App and Webhook (5 minutes)

1. **Go to**: https://api.slack.com/apps
2. **Click**: "Create New App"
3. **Choose**: "From scratch"
4. **Fill in**:
   - App Name: `SonarQube Notifier`
   - Workspace: Select your workspace
   - Click "Create App"

5. **Enable Incoming Webhooks**:
   - In left sidebar: Click "Incoming Webhooks"
   - Toggle "Activate Incoming Webhooks" to **ON**
   - Click "Add New Webhook to Workspace"
   - Select channel: `#sonarqube-alerts`
   - Click "Allow"

6. **Copy the Webhook URL**:
   ```
   https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXX
   ```
   **Save this URL!**

### Step 2.3: Add Slack Webhook to GitHub (3 minutes)

1. **Go to GitHub repository**: Settings → Secrets and variables → Actions
2. **Add new secret**:
   - Name: `SLACK_WEBHOOK_URL`
   - Value: (paste the webhook URL from Step 2.2)
   - Click "Add secret"

### Step 2.4: Update GitHub Actions Workflows (5 minutes)

You need to add Slack notification steps to your workflows.

**Option A: I can create updated workflow files for you**

**Option B: Manual update** - Add this to the end of each workflow file:

For `.github/workflows/sonarqube-java.yml`, add at the end:
```yaml
  notify:
    name: Slack Notification
    needs: [sonarqube-analysis]
    runs-on: ubuntu-latest
    if: always()
    steps:
      - name: 📢 Notify Success
        if: needs.sonarqube-analysis.result == 'success'
        run: |
          curl -X POST ${{ secrets.SLACK_WEBHOOK_URL }} \
            -H 'Content-Type: application/json' \
            -d '{
              "text": "✅ *SonarQube Analysis PASSED*",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*✅ Quality Gate PASSED*\n*Project:* Java Services\n*Commit:* ${{ github.sha }}\n*Author:* ${{ github.actor }}"
                  }
                },
                {
                  "type": "actions",
                  "elements": [
                    {
                      "type": "button",
                      "text": {
                        "type": "plain_text",
                        "text": "View in SonarQube"
                      },
                      "url": "${{ secrets.SONAR_HOST_URL }}"
                    }
                  ]
                }
              ]
            }'
      
      - name: 📢 Notify Failure
        if: needs.sonarqube-analysis.result == 'failure'
        run: |
          curl -X POST ${{ secrets.SLACK_WEBHOOK_URL }} \
            -H 'Content-Type: application/json' \
            -d '{
              "text": "❌ *SonarQube Analysis FAILED*",
              "blocks": [
                {
                  "type": "section",
                  "text": {
                    "type": "mrkdwn",
                    "text": "*❌ Quality Gate FAILED*\n*Project:* Java Services\n*Commit:* ${{ github.sha }}\n*Author:* ${{ github.actor }}\n\n⚠️ *Action Required:* Fix issues before merging."
                  }
                },
                {
                  "type": "actions",
                  "elements": [
                    {
                      "type": "button",
                      "text": {
                        "type": "plain_text",
                        "text": "View Issues"
                      },
                      "url": "${{ secrets.SONAR_HOST_URL }}",
                      "style": "danger"
                    }
                  ]
                }
              ]
            }'
```

### Step 2.5: Test Slack Notifications

1. **Make a test commit**:
   ```bash
   echo "<!-- Slack test - $(date) -->" >> README.md
   git add README.md
   git commit -m "test: Test Slack notifications"
   git push origin main
   ```

2. **Check Slack**:
   - Open your Slack workspace
   - Go to `#sonarqube-alerts` channel
   - You should see a message when the workflow completes

**Expected Result**: ✅ Message appears in Slack channel

---

## ✅ STEP 3: Verify Everything Works (CRITICAL)

### Step 3.1: Run Verification Script

```bash
cd /Users/chan.myint/Desktop/safe-zone
./audit-verification.sh
```

**Expected**: 100% pass rate

### Step 3.2: Test Complete Flow

1. **Make a meaningful change**:
   ```bash
   # Edit a Java file
   echo "// Audit demo change" >> api-gateway/src/main/java/com/example/apigateway/ApiGatewayApplication.java
   
   git add .
   git commit -m "feat: Add audit demo comment"
   git push origin main
   ```

2. **Verify each step**:
   - [ ] GitHub Actions runs automatically
   - [ ] SonarQube receives the analysis
   - [ ] Quality Gate is checked
   - [ ] New analysis appears in SonarQube dashboard
   - [ ] (If configured) Slack notification received

### Step 3.3: Document Success

Take screenshots for audit:
- [ ] GitHub Actions showing successful workflow
- [ ] SonarQube dashboard with all 5 projects
- [ ] Recent analysis in SonarQube with commit hash
- [ ] (Optional) Slack notification

---

## 📸 What to Show Auditors

### Requirement 1: SonarQube Accessible
**Show**: Browser at localhost:9000, dashboard with 5 projects

### Requirement 2: GitHub Integration with Auto-Trigger ⚠️ CRITICAL
**Show**: 
1. GitHub Actions tab with workflow runs
2. Workflow details showing SonarQube scan
3. SonarQube dashboard showing analysis triggered by Git push
4. Matching commit hashes between GitHub and SonarQube

### Requirement 3: Docker-Based Setup
**Show**: `docker ps` output showing sonarqube containers

### Requirement 4: Automated Analysis with Quality Gate
**Show**: 
1. Jenkinsfile or GitHub Actions workflow with Quality Gate stage
2. Build logs showing Quality Gate check
3. Example of failed Quality Gate stopping deployment

### Requirement 5: Code Review Process
**Show**: 
1. .github/PULL_REQUEST_TEMPLATE.md with SonarQube section
2. .github/CODEOWNERS file
3. Sample PR with template populated

### Requirement 6-8: Documentation
**Show**: All documentation files exist and are comprehensive

### Bonus: Notifications
**Show**: Slack channel with notifications (if configured)

---

## 🚨 Troubleshooting

### Problem: GitHub Actions fails with "Connection refused"
**Solution**: 
- Check ngrok is still running
- Verify `SONAR_HOST_URL` secret has correct ngrok URL
- ngrok URLs expire after 2 hours on free plan - restart if needed

### Problem: "Invalid authentication token"
**Solution**:
- Regenerate tokens in SonarQube
- Update GitHub secrets with new tokens

### Problem: Quality Gate never completes
**Solution**:
- Check SonarQube Compute Engine is running
- Administration → System → Compute Engine
- Restart SonarQube if needed

### Problem: Slack notifications not working
**Solution**:
- Test webhook URL manually:
  ```bash
  curl -X POST YOUR_WEBHOOK_URL \
    -H 'Content-Type: application/json' \
    -d '{"text": "Test message"}'
  ```
- Check `SLACK_WEBHOOK_URL` secret is correct
- Verify workflow has notification step

---

## ⏱️ Timeline for Completion

| Task | Time | Priority |
|------|------|----------|
| Generate SonarQube tokens | 5 min | CRITICAL |
| Set up ngrok | 10 min | CRITICAL |
| Configure GitHub secrets | 5 min | CRITICAL |
| Test auto-trigger | 5 min | CRITICAL |
| **REQUIRED SUBTOTAL** | **25 min** | |
| Set up Slack workspace | 5 min | BONUS |
| Create Slack webhook | 5 min | BONUS |
| Update workflows for Slack | 10 min | BONUS |
| Test Slack notifications | 5 min | BONUS |
| **BONUS SUBTOTAL** | **25 min** | |
| **TOTAL** | **50 min** | |

---

## 📝 Quick Checklist

Before audit, verify:
- [ ] SonarQube is running (`docker ps`)
- [ ] All 5 projects visible in SonarQube
- [ ] ngrok tunnel is active (if using GitHub Actions)
- [ ] GitHub secrets configured (6 secrets minimum)
- [ ] Made test commit that triggers workflow
- [ ] GitHub Actions shows successful workflow run
- [ ] SonarQube shows new analysis with correct commit hash
- [ ] (Bonus) Slack notifications working
- [ ] Verification script shows 100% pass: `./audit-verification.sh`
- [ ] Screenshots taken of all evidence
- [ ] Reviewed AUDIT_COMPLIANCE_REPORT.md

---

## 🎯 Which Option Should You Choose?

### Option 1: Minimum for Audit Pass (25 minutes)
- Do Step 1 only (GitHub auto-trigger)
- Skip Slack notifications
- **Result**: Pass all required audit requirements

### Option 2: Impress the Auditors (50 minutes)
- Do Step 1 (GitHub auto-trigger)
- Do Step 2 (Slack notifications)
- **Result**: Pass all requirements + bonus points

### My Recommendation: 
**Do Option 1 first (25 min) → Verify it works → Then add Option 2 if time permits**

---

## 🆘 Need Help?

If you get stuck on any step:

1. **Check the detailed guides**:
   - [docs/SONARQUBE_INTEGRATION_GUIDE.md](docs/SONARQUBE_INTEGRATION_GUIDE.md)
   - [docs/BONUS_FEATURES_GUIDE.md](docs/BONUS_FEATURES_GUIDE.md)

2. **Run verification**: `./audit-verification.sh`

3. **Check logs**:
   - SonarQube: `docker logs sonarqube`
   - GitHub Actions: Click on failed workflow → View logs

4. **Key things to remember**:
   - ngrok must stay running during GitHub Actions
   - Tokens expire - regenerate if needed
   - GitHub secrets must be EXACTLY named as shown
   - SonarQube must be accessible from internet for GitHub Actions

---

## ✅ You're Ready When...

- [x] `./audit-verification.sh` shows 100% pass
- [x] You can push code and see GitHub Actions run automatically
- [x] SonarQube dashboard shows the new analysis
- [x] You have screenshots of everything
- [x] You can explain the flow: Git push → GitHub Actions → SonarQube → Quality Gate

**Good luck! You've got this! 🚀**
