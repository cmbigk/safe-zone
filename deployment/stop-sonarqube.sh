#!/bin/bash

# Stop SonarQube services

echo "🛑 Stopping SonarQube..."

docker-compose -f docker-compose.sonarqube.yml down

echo "✅ SonarQube stopped successfully"
