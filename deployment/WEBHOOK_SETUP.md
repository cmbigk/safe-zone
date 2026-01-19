# GitHub Webhook Setup for Jenkins CI/CD

This guide explains how to configure GitHub webhooks for instant build triggers instead of SCM polling.

## Why Webhooks?

**Before (SCM Polling):**
- ⏱️ Polls GitHub every 2 minutes
- 🐌 Build starts 0-2 minutes after commit
- 📈 Increases GitHub API usage

**After (Webhooks):**
- ⚡ Instant notification on push
- 🚀 Build starts within seconds
- 📉 Minimal API requests

## Prerequisites

1. Jenkins accessible from internet (or use ngrok/tunneling for local development)
2. GitHub repository with admin access
3. Jenkins GitHub plugin installed (usually pre-installed)

## Step 1: Get Your Jenkins Webhook URL

Your webhook URL format:
```
http://<JENKINS_HOST>:<PORT>/github-webhook/
```

Examples:
- Production: `http://jenkins.yourcompany.com:8090/github-webhook/`
- Local (with ngrok): `https://abc123.ngrok.io/github-webhook/`
- Docker: `http://<HOST_IP>:8090/github-webhook/`

**Important:** 
- URL must end with `/github-webhook/` (note the trailing slash)
- Use HTTP or HTTPS depending on your setup
- Must be publicly accessible from GitHub

## Step 2: Configure GitHub Webhook

### Navigate to Webhook Settings
1. Go to your GitHub repository
2. Click **Settings** (top right)
3. Click **Webhooks** (left sidebar)
4. Click **Add webhook** button

### Configure Webhook

**Payload URL:**
```
http://YOUR_JENKINS_HOST:8090/github-webhook/
```

**Content type:**
```
application/json
```

**Secret:** (Optional but recommended)
```
Leave empty for now (or add Jenkins secret token)
```

**Which events would you like to trigger this webhook?**
- Select: **Just the push event** ✓

**Active:**
- ✓ Checked (enabled)

### Click "Add webhook"

## Step 3: Verify Webhook Configuration

### Test the Webhook

1. After creating, GitHub will send a test ping
2. Check the webhook page for **Recent Deliveries**
3. Click on the first delivery
4. You should see:
   - **Response Code:** 200 OK
   - **Response Body:** (empty is normal)

### Expected Results

**✅ Success:**
```
Status: 200 OK
Response: (empty or minimal JSON)
```

**❌ Failed - Jenkins Not Accessible:**
```
Status: Failed to connect
Error: "We couldn't deliver this payload..."
```
→ Check Jenkins URL, firewall, or use ngrok

**❌ Failed - Wrong URL:**
```
Status: 404 Not Found
```
→ Ensure URL ends with `/github-webhook/`

## Step 4: Verify Jenkins Configuration

### Check Jenkins GitHub Plugin

1. Go to Jenkins → **Manage Jenkins** → **Manage Plugins**
2. Search for **GitHub Plugin**
3. Should be installed and enabled

### Verify Jenkinsfile Trigger

Your `Jenkinsfile` should have:
```groovy
pipeline {
    agent any
    
    triggers {
        githubPush()  // ✓ Webhook trigger
        // pollSCM('H/2 * * * *')  // ✗ Remove or comment out
    }
    
    // ... rest of pipeline
}
```

## Step 5: Test End-to-End

### Make a Test Commit

```bash
# Make a small change
echo "# Test webhook trigger" >> README.md

# Commit and push
git add README.md
git commit -m "test: webhook trigger"
git push origin main
```

### Verify Build Triggers

1. Watch Jenkins dashboard
2. Build should start **within 5-10 seconds** of push
3. Check console output for: `Started by GitHub push`

**Expected Console Output:**
```
Started by GitHub push by <username>
Obtained deployment/Jenkinsfile.fullstack from git ...
```

## Troubleshooting

### Build Not Triggering

**Check 1: Webhook Delivery**
- GitHub → Settings → Webhooks
- Click your webhook
- Check Recent Deliveries
- Should see 200 OK responses

