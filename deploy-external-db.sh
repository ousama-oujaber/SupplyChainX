#!/bin/bash

# Quick Deploy Script for External Database Setup
# Run this on your DigitalOcean droplet

set -e

echo "🚀 SupplyChainX Backend Deployment (External DB)"
echo "================================================"
echo ""

# Step 1: Create .env file
if [ ! -f ".env" ]; then
    echo "📝 Creating .env file from template..."
    cat > .env << 'EOF'
# Docker Hub Configuration
DOCKER_HUB_USERNAME=your-dockerhub-username
IMAGE_TAG=backend

# External Database
DB_HOST=your-db-host
DB_PORT=3306
DB_NAME=supplychainx
DB_USER=your-db-user
DB_PASSWORD=your-db-password

# Keycloak
KEYCLOAK_URL=http://your-keycloak:8090/realms/supplychainx

# Application
APP_PORT=8080
EOF
    
    echo "✅ Created .env file"
    echo ""
    echo "⚠️  IMPORTANT: Edit .env with your actual values:"
    echo "   nano .env"
    echo ""
    read -p "Press Enter after you've edited the .env file..."
fi

# Step 2: Create docker-compose.yml
if [ ! -f "docker-compose.yml" ]; then
    echo "📝 Creating docker-compose.yml..."
    cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  app:
    image: ${DOCKER_HUB_USERNAME}/supplychainx:${IMAGE_TAG:-backend}
    container_name: supplychainx-app
    restart: unless-stopped
    ports:
      - "${APP_PORT:-8080}:8080"
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_LIQUIBASE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_LIQUIBASE_USER: ${DB_USER}
      SPRING_LIQUIBASE_PASSWORD: ${DB_PASSWORD}
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: ${KEYCLOAK_URL}
EOF
    echo "✅ Created docker-compose.yml"
fi

# Step 3: Test database connection
echo ""
echo "🔍 Testing database connection..."
source .env

if command -v mysql &> /dev/null; then
    if mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p${DB_PASSWORD} -e "USE ${DB_NAME};" 2>/dev/null; then
        echo "✅ Database connection successful!"
    else
        echo "⚠️  Cannot connect to database. Please check your credentials."
        read -p "Continue anyway? (y/n) " -n 1 -r
        echo
        if [[ ! $REPLY =~ ^[Yy]$ ]]; then
            exit 1
        fi
    fi
else
    echo "⚠️  MySQL client not installed. Skipping connection test."
fi

# Step 4: Pull and deploy
echo ""
echo "📦 Pulling latest image from Docker Hub..."
docker compose pull

echo ""
echo "🚀 Starting application..."
docker compose up -d

echo ""
echo "⏳ Waiting for application to start..."
sleep 5

# Step 5: Check status
echo ""
echo "📊 Application status:"
docker compose ps

echo ""
echo "📋 Recent logs:"
docker compose logs --tail=20 app

echo ""
echo "✅ Deployment complete!"
echo ""
echo "🌐 Your application should be accessible at:"
echo "   http://$(hostname -I | awk '{print $1}'):${APP_PORT}"
echo ""
echo "📖 Useful commands:"
echo "   docker compose logs -f app    # View logs"
echo "   docker compose restart app    # Restart app"
echo "   docker compose down           # Stop app"
echo "   docker compose pull && docker compose up -d  # Update app"
