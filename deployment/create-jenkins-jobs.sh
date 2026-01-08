#!/bin/bash

# Jenkins Pipeline Setup Automation Script
# This script helps create all necessary Jenkins pipeline jobs

set -e

echo "╔════════════════════════════════════════════════════════════════╗"
echo "║                                                                ║"
echo "║        Jenkins Pipeline Jobs Setup - Automation Script        ║"
echo "║                                                                ║"
echo "╚════════════════════════════════════════════════════════════════╝"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Jenkins Configuration
JENKINS_URL="http://localhost:8090"
JENKINS_USER="admin"
JENKINS_PASSWORD="admin123"

# Prompt for GitHub info
echo -e "${YELLOW}📝 GitHub Repository Information${NC}"
echo ""
read -p "Enter your GitHub username: " GITHUB_USER
read -p "Enter your repository name [jenkins]: " REPO_NAME
REPO_NAME=${REPO_NAME:-jenkins}
read -p "Enter your branch name [main]: " BRANCH_NAME
BRANCH_NAME=${BRANCH_NAME:-main}

GITHUB_REPO_URL="https://github.com/${GITHUB_USER}/${REPO_NAME}.git"
GITHUB_PROJECT_URL="https://github.com/${GITHUB_USER}/${REPO_NAME}/"

echo ""
echo -e "${BLUE}Repository URL: ${GITHUB_REPO_URL}${NC}"
echo ""

# Function to create Jenkins job
create_jenkins_job() {
    local job_name=$1
    local jenkinsfile_path=$2
    local description=$3
    
    echo -e "${YELLOW}Creating job: ${job_name}...${NC}"
    
    # Get Jenkins crumb for CSRF protection
    CRUMB=$(curl -s -u "${JENKINS_USER}:${JENKINS_PASSWORD}" \
        "${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)")
    
    # Create job XML configuration
    cat > "/tmp/${job_name}.xml" <<EOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <description>${description}</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <com.coravy.hudson.plugins.github.GithubProjectProperty>
      <projectUrl>${GITHUB_PROJECT_URL}</projectUrl>
      <displayName></displayName>
    </com.coravy.hudson.plugins.github.GithubProjectProperty>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>ENVIRONMENT</name>
          <description>Deployment Environment</description>
          <choices class="java.util.Arrays\$ArrayList">
            <a class="string-array">
              <string>dev</string>
              <string>staging</string>
              <string>production</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
        <hudson.model.BooleanParameterDefinition>
          <name>DEPLOY</name>
          <description>Deploy after build?</description>
          <defaultValue>true</defaultValue>
        </hudson.model.BooleanParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition">
    <scm class="hudson.plugins.git.GitSCM">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>${GITHUB_REPO_URL}</url>
          <credentialsId>github-credentials</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/${BRANCH_NAME}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>${jenkinsfile_path}</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers>
    <hudson.triggers.SCMTrigger>
      <spec>H/5 * * * *</spec>
      <ignorePostCommitHooks>false</ignorePostCommitHooks>
    </hudson.triggers.SCMTrigger>
  </triggers>
  <disabled>false</disabled>
</flow-definition>
EOF

    # Create job using Jenkins CLI
    curl -s -X POST "${JENKINS_URL}/createItem?name=${job_name}" \
         --user "${JENKINS_USER}:${JENKINS_PASSWORD}" \
         --header "${CRUMB}" \
         --header "Content-Type: application/xml" \
         --data-binary "@/tmp/${job_name}.xml" \
         > /tmp/${job_name}_result.txt 2>&1
    
    # Check if job was created successfully
    if grep -q "already exists" /tmp/${job_name}_result.txt; then
        echo -e "${YELLOW}⚠ ${job_name} already exists, skipping...${NC}"
    elif grep -q "error\|Error\|ERROR" /tmp/${job_name}_result.txt; then
        echo -e "${RED}✗ Failed to create ${job_name}${NC}"
        cat /tmp/${job_name}_result.txt
    else
        echo -e "${GREEN}✓ Successfully created ${job_name}${NC}"
    fi
    
    # Clean up
    rm -f "/tmp/${job_name}.xml" "/tmp/${job_name}_result.txt"
}

