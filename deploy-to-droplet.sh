#!/bin/bash

###############################################################################
# SupplyChainX Deployment Script for DigitalOcean Droplet
# 
# This script deploys the application to a remote server
# Usage: Called by Jenkins or can be run manually
###############################################################################

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
DROPLET_IP="${DROPLET_IP:-64.226.103.218}"
DROPLET_USER="${DROPLET_USER:-root}"
APP_NAME="supplychainx"
DOCKER_IMAGE="${DOCKER_IMAGE:-protocol404/supplychainx:latest}"
DEPLOY_DIR="/opt/supplychainx"

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║         SupplyChainX Deployment to DigitalOcean          ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to execute remote commands
remote_exec() {
    ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
        ${DROPLET_USER}@${DROPLET_IP} "$@"
}

# Function to copy files to remote server
remote_copy() {
    scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
        "$1" ${DROPLET_USER}@${DROPLET_IP}:"$2"
}

echo -e "${YELLOW}Step 1: Testing connection to droplet...${NC}"
if remote_exec "echo 'Connection successful!'"; then
    echo -e "${GREEN}✓ Connected to droplet: ${DROPLET_IP}${NC}"
else
    echo -e "${RED}✗ Failed to connect to droplet${NC}"
    exit 1
fi
echo ""

echo -e "${YELLOW}Step 2: Installing Docker and Docker Compose (if not installed)...${NC}"
remote_exec "
    # Install Docker if not present
    if ! command -v docker &> /dev/null; then
        echo 'Installing Docker...'
        apt-get update
        apt-get install -y apt-transport-https ca-certificates curl software-properties-common
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add -
        add-apt-repository 'deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable'
        apt-get update
        apt-get install -y docker-ce docker-ce-cli containerd.io
        systemctl enable docker
        systemctl start docker
        echo '✓ Docker installed'
    else
        echo '✓ Docker already installed'
    fi
    
    # Install Docker Compose if not present
    if ! command -v docker-compose &> /dev/null; then
        echo 'Installing Docker Compose...'
        curl -L \"https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-\$(uname -s)-\$(uname -m)\" -o /usr/local/bin/docker-compose
        chmod +x /usr/local/bin/docker-compose
        echo '✓ Docker Compose installed'
    else
        echo '✓ Docker Compose already installed'
    fi
"
echo -e "${GREEN}✓ Docker environment ready${NC}"
echo ""

echo -e "${YELLOW}Step 3: Creating deployment directory...${NC}"
remote_exec "mkdir -p ${DEPLOY_DIR}"
echo -e "${GREEN}✓ Deployment directory created: ${DEPLOY_DIR}${NC}"
echo ""

echo -e "${YELLOW}Step 4: Copying deployment files...${NC}"
echo "Copying docker-compose.prod.yaml..."
remote_copy "docker-compose.prod.yaml" "${DEPLOY_DIR}/docker-compose.yaml"

echo "Copying schema files..."
remote_exec "mkdir -p ${DEPLOY_DIR}/schema"
if [ -d "schema" ]; then
    scp -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \
        -r schema/* ${DROPLET_USER}@${DROPLET_IP}:${DEPLOY_DIR}/schema/
fi
echo -e "${GREEN}✓ Files copied${NC}"
echo ""

echo -e "${YELLOW}Step 5: Creating .env file on droplet...${NC}"
remote_exec "cat > ${DEPLOY_DIR}/.env << 'EOF'
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=supplychainx
MYSQL_USER=scx_user
MYSQL_PASSWORD=scx_pass
DOCKER_IMAGE=${DOCKER_IMAGE}
EOF
"
echo -e "${GREEN}✓ Environment file created${NC}"
echo ""

echo -e "${YELLOW}Step 6: Pulling latest Docker image...${NC}"
remote_exec "
    cd ${DEPLOY_DIR}
    docker pull ${DOCKER_IMAGE}
"
echo -e "${GREEN}✓ Docker image pulled${NC}"
echo ""

echo -e "${YELLOW}Step 7: Stopping old containers...${NC}"
remote_exec "
    cd ${DEPLOY_DIR}
    docker-compose down || true
"
echo -e "${GREEN}✓ Old containers stopped${NC}"
echo ""

echo -e "${YELLOW}Step 8: Starting new containers...${NC}"
remote_exec "
    cd ${DEPLOY_DIR}
    docker-compose up -d
"
echo -e "${GREEN}✓ New containers started${NC}"
echo ""

echo -e "${YELLOW}Step 9: Waiting for application to be healthy...${NC}"
echo -n "Waiting"
for i in {1..30}; do
    if remote_exec "curl -s http://localhost:8080/actuator/health | grep -q UP"; then
        echo ""
        echo -e "${GREEN}✓ Application is healthy!${NC}"
        break
    fi
    echo -n "."
    sleep 2
done
echo ""

echo -e "${YELLOW}Step 10: Verifying deployment...${NC}"
remote_exec "
    cd ${DEPLOY_DIR}
    docker-compose ps
"
echo ""

echo -e "${BLUE}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║               Deployment Completed Successfully           ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}Application URL: http://${DROPLET_IP}:8080${NC}"
echo -e "${GREEN}Health Check: http://${DROPLET_IP}:8080/actuator/health${NC}"
echo ""
echo "To view logs:"
echo "  ssh ${DROPLET_USER}@${DROPLET_IP} 'cd ${DEPLOY_DIR} && docker-compose logs -f'"
echo ""
echo "To restart application:"
echo "  ssh ${DROPLET_USER}@${DROPLET_IP} 'cd ${DEPLOY_DIR} && docker-compose restart'"
echo ""
