#!/bin/bash

echo "Starting Jenkins CI/CD Server..."

# Create Jenkins data directory if it doesn't exist
mkdir -p jenkins-data

# Create network if it doesn't exist
docker network create buy-01_default 2>/dev/null || true

# Stop and remove existing Jenkins container to ensure clean start
echo "Cleaning up existing Jenkins container..."
docker-compose -f docker-compose.jenkins.yml down 2>/dev/null || true


# Start Jenkins
echo "Starting Jenkins with Configuration as Code..."
docker-compose -f docker-compose.jenkins.yml up -d

echo ""
echo "======================================"
echo "Jenkins is starting up..."
echo "======================================"
echo "Jenkins UI: http://localhost:8090"
echo ""
echo "Credentials (configured via JCasC):"
echo "  Username: admin"
echo "  Password: admin123"
echo ""
echo "Jenkins is configured with:"
echo "  ✓ Pre-configured admin user"
echo "  ✓ Git, Maven, JDK tools"
echo "  ✓ GitHub credentials placeholder"
echo ""
echo "Waiting for Jenkins to be ready..."
echo "This may take 1-2 minutes..."
echo ""

# Wait for Jenkins to be ready
max_attempts=60
attempt=0
while [ $attempt -lt $max_attempts ]; do
  if curl -s -f http://localhost:8090 > /dev/null 2>&1; then
    echo ""
    echo "✓ Jenkins is ready!"
    echo ""
    echo "Open: http://localhost:8090"
    echo "Login: admin / *****"
    echo ""
    break
  fi
  attempt=$((attempt + 1))
  echo -n "."
  sleep 2
done

if [ $attempt -eq $max_attempts ]; then
  echo ""
  echo "Jenkins is taking longer than expected."
  echo "Check logs: docker logs -f jenkins-ci"
fi

echo ""
echo "Useful commands:"
echo "  View logs:    docker logs -f jenkins-ci"
echo "  Stop Jenkins: ./stop-jenkins.sh"
echo "======================================"