# Wait for Jenkins to be ready
echo -e "${YELLOW}🔍 Checking Jenkins availability...${NC}"
max_attempts=30
attempt=0

while [ $attempt -lt $max_attempts ]; do
    # Check if Jenkins responds with any HTTP status (including 403 for login page)
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "${JENKINS_URL}/login" 2>/dev/null)
    if [ "$http_code" = "200" ] || [ "$http_code" = "403" ]; then
        echo -e "${GREEN}✓ Jenkins is ready!${NC}"
        echo ""
        break
    fi
    attempt=$((attempt + 1))
    echo -e "${YELLOW}Waiting for Jenkins... (${attempt}/${max_attempts})${NC}"
    sleep 2
done

if [ $attempt -eq $max_attempts ]; then
    echo -e "${RED}✗ Jenkins is not available at ${JENKINS_URL}${NC}"
    echo "Please make sure Jenkins is running and fully started."
    echo "Check logs: docker logs jenkins-ci"
    exit 1
fi

# Create all pipeline jobs
echo ""
echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║            Creating Jenkins Pipeline Jobs                 ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

create_jenkins_job "user-service-pipeline" "user-service/Jenkinsfile" "User Service CI/CD Pipeline"
create_jenkins_job "product-service-pipeline" "product-service/Jenkinsfile" "Product Service CI/CD Pipeline"
create_jenkins_job "media-service-pipeline" "media-service/Jenkinsfile" "Media Service CI/CD Pipeline"
create_jenkins_job "api-gateway-pipeline" "api-gateway/Jenkinsfile" "API Gateway CI/CD Pipeline"
create_jenkins_job "frontend-pipeline" "frontend/Jenkinsfile" "Frontend CI/CD Pipeline"

# Create fullstack pipeline
echo ""
echo -e "${YELLOW}Creating master orchestration job...${NC}"

cat > "/tmp/fullstack-pipeline.xml" <<EOF
<?xml version='1.1' encoding='UTF-8'?>
<flow-definition plugin="workflow-job@2.40">
  <description>Full Stack CI/CD Pipeline - Orchestrates all services</description>
  <keepDependencies>false</keepDependencies>
  <properties>
    <com.coravy.hudson.plugins.github.GithubProjectProperty>
      <projectUrl>${GITHUB_PROJECT_URL}</projectUrl>
    </com.coravy.hudson.plugins.github.GithubProjectProperty>
    <hudson.model.ParametersDefinitionProperty>
      <parameterDefinitions>
        <hudson.model.ChoiceParameterDefinition>
          <name>ENVIRONMENT</name>
          <description>Deployment Environment</description>
          <choices class="java.util.Arrays\$ArrayList">
            <a class="string-array">
              <string>dev</string>
              <string>staging</string>
              <string>production</string>
            </a>
          </choices>
        </hudson.model.ChoiceParameterDefinition>
      </parameterDefinitions>
    </hudson.model.ParametersDefinitionProperty>
  </properties>
  <definition class="org.jenkinsci.plugins.workflow.cps.CpsScmFlowDefinition">
    <scm class="hudson.plugins.git.GitSCM">
      <configVersion>2</configVersion>
      <userRemoteConfigs>
        <hudson.plugins.git.UserRemoteConfig>
          <url>${GITHUB_REPO_URL}</url>
          <credentialsId>github-credentials</credentialsId>
        </hudson.plugins.git.UserRemoteConfig>
      </userRemoteConfigs>
      <branches>
        <hudson.plugins.git.BranchSpec>
          <name>*/${BRANCH_NAME}</name>
        </hudson.plugins.git.BranchSpec>
      </branches>
      <doGenerateSubmoduleConfigurations>false</doGenerateSubmoduleConfigurations>
      <submoduleCfg class="list"/>
      <extensions/>
    </scm>
    <scriptPath>deployment/Jenkinsfile.fullstack</scriptPath>
    <lightweight>true</lightweight>
  </definition>
  <triggers>
    <hudson.triggers.SCMTrigger>
      <spec>H/5 * * * *</spec>
      <ignorePostCommitHooks>false</ignorePostCommitHooks>
    </hudson.triggers.SCMTrigger>
  </triggers>
  <disabled>false</disabled>
