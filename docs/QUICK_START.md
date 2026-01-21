# SonarQube Integration Quick Start Guide
## Get Up and Running in 10 Minutes

---

## 🎯 Overview

This guide helps you quickly set up SonarQube for your e-commerce microservices project.

**What you'll achieve**:
- ✅ SonarQube running locally
- ✅ All 5 projects configured
- ✅ First analysis completed
- ✅ Ready for CI/CD integration

**Time Required**: ~10-15 minutes

---

## 📋 Prerequisites

### Required Software
- ✅ Docker & Docker Compose
- ✅ Git
- ✅ Java 17+ (for Java services)
- ✅ Node.js 18+ (for frontend)
- ✅ Maven 3.8+ (for Java services)

### Verify Prerequisites

```bash
# Check Docker
docker --version
docker-compose --version

# Check Java
java -version

# Check Node.js
node --version

# Check Maven
mvn --version
```

---

## 🚀 Step 1: Start SonarQube (2 minutes)

```bash
# Navigate to deployment directory
cd deployment

# Start SonarQube
./start-sonarqube.sh

# Wait for startup (watch logs)
docker logs -f sonarqube
```

**Look for**: `SonarQube is operational`

**Access SonarQube**: http://localhost:9000

**Default Login**:
- Username: `admin`
- Password: `admin`

**⚠️ IMPORTANT**: Change password when prompted!

---

## 🔧 Step 2: Create Projects (3 minutes)

### Option A: Via Web UI (Manual)

For each service, create a project:

1. Click **Create Project** (+)
2. Choose **Manually**
3. Enter:
   - **Project key**: `ecommerce-api-gateway`
   - **Display name**: `E-Commerce API Gateway`
4. Click **Set Up**
5. Choose **Locally**
6. Generate token:
   - Name: `api-gateway-token`
   - Type: **Project Analysis Token**
   - Expires: No expiration (for learning)
7. Click **Generate** and **SAVE TOKEN**
8. Select build tool: **Maven** (for Java) or **Other** (for frontend)

**Repeat for**:
- `ecommerce-user-service`
- `ecommerce-product-service`
- `ecommerce-media-service`
- `ecommerce-frontend`

### Option B: Via Script (Coming Soon)

---

## 📊 Step 3: Run First Analysis (5 minutes)

### For Java Services (API Gateway example)

```bash
# Navigate to service directory
cd api-gateway

# Run Maven with SonarQube
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=ecommerce-api-gateway \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN_HERE
```

**Replace `YOUR_TOKEN_HERE`** with the token you generated!

### For Frontend

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (if not already done)
npm install

# Run tests with coverage
npm run test:coverage

# Run SonarQube analysis
npx sonar-scanner \
  -Dsonar.projectKey=ecommerce-frontend \
  -Dsonar.sources=src \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN_HERE
```

---

## ✅ Step 4: Verify Setup

### Check in SonarQube UI

1. Go to http://localhost:9000
2. **Projects** page should show your projects
3. Click on a project to see:
   - Dashboard with metrics
   - Issues (if any)
   - Coverage
   - Code smells

### Expected First Results

**Java Services** (typical):
- Coverage: 60-80%
- Bugs: 0-5
- Vulnerabilities: 0-3
- Code Smells: 10-50

**Frontend** (typical):
- Coverage: 50-70%
- Bugs: 0-3
- Code Smells: 20-60

**Don't worry about issues yet!** The goal is to establish a baseline.

---

## 🎨 Optional: Quick Improvements

### Fix Easy Issues

```bash
# View issues in SonarQube UI
# Navigate to: Project → Issues

# Common quick fixes:
# 1. Remove unused imports
# 2. Add missing @Override annotations
# 3. Fix hardcoded strings (extract to constants)
```

### Run Analysis Again

```bash
# After fixing issues
mvn clean verify sonar:sonar \
  -Dsonar.projectKey=ecommerce-api-gateway \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=YOUR_TOKEN