**Check 2: Jenkins Logs**
```bash
# View Jenkins logs in Docker
docker logs jenkins-ci -f

# Look for:
"Received POST from https://github.com/..."
```

**Check 3: Branch Configuration**
- Webhook triggers for configured branches only
- Check Jenkinsfile branch specifiers

### 403 Forbidden Error

**Cause:** Jenkins CSRF protection blocking webhook

**Solution:** Configure GitHub plugin
1. Manage Jenkins → Configure System
2. GitHub → Advanced
3. Check: "Manage hooks"
4. Uncheck: "Override Hook URL" (unless needed)

### Firewall/Network Issues

**For Local Development:**

Use ngrok to expose Jenkins:
```bash
# Install ngrok
brew install ngrok

# Expose Jenkins port
ngrok http 8090

# Use the ngrok URL in GitHub webhook
# Example: https://abc123.ngrok.io/github-webhook/
```

**For Production:**

Ensure firewall allows incoming HTTP/HTTPS:
```bash
# Check if port 8090 is accessible
curl http://YOUR_JENKINS_HOST:8090/

# Configure firewall (example)
sudo ufw allow 8090/tcp
```

## Security Best Practices

### 1. Use HTTPS in Production

Configure Jenkins with SSL certificate:
```bash
# Generate self-signed cert (development only)
openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -days 365 -nodes

# Configure Jenkins with HTTPS
java -jar jenkins.war --httpPort=-1 --httpsPort=8443 \
     --httpsCertificate=cert.pem --httpsPrivateKey=key.pem
```

### 2. Add Webhook Secret

**Generate Secret:**
```bash
openssl rand -hex 32
```

**Configure in Jenkins:**
1. Manage Jenkins → Configure System
2. GitHub → Advanced
3. Add Credentials → Secret text
4. Paste generated secret

**Configure in GitHub:**
1. Edit webhook
2. Add secret in "Secret" field

### 3. Restrict Webhook IP Range

In Jenkins or firewall, allow only GitHub IPs:
```
# GitHub Webhook IPs (check latest at https://api.github.com/meta)
192.30.252.0/22
185.199.108.0/22
140.82.112.0/20
143.55.64.0/20
```

## Multiple Repositories

For multiple repositories, you can:

### Option 1: Same Webhook URL
- Use same Jenkins URL for all repos
- Jenkins automatically routes to correct job
- Based on repository name in payload

### Option 2: Different Jenkins Jobs
- Create separate Jenkins jobs per repository
- Each with own Jenkinsfile
- All use same webhook endpoint

## Monitoring and Maintenance

### Check Webhook Health

**GitHub Dashboard:**
- Settings → Webhooks → Your webhook
- Monitor Recent Deliveries
- Should see successful deliveries for each push

**Jenkins Monitoring:**
```bash
# Check build triggers
# Jenkins → Job → Build History
# Each build should show "Started by GitHub push"
```

### Webhook Payload Example

GitHub sends this JSON on push:
```json
{
  "ref": "refs/heads/main",
  "repository": {
    "name": "jenkins",
    "full_name": "username/jenkins"
  },
  "pusher": {
    "name": "username"
  },
  "commits": [
    {
      "id": "abc123...",
      "message": "fix: update configuration",
      "author": {
        "name": "Developer"
      }
    }
  ]
}
```

Jenkins parses this and triggers the appropriate job.

## Summary

✅ **Completed Setup:**
1. Webhook configured in GitHub
2. Jenkinsfile uses `githubPush()` trigger
3. Test commit triggers build instantly
4. Console shows "Started by GitHub push"

⚡ **Result:** Instant builds on every commit!

## Additional Resources

- [Jenkins GitHub Plugin Documentation](https://plugins.jenkins.io/github/)
- [GitHub Webhooks Guide](https://docs.github.com/en/webhooks)
- [Ngrok Documentation](https://ngrok.com/docs)

## Support

If you encounter issues:
1. Check Recent Deliveries in GitHub webhook page
2. Check Jenkins logs: `docker logs jenkins-ci -f`
3. Verify network connectivity: `curl YOUR_JENKINS_URL/github-webhook/`
4. Review this documentation for troubleshooting steps
