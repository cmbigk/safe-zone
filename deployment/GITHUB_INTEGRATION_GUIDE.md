# GitHub Integration with Jenkins - Step-by-Step Guide

## 🎯 Goal
Connect your GitHub repository to Jenkins so it automatically:
- Fetches the latest commits
- Builds all services
- Runs tests
- Deploys to local server

---

## 📝 Prerequisites Checklist

Before starting, make sure you have:
- ✅ Jenkins running at http://localhost:8090
- ✅ Your code pushed to a GitHub repository
- ✅ A GitHub account

---

## 🚀 Step-by-Step Setup

### Step 1: Create a GitHub Personal Access Token

1. Go to GitHub → **Settings** → **Developer settings** → **Personal access tokens** → **Tokens (classic)**
   - Direct link: https://github.com/settings/tokens

2. Click **"Generate new token"** → **"Generate new token (classic)"**

3. Configure the token:
   - **Note:** `Jenkins CI/CD`
   - **Expiration:** 90 days (or custom)
   - **Select scopes:**
     - ✅ `repo` (Full control of private repositories)
     - ✅ `admin:repo_hook` (Full control of repository hooks)
     - ✅ `admin:org_hook` (if using organization)

4. Click **"Generate token"**

5. **IMPORTANT:** Copy the token immediately (it won't be shown again)
   - Example: `ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

---

### Step 2: Configure Jenkins Credentials

1. Open Jenkins: http://localhost:8090
   - **Username:** `admin`
   - **Password:** `admin123`

2. **Access Credentials** (try these methods in order):

   **Method 1 (Most Common):**
   - From Jenkins Dashboard, look at the left sidebar
   - Click **"Manage Jenkins"**
   - Look for **"Security"** section
   - Click **"Manage Credentials"** or **"Credentials"**
   
   **Method 2 (Direct URL):**
   - Go directly to: http://localhost:8090/manage/credentials/
   
   **Method 3 (Alternative Navigation):**
   - Dashboard → **"Credentials"** (in left sidebar)
   - Or Dashboard → **"Manage Jenkins"** → Scroll down to **"Security"** section → **"Manage Credentials"**

3. Once in Credentials page:
   - Click **"Stores scoped to Jenkins"** → **"System"**
   - Click **"Global credentials (unrestricted)"**
   - Click **"Add Credentials"** (on the left sidebar)

4. Fill in the form:
   - **Kind:** Username with password
   - **Scope:** Global
   - **Username:** Your GitHub username (e.g., `yourusername`)
   - **Password:** Paste your GitHub Personal Access Token
   - **ID:** `github-credentials`
   - **Description:** `GitHub credentials`

5. Click **"Create"**

6. **Verify Credentials Created:**
   - You should see your credential listed with ID: `github-credentials`
   - The username should show your GitHub username
   - The password will be hidden

> **💡 Tip:** If you still can't find Credentials:
> - Make sure you're logged in as admin
> - Check if Jenkins fully loaded after startup (wait 1-2 minutes)
> - Try accessing directly: http://localhost:8090/manage/credentials/
> - Restart Jenkins: `./stop-jenkins.sh && ./start-jenkins.sh`

---

### Step 3: Install Required Jenkins Plugins

1. Go to: **Manage Jenkins** → **Plugins** → **Available plugins**

2. Search and install these plugins (if not already installed):
   - ✅ **Git plugin**
   - ✅ **GitHub plugin**
   - ✅ **GitHub Branch Source plugin**
   - ✅ **Pipeline**
   - ✅ **Pipeline: Stage View**
   - ✅ **Blue Ocean** (optional, but recommended)
   - ✅ **Docker Pipeline**
   - ✅ **NodeJS Plugin**

3. Check the boxes and click **"Install"**

4. Wait for installation to complete, then click **"Restart Jenkins"**

---

### Step 4: Configure GitHub Webhook (Optional but Recommended)

This enables automatic builds when you push to GitHub.

#### Option A: Using Jenkins (Recommended)

1. In Jenkins, go to: **Manage Jenkins** → **System**

2. Scroll to **GitHub** section

3. Click **"Add GitHub Server"** → **"GitHub Server"**

4. Configure:
   - **Name:** `GitHub`
   - **API URL:** `https://api.github.com` (default)
   - **Credentials:** Select `github-credentials`
   - **Manage hooks:** ✅ Check this box

5. Click **"Test connection"** - should show your username

6. Click **"Save"**

#### Option B: Manual Webhook Setup

1. Go to your GitHub repository

2. Click **Settings** → **Webhooks** → **Add webhook**

3. Configure:
   - **Payload URL:** `http://YOUR_PUBLIC_IP:8090/github-webhook/`
   - **Content type:** `application/json`
   - **Which events:** "Just the push event"
   - **Active:** ✅

4. Click **"Add webhook"**

> **Note:** For local Jenkins, you'll need to expose it using ngrok or similar service for webhooks to work. For now, you can use polling instead.

---

### Step 5: Create Jenkins Pipeline Jobs

Now we'll create pipeline jobs for each service.

#### 5.1 Create User Service Pipeline

1. Click **"New Item"** on Jenkins dashboard

2. Configure:
   - **Enter name:** `user-service-pipeline`
   - **Type:** Select **"Pipeline"**
   - Click **"OK"**

3. In the configuration page:

   **General:**
   - ✅ Check **"GitHub project"**
   - **Project url:** `https://github.com/YOUR_USERNAME/jenkins/`

   **Build Triggers:**
   - ✅ Check **"Poll SCM"**
   - **Schedule:** `H/5 * * * *` (polls every 5 minutes)
   - Or ✅ Check **"GitHub hook trigger for GITScm polling"** (if webhook is configured)

   **Pipeline:**
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** `https://github.com/YOUR_USERNAME/jenkins.git`
   - **Credentials:** Select `github-credentials`
   - **Branch Specifier:** `*/main` (or `*/master`)
   - **Script Path:** `user-service/Jenkinsfile`

4. Click **"Save"**

5. Click **"Build Now"** to test

#### 5.2 Create Product Service Pipeline

Repeat Step 5.1 with these changes:
- **Name:** `product-service-pipeline`
- **Script Path:** `product-service/Jenkinsfile`

#### 5.3 Create Media Service Pipeline

Repeat Step 5.1 with these changes:
- **Name:** `media-service-pipeline`
- **Script Path:** `media-service/Jenkinsfile`

#### 5.4 Create API Gateway Pipeline

Repeat Step 5.1 with these changes:
- **Name:** `api-gateway-pipeline`
- **Script Path:** `api-gateway/Jenkinsfile`

#### 5.5 Create Frontend Pipeline

Repeat Step 5.1 with these changes:
- **Name:** `frontend-pipeline`
- **Script Path:** `frontend/Jenkinsfile`

---

### Step 6: Create Full Stack Pipeline (Master Pipeline)

1. Click **"New Item"**

2. Configure:
   - **Name:** `fullstack-pipeline`
   - **Type:** Pipeline
   - Click **"OK"**

3. Configuration:

   **General:**
   - ✅ Check **"This project is parameterized"**
   - Click **"Add Parameter"** → **"Choice Parameter"**
     - **Name:** `ENVIRONMENT`
     - **Choices:** (one per line)
       ```
       dev
       staging
       production
       ```
     - **Description:** `Deployment Environment`

   **Build Triggers:**
   - ✅ Check **"Poll SCM"**
   - **Schedule:** `H/5 * * * *`

   **Pipeline:**
   - **Definition:** Pipeline script from SCM
   - **SCM:** Git
   - **Repository URL:** `https://github.com/YOUR_USERNAME/jenkins.git`
   - **Credentials:** Select `github-credentials`
   - **Branch:** `*/main`
   - **Script Path:** `deployment/Jenkinsfile.fullstack`

4. Click **"Save"**

---

### Step 7: Configure NodeJS Tool

For the frontend pipeline:

1. Go to: **Manage Jenkins** → **Tools**

2. Scroll to **NodeJS installations**

3. Click **"Add NodeJS"**

4. Configure:
   - **Name:** `NodeJS-20`
   - **Version:** Select latest Node 20.x
   - ✅ Check **"Install automatically"**

5. Click **"Save"**

---

### Step 8: Test Your Setup

1. Make a small change to your code locally:
   ```bash
   echo "# CI/CD is working!" >> README.md
   git add README.md
   git commit -m "Test CI/CD trigger"
   git push origin main
   ```

2. Wait 5 minutes (or trigger manually)

3. In Jenkins, you should see:
   - Your pipelines automatically building
   - Progress in the dashboard
   - Build logs in each job

---

## 🔄 Automatic Build Trigger Options

### Option 1: SCM Polling (Easiest for Local)
- Jenkins checks GitHub every 5 minutes
- No external access needed
- Already configured in Step 5

### Option 2: GitHub Webhooks (Best for Production)
- Instant builds on push
- Requires public IP or ngrok
- Steps in Step 4

### Option 3: Manual Trigger
- Click "Build Now" in Jenkins
- Good for testing

---

## 🧪 Testing the Full Pipeline

### Test Individual Service:

1. Go to `user-service-pipeline` in Jenkins

2. Click **"Build with Parameters"**

3. Select:
   - **ENVIRONMENT:** `dev`
   - **DEPLOY:** ✅

4. Click **"Build"**

5. Watch the stages execute:
   - Checkout
   - Build (Maven)
   - Test (JUnit)
   - Docker Build
   - Deploy

### Test Full Stack:

1. Go to `fullstack-pipeline`

2. Click **"Build with Parameters"**

3. Select **ENVIRONMENT:** `dev`

4. Click **"Build"**

5. This will trigger all services in parallel:
   - User Service
   - Product Service
   - Media Service
   - API Gateway
   - Frontend

---

## 📊 Monitoring Your Pipeline

### View Build Status:

1. **Dashboard View:**
   - Shows all jobs and their status
   - Green = Success, Red = Failed

2. **Blue Ocean (Recommended):**
   - Click "Open Blue Ocean" in sidebar
   - Visual pipeline view
   - Better logs and error messages

3. **Build History:**
   - Click on any job
   - See all previous builds
   - Click on build number for details

### Check Logs:

1. Click on a build number (e.g., `#1`)

2. Click **"Console Output"**

3. See real-time logs

---

## 🐳 Verify Deployment

After a successful build:

```bash
# Check running containers
docker ps

# Should see:
# - api-gateway
# - user-service
# - product-service
# - media-service
# - frontend

# Check service health
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:8082/actuator/health  # Product Service
curl http://localhost:8083/actuator/health  # Media Service
curl http://localhost:4200                   # Frontend
```

---

## 🔧 Troubleshooting

### Issue: "Permission denied" when building Docker images

**Solution:**
```bash
# Add Jenkins user to docker group (in Jenkins container)
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

### Issue: "Git not found"

**Solution:**
- Jenkins needs Git plugin installed (Step 3)
- Check: **Manage Jenkins** → **Tools** → **Git** → Should show "Default"

### Issue: "Credentials not found"

**Solution:**
- Verify credential ID is exactly `github-credentials`
- Check: **Manage Jenkins** → **Credentials** → Should see your GitHub credentials

### Issue: Maven/NodeJS not found

**Solution:**
- Ensure tools are configured in: **Manage Jenkins** → **Tools**
- Tool names in Jenkinsfile must match:
  - `Maven-3.9`
  - `NodeJS-20`
  - `JDK-21`

### Issue: Webhook not triggering builds

**Solution:**
- For local Jenkins, use SCM polling instead
- Or use ngrok to expose Jenkins:
  ```bash
  ngrok http 8090
  # Use the ngrok URL in GitHub webhook
  ```

### Issue: Build fails with "No space left on device"

**Solution:**
```bash
# Clean up Docker
docker system prune -a
docker volume prune
```

---

## 🚀 Quick Start Commands

```bash
# Start Jenkins
cd deployment
./start-jenkins.sh

# Access Jenkins
open http://localhost:8090

# View logs
docker logs -f jenkins

# Stop Jenkins
./stop-jenkins.sh

# Restart Jenkins
./stop-jenkins.sh && ./start-jenkins.sh
```

---

## 📚 Next Steps

1. ✅ Set up email notifications (edit Jenkinsfiles)
2. ✅ Configure Slack notifications (add webhook)
3. ✅ Set up automated testing
4. ✅ Configure blue-green deployment
5. ✅ Set up monitoring and health checks

---

## 🆘 Need Help?

- Check Jenkins logs: `docker logs jenkins`
- Check build logs in Jenkins UI
- Review the Jenkinsfile for each service
- Check GitHub Actions tab for webhook delivery

---

## 📝 Summary

You've now configured:
- ✅ Jenkins with GitHub integration
- ✅ Automated builds from GitHub commits
- ✅ Separate pipelines for each service
- ✅ Master pipeline for full stack deployment
- ✅ Docker-based deployment
- ✅ Health checks and testing

**Your CI/CD pipeline is ready! 🎉**

Push to GitHub → Jenkins builds → Tests run → Deploys to local server
