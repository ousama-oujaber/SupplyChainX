#!/bin/bash

# Jenkins CI/CD Helper Script for SupplyChainX
# This script helps with common Jenkins operations

set -e

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║  SupplyChainX Jenkins Helper Script   ║${NC}"
echo -e "${GREEN}╔════════════════════════════════════════╗${NC}"
echo ""

# Function to get Jenkins initial password
get_jenkins_password() {
    echo -e "${YELLOW}📋 Getting Jenkins initial admin password...${NC}"
    docker exec supplychainx-jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || {
        echo -e "${RED}❌ Jenkins container not found or already configured${NC}"
        return 1
    }
    echo ""
}

# Function to check Jenkins status
check_jenkins_status() {
    echo -e "${YELLOW}🔍 Checking Jenkins status...${NC}"
    if docker ps | grep -q supplychainx-jenkins; then
        echo -e "${GREEN}✅ Jenkins is running${NC}"
        echo -e "   URL: http://localhost:8090"
        echo ""
        docker ps --filter name=supplychainx-jenkins --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    else
        echo -e "${RED}❌ Jenkins is not running${NC}"
        echo -e "   Start it with: docker-compose -f docker-compose.jenkins.yaml up -d"
    fi
    echo ""
}

# Function to view Jenkins logs
view_jenkins_logs() {
    echo -e "${YELLOW}📜 Viewing Jenkins logs (Ctrl+C to exit)...${NC}"
    docker logs -f supplychainx-jenkins
}

# Function to restart Jenkins
restart_jenkins() {
    echo -e "${YELLOW}🔄 Restarting Jenkins...${NC}"
    docker restart supplychainx-jenkins
    echo -e "${GREEN}✅ Jenkins restarted${NC}"
    echo ""
}

# Function to validate Jenkinsfile
validate_jenkinsfile() {
    echo -e "${YELLOW}✅ Validating Jenkinsfile...${NC}"
    if [ ! -f "Jenkinsfile" ]; then
        echo -e "${RED}❌ Jenkinsfile not found in current directory${NC}"
        return 1
    fi
    
    # Basic syntax check (you can enhance this)
    if grep -q "pipeline" Jenkinsfile && grep -q "stages" Jenkinsfile; then
        echo -e "${GREEN}✅ Jenkinsfile basic structure looks good${NC}"
    else
        echo -e "${RED}⚠️  Jenkinsfile might have issues${NC}"
    fi
    echo ""
}

# Function to trigger a build
trigger_build() {
    echo -e "${YELLOW}🚀 This will help you trigger a build...${NC}"
    echo -e "   Manual method: Go to http://localhost:8090 and click 'Build Now'"
    echo -e "   Or push changes to GitHub to trigger webhook"
    echo ""
}

# Function to setup GitHub webhook
setup_github_webhook() {
    echo -e "${YELLOW}🔗 GitHub Webhook Setup Instructions:${NC}"
    echo ""
    echo "1. Go to: https://github.com/ousama-oujaber/SupplyChainX/settings/hooks"
    echo "2. Click 'Add webhook'"
    echo "3. Payload URL: http://YOUR_PUBLIC_IP:8090/github-webhook/"
    echo "4. Content type: application/json"
    echo "5. Select events: Push, Pull requests"
    echo "6. Click 'Add webhook'"
    echo ""
    echo -e "${YELLOW}💡 For local development, use ngrok:${NC}"
    echo "   ngrok http 8090"
    echo "   Then use the ngrok URL for webhook"
    echo ""
}

# Function to check Docker Hub credentials
check_dockerhub() {
    echo -e "${YELLOW}🐳 Checking Docker Hub connection...${NC}"
    if docker info | grep -q "Username"; then
        echo -e "${GREEN}✅ Docker is logged in${NC}"
        docker info | grep "Username"
    else
        echo -e "${RED}⚠️  Not logged into Docker Hub${NC}"
        echo "   Login with: docker login"
    fi
    echo ""
}

