#!/bin/bash

# Jenkins Admin Password Change Script
# This script helps change the Jenkins admin password securely

set -e

echo "============================================"
echo "Jenkins Admin Password Change Script"
echo "============================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if Jenkins is running
if ! docker ps | grep -q jenkins-ci; then
    echo -e "${RED}❌ Error: Jenkins container is not running${NC}"
    echo "Please start Jenkins first: ./start-jenkins.sh"
    exit 1
fi

echo -e "${GREEN}✓${NC} Jenkins container is running"
echo ""

# Get current password
echo "Enter current admin password (default: admin123):"
read -s CURRENT_PASSWORD
echo ""

# Validate current password
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" -u admin:${CURRENT_PASSWORD} http://localhost:8090/api/json)

if [ "$HTTP_CODE" != "200" ]; then
    echo -e "${RED}❌ Error: Current password is incorrect${NC}"
    exit 1
fi

echo -e "${GREEN}✓${NC} Current password verified"
echo ""

# Get new password
echo "Enter new admin password (minimum 16 characters recommended):"
read -s NEW_PASSWORD
echo ""

echo "Confirm new admin password:"
read -s NEW_PASSWORD_CONFIRM
echo ""

if [ "$NEW_PASSWORD" != "$NEW_PASSWORD_CONFIRM" ]; then
    echo -e "${RED}❌ Error: Passwords do not match${NC}"
    exit 1
fi

# Check password strength
if [ ${#NEW_PASSWORD} -lt 12 ]; then
    echo -e "${YELLOW}⚠️  Warning: Password is less than 12 characters${NC}"
    echo "Recommended: Use at least 16 characters with mix of uppercase, lowercase, numbers, and symbols"
    echo ""
    read -p "Continue anyway? (yes/no): " CONTINUE
    if [ "$CONTINUE" != "yes" ]; then
        echo "Password change cancelled"
        exit 0
    fi
fi

echo -e "${GREEN}✓${NC} Passwords match"
echo ""

# Generate Groovy script to change password
GROOVY_SCRIPT=$(cat <<EOF
import jenkins.model.Jenkins
import hudson.security.HudsonPrivateSecurityRealm

def instance = Jenkins.getInstance()
def hudsonRealm = instance.getSecurityRealm()

if (hudsonRealm instanceof HudsonPrivateSecurityRealm) {
    def user = hudsonRealm.getUser('admin')
    if (user) {
        user.save()
        hudsonRealm.createAccount('admin', '${NEW_PASSWORD}')
        println 'Password changed successfully'
    } else {
        println 'Error: admin user not found'
    }
} else {
    println 'Error: Jenkins is not using local user database'
}
EOF
)

echo "Changing password..."

# Execute groovy script via Jenkins API
RESPONSE=$(docker exec jenkins-ci java -jar /var/jenkins_home/war/WEB-INF/jenkins-cli.jar -s http://localhost:8090/ -auth admin:${CURRENT_PASSWORD} groovy = <<< "$GROOVY_SCRIPT" 2>&1)

if echo "$RESPONSE" | grep -q "Password changed successfully"; then
    echo ""
    echo -e "${GREEN}============================================${NC}"
    echo -e "${GREEN}✅ Password Changed Successfully!${NC}"
    echo -e "${GREEN}============================================${NC}"
    echo ""
    echo "New admin credentials:"
    echo "  Username: admin"
    echo "  Password: [the password you entered]"
    echo ""
    echo -e "${YELLOW}IMPORTANT:${NC}"
    echo "1. Store the new password in your password manager"
    echo "2. Update any automation scripts that use the old password"
    echo "3. Inform team members of the password change"
    echo ""
    echo "Test login: http://localhost:8090"
    
    # Update jenkins.yaml if it exists
    if [ -f "jenkins-config/jenkins.yaml" ]; then
        echo ""
        read -p "Update jenkins.yaml with new password hash? (yes/no): " UPDATE_YAML
        if [ "$UPDATE_YAML" = "yes" ]; then
            echo -e "${YELLOW}Note: jenkins.yaml should use password hash, not plaintext${NC}"
            echo "Consider removing password from jenkins.yaml and managing users via UI"
        fi
    fi
else
    echo -e "${RED}❌ Error changing password${NC}"
    echo "Response: $RESPONSE"
    echo ""
    echo "Alternative method:"
    echo "1. Go to http://localhost:8090"
    echo "2. Login as admin"
    echo "3. Click 'admin' (top right) → Configure"
    echo "4. Enter new password in Password section"
    echo "5. Click Save"
    exit 1
fi

echo ""
echo -e "${GREEN}✓${NC} Password change complete!"
