#!/bin/bash
# Jenkins Manual Build Trigger Script
# Usage: ./jenkins-manual-build.sh

JENKINS_URL="http://localhost:8090"
JOB_NAME="SupplyChainX-Manual-Build"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🚀 Triggering Jenkins build for ${JOB_NAME}...${NC}"

# Check if Jenkins is accessible
if ! curl -s -o /dev/null -w "%{http_code}" "${JENKINS_URL}" | grep -q "200\|403"; then
    echo -e "${RED}❌ Error: Jenkins is not accessible at ${JENKINS_URL}${NC}"
    echo "Make sure Jenkins is running: docker ps | grep jenkins"
    exit 1
fi

echo -e "${GREEN}✅ Jenkins is accessible${NC}"

# Trigger the build
echo -e "${YELLOW}📦 Starting build...${NC}"
curl -X POST "${JENKINS_URL}/job/${JOB_NAME}/build" \
     --user "admin:YOUR_PASSWORD_HERE" \
     -H "Jenkins-Crumb:YOUR_CRUMB_HERE" 2>/dev/null

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Build triggered successfully!${NC}"
    echo -e "View build at: ${JENKINS_URL}/job/${JOB_NAME}/"
    
    # Optional: Open in browser
    echo -e "\n${YELLOW}Opening Jenkins in browser...${NC}"
    xdg-open "${JENKINS_URL}/job/${JOB_NAME}/" 2>/dev/null || \
    open "${JENKINS_URL}/job/${JOB_NAME}/" 2>/dev/null || \
    echo "Open manually: ${JENKINS_URL}/job/${JOB_NAME}/"
else
    echo -e "${RED}❌ Failed to trigger build${NC}"
    exit 1
fi