# Watch metrics improve!
```

---

## 🔗 Next Steps

### 1. Configure Quality Gates (5 minutes)

- **Navigate to**: Quality Gates → Create
- **Name**: `E-Commerce-Quality-Gate`
- **Add conditions**:
  - Bugs > 0 → ERROR
  - Vulnerabilities > 0 → ERROR
  - Coverage < 80% → WARNING

### 2. Set Up Jenkins Integration (10 minutes)

- Install SonarQube plugin in Jenkins
- Configure SonarQube server
- Update Jenkinsfile (templates provided)

See: [SONARQUBE_INTEGRATION_GUIDE.md](SONARQUBE_INTEGRATION_GUIDE.md) Section 5

### 3. Set Up GitHub Actions (5 minutes)

- Copy workflow files from `.github/workflows/`
- Add GitHub secrets for tokens
- Push to trigger analysis

See: [SONARQUBE_INTEGRATION_GUIDE.md](SONARQUBE_INTEGRATION_GUIDE.md) Section 4

### 4. Configure Security & Permissions (10 minutes)

- Create user accounts
- Set up groups
- Configure permissions

See: [SECURITY_PERMISSIONS_GUIDE.md](SECURITY_PERMISSIONS_GUIDE.md)

---

## 🔧 Troubleshooting

### SonarQube won't start

```bash
# Check logs
docker logs sonarqube

# Restart
docker-compose -f docker-compose.sonarqube.yml restart
```

### Analysis fails with "401 Unauthorized"

- Check token is correct
- Token hasn't expired
- Generate new token if needed

### Analysis fails with "Connection refused"

```bash
# Check SonarQube is running
docker ps | grep sonarqube

# Check URL is correct (http://localhost:9000)
curl http://localhost:9000/api/system/status
```

### Frontend analysis fails

```bash
# Install sonar-scanner globally
npm install -g sonar-scanner

# Or use npx
npx sonar-scanner --version
```

---

## 📚 Full Documentation

For complete setup including audit requirements:

1. **[SONARQUBE_INTEGRATION_GUIDE.md](SONARQUBE_INTEGRATION_GUIDE.md)** - Complete guide
2. **[SECURITY_PERMISSIONS_GUIDE.md](SECURITY_PERMISSIONS_GUIDE.md)** - Security setup
3. **[CODE_REVIEW_CHECKLIST.md](CODE_REVIEW_CHECKLIST.md)** - Review process
4. **[AUDIT_COMPLIANCE_CHECKLIST.md](AUDIT_COMPLIANCE_CHECKLIST.md)** - Audit prep
5. **[BONUS_FEATURES_GUIDE.md](BONUS_FEATURES_GUIDE.md)** - Slack, IDE integration

---

## 🎯 Quick Reference

### Start/Stop Commands

```bash
# Start SonarQube
cd deployment && ./start-sonarqube.sh

# Stop SonarQube
cd deployment && ./stop-sonarqube.sh

# Check status
docker ps | grep sonarqube
```

### Analysis Commands

**Java**:
```bash
mvn sonar:sonar \
  -Dsonar.projectKey=PROJECT_KEY \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TOKEN
```

**Frontend**:
```bash
npx sonar-scanner \
  -Dsonar.projectKey=PROJECT_KEY \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.token=TOKEN
```

### Important URLs

- **SonarQube UI**: http://localhost:9000
- **API Documentation**: http://localhost:9000/web_api
- **Quality Gates**: http://localhost:9000/quality_gates
- **Rules**: http://localhost:9000/coding_rules

---

## ✅ Success Checklist

After completing quick start:

- [ ] SonarQube running on http://localhost:9000
- [ ] Can login (password changed from default)
- [ ] 5 projects created
- [ ] At least 1 analysis run successfully
- [ ] Dashboard shows metrics
- [ ] Tokens saved securely

**🎉 Congratulations! You're ready to integrate with CI/CD!**

---

## 💡 Tips

1. **Save your tokens immediately** - They're shown only once
2. **Start with one service** - Get it working, then replicate
3. **Don't worry about initial metrics** - Focus on improvement over time
4. **Run analysis frequently** - See results immediately
5. **Check the guides** - Comprehensive docs available for everything

---

## 🆘 Need Help?

- **Full Guide**: [SONARQUBE_INTEGRATION_GUIDE.md](SONARQUBE_INTEGRATION_GUIDE.md)
- **Check Logs**: `docker logs sonarqube`
- **SonarQube Docs**: https://docs.sonarqube.org/latest/

---

**Time to Complete**: ~10-15 minutes  
**Next Step**: Choose Jenkins or GitHub Actions integration
