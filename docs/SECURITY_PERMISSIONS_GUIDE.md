# SonarQube Security & Permissions Guide

## Table of Contents
1. [User Management](#user-management)
2. [Group Management](#group-management)
3. [Permission Levels](#permission-levels)
4. [Token Security](#token-security)
5. [Project Permissions](#project-permissions)
6. [Security Best Practices](#security-best-practices)
7. [Audit Trail](#audit-trail)

---

## 1. User Management

### 1.1 Creating Users

**Navigate to**: Administration → Security → Users

#### Steps:
1. Click **Create User**
2. Fill in details:
   - **Login**: username (e.g., john.doe)
   - **Name**: Full name
   - **Email**: user@example.com
   - **Password**: Strong password (min 12 characters)
3. Click **Create**

#### Default Users to Create for Student Project:

| Login | Name | Email | Role |
|-------|------|-------|------|
| admin | Administrator | admin@ecommerce.local | System Admin |
| devops.lead | DevOps Lead | devops@ecommerce.local | Admin |
| backend.dev1 | Backend Developer 1 | backend1@ecommerce.local | Developer |
| backend.dev2 | Backend Developer 2 | backend2@ecommerce.local | Developer |
| frontend.dev | Frontend Developer | frontend@ecommerce.local | Developer |
| code.reviewer | Code Reviewer | reviewer@ecommerce.local | Reviewer |
| qa.tester | QA Tester | qa@ecommerce.local | Viewer |

### 1.2 Password Policy

**Recommended settings**:
- Minimum length: 12 characters
- Require: uppercase, lowercase, numbers, special characters
- Password expiration: 90 days
- Prevent password reuse: Last 5 passwords

**Implementation** (Document in security policy):
```
Password Requirements:
✓ At least 12 characters
✓ Contains uppercase (A-Z)
✓ Contains lowercase (a-z)
✓ Contains numbers (0-9)
✓ Contains special characters (!@#$%^&*)
✗ No common words or patterns
✗ No personal information
```

---

## 2. Group Management

### 2.1 Default Groups

**Navigate to**: Administration → Security → Groups

SonarQube comes with two default groups:
- **sonar-administrators**: Full system access
- **sonar-users**: Basic authenticated users

### 2.2 Custom Groups for E-Commerce Project

Create these groups for proper access control:

#### Development Groups

**1. Backend Developers**
- **Name**: `backend-developers`
- **Description**: Backend Java developers working on microservices
- **Members**: backend.dev1, backend.dev2

**2. Frontend Developers**
- **Name**: `frontend-developers`
- **Description**: Frontend Angular developers
- **Members**: frontend.dev

**3. DevOps Team**
- **Name**: `devops-team`
- **Description**: DevOps engineers managing CI/CD
- **Members**: devops.lead

**4. Code Reviewers**
- **Name**: `code-reviewers`
- **Description**: Senior developers responsible for code review
- **Members**: code.reviewer, devops.lead

**5. QA Team**
- **Name**: `qa-team`
- **Description**: Quality assurance testers
- **Members**: qa.tester

### 2.3 Creating Groups

**Steps**:
1. **Administration** → **Security** → **Groups**
2. Click **Create Group**
3. Enter name and description
4. Click **Create**
5. Click on group name
6. Click **Users** tab
7. Add members

---

## 3. Permission Levels

### 3.1 Global Permissions

**Navigate to**: Administration → Security → Global Permissions

#### Permission Matrix

| Permission | Description | Grant To |
|------------|-------------|----------|
| **Administer System** | Full system administration | sonar-administrators, devops-team |
| **Administer Quality Gates** | Create/edit quality gates | devops-team, code-reviewers |
| **Administer Quality Profiles** | Create/edit quality profiles | devops-team, code-reviewers |
| **Create Projects** | Create new projects | devops-team, backend-developers, frontend-developers |
| **Create Applications** | Create application portfolios | devops-team |
| **Browse** | Access SonarQube interface | All authenticated users |

### 3.2 Project Permissions

**Navigate to**: Project → Project Settings → Permissions

#### Permission Types

| Permission | Description | Use Case |
|------------|-------------|----------|
| **Administer** | Full project admin | DevOps team, project leads |
| **Administer Issues** | Change issue severity, type, status | Code reviewers |
| **Administer Security Hotspots** | Manage security hotspots | Security team, code reviewers |
| **Browse** | View project dashboard | All developers, QA |
| **See Source Code** | View source code in SonarQube | Developers, reviewers |
| **Execute Analysis** | Run analysis via token | CI/CD pipelines |

### 3.3 Recommended Project Permissions

#### For API Gateway Project:

| Group | Administer | Admin Issues | Browse | See Source | Execute Analysis |
|-------|-----------|--------------|--------|------------|-----------------|
| devops-team | ✅ | ✅ | ✅ | ✅ | ✅ |
| backend-developers | ❌ | ❌ | ✅ | ✅ | ❌ |
| code-reviewers | ❌ | ✅ | ✅ | ✅ | ❌ |
| qa-team | ❌ | ❌ | ✅ | ❌ | ❌ |

**Repeat for all projects**: user-service, product-service, media-service, frontend

---

## 4. Token Security

### 4.1 Token Types

SonarQube supports three types of tokens:

1. **User Tokens**: Associated with a specific user account
2. **Project Analysis Tokens**: Limited to analyzing specific projects
3. **Global Analysis Tokens**: Can analyze any project

### 4.2 Creating Tokens

#### For CI/CD Pipelines (Recommended: Project Analysis Tokens)

**Steps**:
1. Navigate to project
2. **Project Settings** → **Analysis Tokens**
3. Click **Generate Token**
4. Enter details:
   - **Name**: `jenkins-pipeline` or `github-actions`
   - **Type**: Project Analysis Token
   - **Expires in**: 90 days (for production) or No expiration (for learning)
5. Click **Generate**
6. **IMPORTANT**: Copy token immediately (shown only once)

#### For IDE Integration (User Tokens)

**Steps**:
1. Click profile icon → **My Account**
2. **Security** tab
3. **Generate Token**
4. Enter name: `vscode-integration` or `intellij-integration`
5. Click **Generate**
6. Copy token

### 4.3 Token Naming Convention

Use descriptive names to identify token purpose:

```
Format: <tool>-<environment>-<purpose>
Examples:
  - jenkins-prod-api-gateway
  - github-actions-frontend
  - vscode-dev-john.doe
  - postman-api-testing
```

### 4.4 Token Storage

**❌ NEVER**:
- Commit tokens to Git
- Share tokens in chat/email
- Store in plain text files
- Hardcode in scripts

**✅ ALWAYS**:
- Store in Jenkins credentials
- Use GitHub Secrets
- Store in password manager
- Encrypt in configuration files

#### Example Token Storage

**Jenkins**:
```groovy
withSonarQubeEnv('SonarQube-Local') {
    // Token automatically injected from Jenkins credentials
}
```

**GitHub Actions**:
```yaml
env:
  SONAR_TOKEN: ${{ secrets.SONAR_TOKEN_API_GATEWAY }}
```

**Local Development** (.env file, add to .gitignore):
```bash
SONAR_TOKEN=squ_xxxxxxxxxxxxxxxxxxxx
SONAR_HOST_URL=http://localhost:9000
```

### 4.5 Token Rotation

**Policy**: Rotate tokens every 90 days

**Steps**:
1. Generate new token
2. Update in Jenkins/GitHub
3. Test new token
4. Revoke old token

**Create reminder script**:
```bash
#!/bin/bash
# Token expiration reminder
# Add to cron: 0 9 * * MON (every Monday at 9 AM)

echo "🔐 Token Expiration Check"
echo "Check SonarQube tokens expiring in the next 30 days"
echo "Navigate to: http://localhost:9000 → My Account → Security"
```

### 4.6 Revoking Tokens

**When to revoke**:
- Token compromised
- Employee/student leaves project
- Token no longer needed
- After token rotation

**Steps**:
1. **Administration** → **Security** → **Users** → Select user
2. **Tokens** tab
3. Click **Revoke** next to token

---

## 5. Project Permissions

### 5.1 Setting Up Project-Specific Permissions

For each project, configure granular permissions:

#### API Gateway Example

**Navigate to**: ecommerce-api-gateway → Project Settings → Permissions

**Configuration**:

1. **Remove public access**:
   - Uncheck all permissions for "Anyone"

2. **Grant group permissions**:

**devops-team**:
```
✅ Administer
✅ Administer Issues
✅ Administer Security Hotspots
✅ Browse
✅ See Source Code
✅ Execute Analysis
```

**backend-developers**:
```
❌ Administer
❌ Administer Issues
❌ Administer Security Hotspots
✅ Browse
✅ See Source Code
❌ Execute Analysis
```

**code-reviewers**:
```
❌ Administer
✅ Administer Issues
✅ Administer Security Hotspots
✅ Browse
✅ See Source Code
❌ Execute Analysis
```

**qa-team**:
```
❌ Administer
❌ Administer Issues
❌ Administer Security Hotspots
✅ Browse
❌ See Source Code (can see issues but not source)
❌ Execute Analysis
```

### 5.2 Permission Templates

Create permission templates for consistent project setup:

**Navigate to**: Administration → Security → Permission Templates

**Template: Java Microservices**
```
Template Name: Java Microservices Template
Description: Standard permissions for Java backend services

Groups:
  devops-team: All permissions
  backend-developers: Browse, See Source Code
  code-reviewers: Browse, See Source Code, Administer Issues
  qa-team: Browse
```

**Template: Frontend Application**
```
Template Name: Frontend Application Template
Description: Standard permissions for Angular frontend

Groups:
  devops-team: All permissions
  frontend-developers: Browse, See Source Code
  code-reviewers: Browse, See Source Code, Administer Issues
  qa-team: Browse
```

---

## 6. Security Best Practices

### 6.1 Authentication

#### Force Authentication

**Navigate to**: Administration → Configuration → General Settings → Security

**Enable**:
- ✅ **Force user authentication**: Prevent anonymous access

#### GitHub Authentication (Optional)

For better integration with GitHub:

1. **Administration** → **Configuration** → **General Settings** → **GitHub**
2. Install GitHub Authentication plugin
3. Configure:
   - Client ID: (from GitHub OAuth app)
   - Client Secret: (from GitHub OAuth app)
   - Allow users to sign up: ✅

### 6.2 Session Management

**Configuration**:
- Session timeout: 30 minutes of inactivity
- Concurrent sessions: Maximum 3 per user
- Remember me: Disabled (for security)

### 6.3 API Security

#### Rate Limiting

Document rate limits:
```
API Rate Limits:
- Authenticated users: 1000 requests/hour
- CI/CD tokens: 10000 requests/hour
- Admin users: Unlimited
```

#### IP Whitelisting (Production)

For production environments, whitelist Jenkins/GitHub IPs:
```
Allowed IPs:
- Jenkins server: 10.0.0.5
- GitHub Actions: 140.82.112.0/20
- Office network: 203.0.113.0/24
```

### 6.4 Audit Configuration

**Enable audit logging**:

Navigate to: Administration → Configuration → General Settings → Security

Enable:
- ✅ Log all user actions
- ✅ Log permission changes
- ✅ Log quality gate changes

### 6.5 Security Checklist

**For Audit**:

- [ ] Default admin password changed
- [ ] Force authentication enabled
- [ ] All users have unique accounts
- [ ] Groups configured with least privilege
- [ ] Project permissions set appropriately
- [ ] Tokens use descriptive names
- [ ] Token expiration configured
- [ ] No tokens in source code
- [ ] Audit logging enabled
- [ ] Regular security reviews scheduled

---

## 7. Audit Trail

### 7.1 Viewing Audit Logs

**Navigate to**: Administration → System → Audit Logs

**Log Types**:
- User logins
- Permission changes
- Quality gate modifications
- Project deletions
- Token creation/revocation

### 7.2 User Activity Monitoring

**Per User**:
1. **Administration** → **Security** → **Users**
2. Click on user
3. View:
   - Last connection date
   - Token list
   - Group memberships
   - Project permissions

### 7.3 Documentation for Audit

Create this document structure:

```
docs/security/
├── access-control-matrix.md     # Who has access to what
├── token-inventory.md           # List of all active tokens
├── user-accounts.md             # List of all user accounts
├── permission-changes-log.md   # History of permission changes
└── security-review-reports/    # Monthly security reviews
```

#### Example: Access Control Matrix

| User | Role | API Gateway | User Service | Frontend | Admin Access |
|------|------|-------------|--------------|----------|--------------|
| devops.lead | DevOps Lead | Admin | Admin | Admin | Yes |
| backend.dev1 | Developer | View | View | View | No |
| code.reviewer | Reviewer | Review | Review | Review | No |
| qa.tester | QA | View | View | View | No |

---

## 8. Common Security Scenarios

### Scenario 1: New Developer Joins

**Steps**:
1. Create user account
2. Add to appropriate group (backend-developers or frontend-developers)
3. Generate IDE integration token
4. Provide token securely (password manager or encrypted)
5. Document in user-accounts.md

### Scenario 2: Developer Leaves Project

**Steps**:
1. Revoke all tokens
2. Remove from all groups
3. Disable account (don't delete for audit trail)
4. Update documentation
5. Review permissions on all projects

### Scenario 3: Security Token Compromised

**Immediate Actions**:
1. Revoke compromised token immediately
2. Generate new token
3. Update in Jenkins/GitHub
4. Verify no unauthorized analysis
5. Document incident
6. Review access logs

### Scenario 4: Audit Preparation

**Checklist**:
1. Generate access control matrix
2. Export user list with last login dates
3. Document all active tokens with purpose
4. Review and update group permissions
5. Verify force authentication is enabled
6. Prepare permission change history
7. Document security incidents (if any)

---

## 9. Scripts for Security Management

### Check Token Expiration

```bash
#!/bin/bash
# check-token-expiration.sh

echo "🔐 SonarQube Token Expiration Report"
echo "===================================="
echo ""
echo "Tokens expiring in the next 30 days:"
echo ""
echo "Note: Run this query in SonarQube UI:"
echo "My Account → Security → Tokens"
echo ""
echo "Review tokens and rotate if needed."
```

### Generate Access Report

```bash
#!/bin/bash
# generate-access-report.sh

echo "📊 SonarQube Access Report"
echo "=========================="
echo "Generated: $(date)"
echo ""
echo "Projects:"
echo "  - ecommerce-api-gateway"
echo "  - ecommerce-user-service"
echo "  - ecommerce-product-service"
echo "  - ecommerce-media-service"
echo "  - ecommerce-frontend"
echo ""
echo "Total Users: 7"
echo "Total Groups: 6"
echo ""
echo "For detailed report, navigate to:"
echo "http://localhost:9000/admin/permissions"
```

---

## 10. Audit Questions & Answers

### Q1: How do you prevent unauthorized access to SonarQube?

**Answer**:
- Force authentication enabled (no anonymous access)
- Strong password policy enforced
- Role-based access control (RBAC) implemented
- Least privilege principle applied
- Regular access reviews conducted

### Q2: How are tokens secured?

**Answer**:
- Project analysis tokens used (not global tokens)
- Tokens stored in Jenkins credentials / GitHub secrets
- Never committed to source control
- Expiration dates set (90 days)
- Regular rotation schedule
- Immediate revocation when compromised

### Q3: Who can modify quality gates?

**Answer**:
- Only devops-team and code-reviewers groups
- Regular developers cannot modify
- All changes logged in audit trail
- Requires admin authentication

### Q4: How do you track permission changes?

**Answer**:
- Audit logging enabled in SonarQube
- All permission changes logged with timestamp and user
- Regular export of audit logs
- Documentation maintained in permission-changes-log.md

### Q5: What happens if a token is compromised?

**Answer**:
1. Immediate revocation of token
2. Generation of new token
3. Update in CI/CD system
4. Review of recent analysis runs
5. Incident documentation
6. Security review of related projects

---

## Summary

This security guide ensures:
✅ Proper user and group management
✅ Granular permission control
✅ Secure token handling
✅ Audit trail maintenance
✅ Compliance with security best practices

**For Audit**: Keep this document alongside:
- Access control matrix
- Token inventory
- User account list
- Security review reports