# Function to test GitHub connection
test_github_connection() {
    echo -e "${YELLOW}🔗 Testing GitHub repository access...${NC}"
    if git ls-remote https://github.com/ousama-oujaber/SupplyChainX.git HEAD &>/dev/null; then
        echo -e "${GREEN}✅ GitHub repository is accessible${NC}"
    else
        echo -e "${RED}❌ Cannot access GitHub repository${NC}"
        echo "   Check repository name and permissions"
    fi
    echo ""
}

# Function to show build artifacts
show_artifacts() {
    echo -e "${YELLOW}📦 Listing recent build artifacts...${NC}"
    if [ -d "target" ]; then
        ls -lh target/*.jar 2>/dev/null || echo "No JAR files found"
    else
        echo "No target directory found. Run 'mvn package' first."
    fi
    echo ""
}

# Function to clean build
clean_build() {
    echo -e "${YELLOW}🧹 Cleaning build artifacts...${NC}"
    mvn clean
    echo -e "${GREEN}✅ Build cleaned${NC}"
    echo ""
}

# Function to show pipeline stages
show_pipeline_info() {
    echo -e "${YELLOW}📊 Pipeline Stages:${NC}"
    echo ""
    echo "1. 📦 Checkout - Pull code from GitHub"
    echo "2. 🔨 Build - Compile with Maven"
    echo "3. 🧪 Test - Run unit tests"
    echo "4. 📊 Code Quality - Code analysis"
    echo "5. 📦 Package - Create JAR"
    echo "6. 🐳 Docker Build - Build image"
    echo "7. 📤 Push - Push to Docker Hub"
    echo "8. 🔒 Security Scan - Vulnerability check"
    echo "9. 🚀 Deploy - Deploy to environment"
    echo "10. 🧪 Integration Tests - Post-deployment tests"
    echo "11. 🧹 Cleanup - Clean resources"
    echo ""
}

# Function to display quick setup checklist
show_checklist() {
    echo -e "${YELLOW}✅ Jenkins Setup Checklist:${NC}"
    echo ""
    echo "[ ] 1. Access Jenkins at http://localhost:8090"
    echo "[ ] 2. Get initial password (option 1)"
    echo "[ ] 3. Install suggested plugins"
    echo "[ ] 4. Configure JDK-17 and Maven-3.9.6"
    echo "[ ] 5. Add GitHub credentials"
    echo "[ ] 6. Add Docker Hub credentials"
    echo "[ ] 7. Create Multibranch Pipeline"
    echo "[ ] 8. Configure GitHub webhook"
    echo "[ ] 9. Update Jenkinsfile with your Docker Hub username"
    echo "[ ] 10. Push Jenkinsfile to GitHub"
    echo "[ ] 11. Trigger first build"
    echo ""
}

# Main menu
show_menu() {
    echo -e "${GREEN}Choose an option:${NC}"
    echo "1.  Get Jenkins initial password"
    echo "2.  Check Jenkins status"
    echo "3.  View Jenkins logs"
    echo "4.  Restart Jenkins"
    echo "5.  Validate Jenkinsfile"
    echo "6.  Trigger build (instructions)"
    echo "7.  Setup GitHub webhook (instructions)"
    echo "8.  Check Docker Hub connection"
    echo "9.  Test GitHub connection"
    echo "10. Show build artifacts"
    echo "11. Clean build"
    echo "12. Show pipeline info"
    echo "13. Show setup checklist"
    echo "14. Exit"
    echo ""
    read -p "Enter choice [1-14]: " choice
    
    case $choice in
        1) get_jenkins_password ;;
        2) check_jenkins_status ;;
        3) view_jenkins_logs ;;
        4) restart_jenkins ;;
        5) validate_jenkinsfile ;;
        6) trigger_build ;;
        7) setup_github_webhook ;;
        8) check_dockerhub ;;
        9) test_github_connection ;;
        10) show_artifacts ;;
        11) clean_build ;;
        12) show_pipeline_info ;;
        13) show_checklist ;;
        14) echo "Goodbye!"; exit 0 ;;
        *) echo -e "${RED}Invalid option${NC}" ;;
    esac
    
    echo ""
    read -p "Press Enter to continue..."
    clear
    show_menu
}

# Start the script
clear
show_menu
