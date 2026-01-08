# Quick Setup Guide - GitHub + Jenkins CI/CD

## ⚡ 5-Minute Quick Start

### 1️⃣ Get GitHub Token
```bash
# Go to: https://github.com/settings/tokens
# Click: Generate new token (classic)
# Select: ✅ repo, ✅ admin:repo_hook
# Copy token: ghp_xxxxxxxxxxxxxxxxxxxx
```

### 2️⃣ Add Credentials to Jenkins
```bash
# Open: http://localhost:8090
# Go to: Manage Jenkins → Credentials → System → Global credentials
# Click: Add Credentials
# 
# Fill in:
# - Kind: Username with password
# - Username: <your-github-username>
# - Password: <paste-your-github-token>
# - ID: github-credentials
# - Click: Create
```

### 3️⃣ Run Automated Setup
```bash
cd deployment
chmod +x create-jenkins-jobs.sh
./create-jenkins-jobs.sh
# Enter your GitHub username when prompted
# Enter repository name: jenkins
# Enter branch name: main
```

### 4️⃣ Test It!
```bash
# Make a change and push
echo "# Test CI/CD" >> README.md
git add README.md
git commit -m "Test CI/CD"
git push origin main

# Wait 5 minutes or trigger manually in Jenkins
# Open: http://localhost:8090
```

---

## 🎯 Manual Setup (Alternative)

### Create One Pipeline Job

1. **Jenkins Dashboard** → **New Item**
2. **Name:** `user-service-pipeline`
3. **Type:** Pipeline → **OK**
4. **Configure:**
   - ✅ GitHub project: `https://github.com/YOUR_USERNAME/jenkins/`
   - ✅ Poll SCM: `H/5 * * * *`
   - Pipeline → Definition: **Pipeline script from SCM**
   - SCM: **Git**
   - Repository URL: `https://github.com/YOUR_USERNAME/jenkins.git`
   - Credentials: **github-credentials**
   - Branch: `*/main`
   - Script Path: `user-service/Jenkinsfile`
5. **Save**
6. **Build Now**

Repeat for:
- `product-service-pipeline` → `product-service/Jenkinsfile`
- `media-service-pipeline` → `media-service/Jenkinsfile`
- `api-gateway-pipeline` → `api-gateway/Jenkinsfile`
- `frontend-pipeline` → `frontend/Jenkinsfile`
- `fullstack-pipeline` → `deployment/Jenkinsfile.fullstack`

---

## 📝 Essential Commands

### Jenkins Control
```bash
# Start Jenkins
cd deployment
./start-jenkins.sh

# Stop Jenkins
./stop-jenkins.sh

# View logs
docker logs -f jenkins

# Access Jenkins
open http://localhost:8090
# Username: admin
# Password: admin123
```

### GitHub Operations
```bash
# Check current remote
git remote -v

# Add remote if not exists
git remote add origin https://github.com/USERNAME/jenkins.git

# Push to GitHub
git add .
git commit -m "Your message"
git push origin main
```

### Docker Management
```bash
# View running services
docker ps

# View all containers
docker ps -a

# Check service health
curl http://localhost:8080/actuator/health  # API Gateway
curl http://localhost:8081/actuator/health  # User Service
curl http://localhost:4200                   # Frontend

# Clean up
docker system prune -a
```

---

## 🔍 Troubleshooting Quick Fixes

### Problem: "github-credentials not found"
```bash
# Solution: Add credentials in Jenkins
# Manage Jenkins → Credentials → Add Credentials
# ID must be exactly: github-credentials
```

### Problem: "Git not found"
```bash
# Solution: Install Git plugin
# Manage Jenkins → Plugins → Available plugins
# Search "Git plugin" → Install
```

### Problem: "Permission denied" for Docker
```bash
# Solution: Fix Docker socket permissions
docker exec -u root jenkins chmod 666 /var/run/docker.sock
```

### Problem: "Maven/NodeJS not found"
```bash
# Solution: Configure tools
# Manage Jenkins → Tools
# Add Maven: Name = "Maven-3.9", Auto-install
# Add NodeJS: Name = "NodeJS-20", Auto-install
# Add JDK: Name = "JDK-21", Auto-install
```

### Problem: Build not triggering automatically
```bash
# Solution 1: Check SCM Polling
# Job → Configure → Build Triggers
# ✅ Poll SCM: H/5 * * * *

# Solution 2: Check last poll
# Job page → View "Git Polling Log"

# Solution 3: Trigger manually
# Click "Build with Parameters"
```