</flow-definition>
EOF

# Get crumb for fullstack pipeline
CRUMB=$(curl -s -u "${JENKINS_USER}:${JENKINS_PASSWORD}" \
    "${JENKINS_URL}/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)")

curl -s -X POST "${JENKINS_URL}/createItem?name=fullstack-pipeline" \
     --user "${JENKINS_USER}:${JENKINS_PASSWORD}" \
     --header "${CRUMB}" \
     --header "Content-Type: application/xml" \
     --data-binary "@/tmp/fullstack-pipeline.xml" \
     > /tmp/fullstack-pipeline_result.txt 2>&1

if grep -q "already exists" /tmp/fullstack-pipeline_result.txt; then
    echo -e "${YELLOW}⚠ fullstack-pipeline already exists, skipping...${NC}"
elif grep -q "error\|Error\|ERROR" /tmp/fullstack-pipeline_result.txt; then
    echo -e "${RED}✗ Failed to create fullstack-pipeline${NC}"
    cat /tmp/fullstack-pipeline_result.txt
else
    echo -e "${GREEN}✓ Successfully created fullstack-pipeline${NC}"
fi

rm -f "/tmp/fullstack-pipeline.xml" "/tmp/fullstack-pipeline_result.txt"

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                    Setup Complete! ✓                       ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

echo -e "${BLUE}📋 Created Pipeline Jobs:${NC}"
echo "  1. user-service-pipeline"
echo "  2. product-service-pipeline"
echo "  3. media-service-pipeline"
echo "  4. api-gateway-pipeline"
echo "  5. frontend-pipeline"
echo "  6. fullstack-pipeline (master)"
echo ""

echo -e "${YELLOW}🌐 Access Jenkins:${NC}"
echo "  URL: ${JENKINS_URL}"
echo "  Username: ${JENKINS_USER}"
echo "  Password: ${JENKINS_PASSWORD}"
echo ""

echo -e "${YELLOW}🚀 Next Steps:${NC}"
echo "  1. Open Jenkins: ${JENKINS_URL}"
echo "  2. Verify GitHub credentials are configured"
echo "  3. Click on any pipeline job"
echo "  4. Click 'Build with Parameters' to test"
echo "  5. Or push to GitHub to trigger automatic build"
echo ""

echo -e "${GREEN}✨ Your CI/CD pipeline is ready to use!${NC}"
echo ""

# Optional: Trigger a test build
read -p "Would you like to trigger a test build of fullstack-pipeline? (y/n): " trigger_build

if [[ $trigger_build == "y" || $trigger_build == "Y" ]]; then
    echo ""
    echo -e "${YELLOW}Triggering fullstack-pipeline build...${NC}"
    
    curl -X POST "${JENKINS_URL}/job/fullstack-pipeline/buildWithParameters?ENVIRONMENT=dev" \
         --user "${JENKINS_USER}:${JENKINS_PASSWORD}" \
         2>/dev/null
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ Build triggered successfully!${NC}"
        echo -e "${BLUE}View progress: ${JENKINS_URL}/job/fullstack-pipeline/${NC}"
    else
        echo -e "${RED}✗ Failed to trigger build. You can do it manually from Jenkins UI.${NC}"
    fi
fi

echo ""
echo -e "${BLUE}📖 For detailed instructions, see: deployment/GITHUB_INTEGRATION_GUIDE.md${NC}"
echo ""
