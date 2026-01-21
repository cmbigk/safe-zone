#!/bin/bash

# Start SonarQube with Docker Compose
# This script ensures proper system configuration for SonarQube

echo "🚀 Starting SonarQube..."

# Check if running on macOS or Linux and set vm.max_map_count if needed
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    echo "Checking system configuration..."
    current_max_map_count=$(sysctl -n vm.max_map_count)
    if [ "$current_max_map_count" -lt 262144 ]; then
        echo "Setting vm.max_map_count to 262144..."
        sudo sysctl -w vm.max_map_count=262144
    fi
fi

# Start services
docker-compose -f docker-compose.sonarqube.yml up -d

echo ""
echo "✅ SonarQube is starting..."
echo ""
echo "📊 Access SonarQube at: http://localhost:9000"
echo "🔐 Default credentials:"
echo "   Username: admin"
echo "   Password: admin"
echo ""
echo "⏳ Please wait 1-2 minutes for SonarQube to fully start"
echo ""
echo "📝 To check logs: docker logs -f sonarqube"
echo "🛑 To stop: ./stop-sonarqube.sh"