---

## 🎨 Jenkins Pipeline Structure

```
jenkins/
├── api-gateway/
│   └── Jenkinsfile          → api-gateway-pipeline
├── product-service/
│   └── Jenkinsfile          → product-service-pipeline
├── user-service/
│   └── Jenkinsfile          → user-service-pipeline
├── media-service/
│   └── Jenkinsfile          → media-service-pipeline
├── frontend/
│   └── Jenkinsfile          → frontend-pipeline
└── deployment/
    └── Jenkinsfile.fullstack → fullstack-pipeline (master)
```

---

## 🚀 Pipeline Execution Flow

### Individual Service Pipeline:
```
1. Checkout from GitHub
2. Build (Maven/NPM)
3. Run Tests (JUnit/Karma)
4. Build Docker Image
5. Deploy Container
6. Health Check
7. Notify (Success/Failure)
```

### Fullstack Pipeline:
```
1. Trigger all services in parallel:
   ├── User Service
   ├── Product Service
   ├── Media Service
   └── API Gateway
2. Wait for all to complete
3. Deploy Frontend
4. Run smoke tests
5. Health check all services
6. Notify team
```

---

## ✅ Verification Checklist

After setup, verify:

- [ ] Jenkins is running: `http://localhost:8090`
- [ ] GitHub credentials added
- [ ] All 6 pipeline jobs created
- [ ] Can see GitHub repository in job config
- [ ] SCM polling is enabled
- [ ] Can trigger manual build successfully
- [ ] Build shows "Checkout" stage pulling from GitHub
- [ ] Docker images are created
- [ ] Services are deployed
- [ ] Health checks pass

---

## 🌐 Access URLs

| Service | URL | Port |
|---------|-----|------|
| Jenkins | http://localhost:8090 | 8090 |
| API Gateway | http://localhost:8080 | 8080 |
| User Service | http://localhost:8081 | 8081 |
| Product Service | http://localhost:8082 | 8082 |
| Media Service | http://localhost:8083 | 8083 |
| Frontend | http://localhost:4200 | 4200 |

---

## 📊 Test Your Setup

```bash
# 1. Make a change
echo "# CI/CD Test $(date)" >> README.md

# 2. Commit and push
git add README.md
git commit -m "Test CI/CD pipeline"
git push origin main

# 3. Watch Jenkins (wait ~5 minutes for polling)
open http://localhost:8090

# 4. Or trigger immediately
# Jenkins → fullstack-pipeline → Build with Parameters

# 5. Verify deployment
docker ps
curl http://localhost:8080/actuator/health
```

---

## 💡 Pro Tips

1. **Blue Ocean UI**: Better visualization
   - Install Blue Ocean plugin
   - Click "Open Blue Ocean" in Jenkins sidebar

2. **Instant Builds**: Use webhooks instead of polling
   - Requires public IP or ngrok
   - See full guide: `GITHUB_INTEGRATION_GUIDE.md`

3. **Build Notifications**: 
   - Configure email in Jenkinsfiles
   - Add Slack webhook for team notifications

4. **Branch Strategies**:
   - `main` branch → Auto-deploy to dev
   - `staging` branch → Auto-deploy to staging
   - `production` branch → Manual approval required

5. **Parallel Builds**:
   - Fullstack pipeline runs all services in parallel
   - Saves time on large deployments

---

## 📖 Full Documentation

For detailed instructions, see:
- `GITHUB_INTEGRATION_GUIDE.md` - Complete setup guide
- `README.md` - Full documentation
- Individual `Jenkinsfile` - Pipeline configurations

---

## 🆘 Get Help

1. Check Jenkins logs:
   ```bash
   docker logs jenkins
   ```

2. Check build console output:
   - Jenkins → Job → Build # → Console Output

3. Check GitHub webhook deliveries:
   - GitHub → Repository → Settings → Webhooks

4. Check service logs:
   ```bash
   docker logs api-gateway
   docker logs user-service
   docker logs frontend
   ```

---

**Your CI/CD pipeline is ready! 🎉**

```
Push to GitHub → Jenkins detects change → Builds all services → 
Runs tests → Creates Docker images → Deploys locally → Health checks → 
Notifies team → Done! ✅
```
