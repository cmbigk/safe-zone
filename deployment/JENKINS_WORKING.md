# ✅ Jenkins is Now Running Successfully!

## 🎉 Problem Fixed!

**Issue:** Jenkins was crashing due to invalid configuration in `jenkins.yaml`:
- Slack notifier configuration without Slack plugin
- JDK installer configuration with unsupported syntax

**Solution:** Simplified the `jenkins.yaml` to only include essential, working configurations.

---

## 🚀 Access Jenkins Now

**URL:** http://localhost:8090

**Credentials:**
- **Username:** `admin`
- **Password:** `admin123`

---

## 📊 Current Status

```bash
# Check Jenkins is running
docker ps | grep jenkins

# Should show:
# jenkins-ci    Up    0.0.0.0:8090->8080/tcp
```

---

## 🔧 Useful Commands

```bash
# Start Jenkins
cd /Users/chan.myint/Desktop/jenkins/deployment
docker-compose -f docker-compose.jenkins.yml up -d

# Stop Jenkins
docker-compose -f docker-compose.jenkins.yml down

# View logs
docker logs -f jenkins-ci

# Restart Jenkins
docker-compose -f docker-compose.jenkins.yml restart

# Check status
docker ps | grep jenkins
```

---

## 📝 Next Steps to Connect GitHub

### 1. Get GitHub Personal Access Token

1. Go to: https://github.com/settings/tokens
2. Click: **"Generate new token"** → **"Generate new token (classic)"**
3. Configure:
   - **Note:** `Jenkins CI/CD`
   - **Expiration:** 90 days (or your preference)
   - **Select scopes:**
     - ✅ `repo` (Full control of private repositories)
     - ✅ `admin:repo_hook` (Full control of repository hooks)
4. Click **"Generate token"**
5. **COPY THE TOKEN** - you won't see it again!

### 2. Add Credentials to Jenkins

1. Open Jenkins: http://localhost:8090
2. Login: `admin` / `admin123`
3. Navigate to:
   - **Method 1:** Dashboard → **Manage Jenkins** → **Credentials** → **System** → **Global credentials** → **Add Credentials**
   - **Method 2:** Go directly to http://localhost:8090/manage/credentials/store/system/domain/_/newCredentials
   
4. Fill in the form:
   - **Kind:** Username with password
   - **Scope:** Global (Jenkins, nodes, items, all child items, etc)
   - **Username:** Your GitHub username (e.g., `yourusername`)
   - **Password:** Paste your GitHub Personal Access Token
   - **ID:** `github-credentials` (exactly this!)
   - **Description:** `GitHub credentials for CI/CD`

5. Click **"Create"**

### 3. Configure Maven and JDK Tools

Since we simplified the config, you need to configure tools manually:

1. Go to: **Manage Jenkins** → **Tools**

2. **JDK Installations:**
   - Click **"Add JDK"**
   - **Name:** `JDK-21`
   - ✅ Check **"Install automatically"**
   - Select version: **21** or **latest**
   - Click **"Save"**

3. **Maven Installations:**
   - Click **"Add Maven"**
   - **Name:** `Maven-3.9`
   - ✅ Check **"Install automatically"**
   - Select version: **3.9.5** or **latest 3.9.x**
   - Click **"Save"**

4. **NodeJS** (for frontend):
   - Go to: **Manage Jenkins** → **Plugins** → **Available plugins**
   - Search for **"NodeJS Plugin"**
   - Install it
   - Then go to **Tools** → **NodeJS installations**
   - Click **"Add NodeJS"**
   - **Name:** `NodeJS-20`
   - Select version: **20.x**
   - Click **"Save"**

### 4. Create Pipeline Jobs

#### Option A: Automated (Recommended)

```bash
cd /Users/chan.myint/Desktop/jenkins/deployment
./create-jenkins-jobs.sh
```

When prompted:
- Enter your GitHub username
- Enter repository name: `jenkins` (or your repo name)
- Enter branch: `main` (or `master`)

#### Option B: Manual Creation

For each service, create a pipeline job:

1. **Click "New Item"** in Jenkins
2. **Name:** `user-service-pipeline`
3. **Type:** Pipeline
4. Click **OK**
5. **Configure:**
   - **General:**
     - ✅ GitHub project: `https://github.com/YOUR_USERNAME/jenkins/`
   - **Build Triggers:**
     - ✅ Poll SCM: `H/5 * * * *`
   - **Pipeline:**
     - Definition: **Pipeline script from SCM**
     - SCM: **Git**
     - Repository URL: `https://github.com/YOUR_USERNAME/jenkins.git`
     - Credentials: **github-credentials**
     - Branch: `*/main`
     - Script Path: `user-service/Jenkinsfile`
6. Click **Save**

Repeat for:
- `product-service-pipeline` → `product-service/Jenkinsfile`
- `media-service-pipeline` → `media-service/Jenkinsfile`
- `api-gateway-pipeline` → `api-gateway/Jenkinsfile`
- `frontend-pipeline` → `frontend/Jenkinsfile`
- `fullstack-pipeline` → `deployment/Jenkinsfile.fullstack`

### 5. Test Your CI/CD Pipeline

```bash
# Make a change
echo "# Test CI/CD $(date)" >> README.md

# Commit and push
git add README.md
git commit -m "Test CI/CD pipeline"
git push origin main

# Jenkins will detect the change within 5 minutes
# Or trigger manually: Jenkins → Job → "Build with Parameters"
```

---

## 🐛 Troubleshooting

### Jenkins container keeps stopping

**Check logs:**
```bash
docker logs jenkins-ci 2>&1 | grep -i "error\|severe"
```

**Most common issues:**
- Configuration errors in `jenkins-config/jenkins.yaml`
- Missing plugins for configurations
- Port 8090 already in use

### Can't access Jenkins UI

**Check if Jenkins is running:**
```bash
docker ps | grep jenkins
```

**Check if port is accessible:**
```bash
curl -I http://localhost:8090
```

**Restart Jenkins:**
```bash
cd /Users/chan.myint/Desktop/jenkins/deployment
docker-compose -f docker-compose.jenkins.yml restart
```

### "Permission denied" when building Docker images

```bash
docker exec -u root jenkins-ci chmod 666 /var/run/docker.sock
```

### Credentials not showing up

- Make sure you're logged in as `admin`
- Clear browser cache
- Use direct URL: http://localhost:8090/manage/credentials/

---

## 📚 What's Configured

Your Jenkins currently has:

✅ **Authentication:** Admin user with credentials  
✅ **Plugins:** Configuration as Code, Git, GitHub, Credentials, Pipeline  
✅ **Docker Access:** Via socket mount  
✅ **GitHub Integration:** Ready (needs credentials)  
✅ **Tools:** Need manual configuration (see Step 3)

---

## 🎯 Summary

1. ✅ Jenkins is running at http://localhost:8090
2. ✅ Login with `admin` / `admin123`
3. 📝 Add GitHub credentials
4. 🔧 Configure Maven, JDK, NodeJS tools
5. 🚀 Create pipeline jobs
6. 🧪 Test with a push to GitHub

---

**Jenkins is ready for your CI/CD pipeline! 🎉**
