# SupplyChainX - Continuous Deployment Setup Guide

## 🎯 Overview

This guide explains how to set up Continuous Deployment (CD) to automatically deploy the SupplyChainX application to a DigitalOcean Droplet after successful CI builds.

---

## 📋 Prerequisites

### 1. DigitalOcean Droplet
- **IP Address:** 64.226.103.218
- **OS:** Ubuntu 20.04 or later
- **RAM:** Minimum 2GB (4GB recommended)
- **Storage:** Minimum 20GB

### 2. SSH Access
- SSH key pair for secure authentication
- Root or sudo access

### 3. Jenkins Configuration
- Jenkins running and accessible
- Docker plugin installed
- SSH Agent plugin installed

---

## 🔧 Setup Steps

### Step 1: Generate SSH Key for Jenkins

On your Jenkins server (or local machine), generate an SSH key:

```bash
# Generate SSH key pair
ssh-keygen -t rsa -b 4096 -C "jenkins@supplychainx" -f ~/.ssh/droplet_key -N ""

# This creates:
# - ~/.ssh/droplet_key (private key)
# - ~/.ssh/droplet_key.pub (public key)
```

### Step 2: Add SSH Public Key to Droplet

Copy the public key to your droplet:

```bash
# Display the public key
cat ~/.ssh/droplet_key.pub

# Copy the output, then connect to droplet
ssh root@64.226.103.218

# On the droplet, add the public key to authorized_keys
mkdir -p ~/.ssh
echo "YOUR_PUBLIC_KEY_HERE" >> ~/.ssh/authorized_keys
chmod 700 ~/.ssh
chmod 600 ~/.ssh/authorized_keys
```

Or use ssh-copy-id:

```bash
ssh-copy-id -i ~/.ssh/droplet_key.pub root@64.226.103.218
```

### Step 3: Test SSH Connection

```bash
# Test connection without password
ssh -i ~/.ssh/droplet_key root@64.226.103.218

# If successful, you should connect without being prompted for password
```

### Step 4: Add SSH Credentials to Jenkins

1. **Go to Jenkins Dashboard**
   - Navigate to: `Manage Jenkins` → `Manage Credentials`

2. **Add New Credential**
   - Domain: `Global`
   - Kind: `SSH Username with private key`
   - ID: `droplet-ssh-key`
   - Description: `DigitalOcean Droplet SSH Key`
   - Username: `root`
   - Private Key: `Enter directly`
   - Paste the content of `~/.ssh/droplet_key`
   - Click `Create`

### Step 5: Configure Jenkins Pipeline

The Jenkinsfile has been updated with:
- ✅ Deploy to Production stage
- ✅ Smoke Test stage
- ✅ Deployment only on `main` branch

### Step 6: Prepare the Droplet (Manual - First Time Only)

```bash
# Connect to droplet
ssh root@64.226.103.218

# Update system
apt-get update && apt-get upgrade -y

# Install required packages
apt-get install -y curl vim git ufw

# Configure firewall
ufw allow 22/tcp    # SSH
ufw allow 8080/tcp  # Application
ufw allow 3306/tcp  # MySQL (if needed externally)
ufw --force enable

# The deployment script will install Docker and Docker Compose automatically
```

---

## 🚀 Deployment Process

### Automatic Deployment (via Jenkins)

When you push to the `main` branch:

1. **CI Pipeline Runs:**
   - Checkout
   - Build
   - Test
   - Code Quality Analysis
   - Package
   - Build Docker Image
   - Push to Docker Hub

2. **CD Pipeline Triggers:**
   - Deploy to Production stage executes
   - Connects to droplet via SSH
   - Installs Docker (if needed)
   - Copies deployment files
   - Pulls latest Docker image
   - Stops old containers
   - Starts new containers
   - Runs smoke tests

### Manual Deployment

If you need to deploy manually:

```bash
# From your local machine or Jenkins server
cd /home/protocol/IdeaProjects/docker/prod/SupplyChainX

# Make script executable
chmod +x deploy-to-droplet.sh

# Set environment variables
export DROPLET_IP="64.226.103.218"
export DROPLET_USER="root"
export DOCKER_IMAGE="protocol404/supplychainx:latest"

# Run deployment
./deploy-to-droplet.sh
```

---

## 📁 Deployment Files

### 1. docker-compose.prod.yaml
Production Docker Compose configuration with:
- MySQL 8.0 database
- SupplyChainX application
- Health checks
- Persistent volumes
- Network configuration

### 2. deploy-to-droplet.sh
Automated deployment script that:
- Tests connection to droplet
- Installs Docker/Docker Compose if needed
- Creates deployment directory
- Copies necessary files
- Pulls latest Docker image
- Deploys application
- Verifies deployment

### 3. Jenkinsfile
Updated with CD stages:
- Deploy to Production (only on main branch)
- Smoke Test (validates deployment)

---

## 🔍 Verification

After deployment, verify the application:

### 1. Check Application Health

```bash
# From anywhere
curl http://64.226.103.218:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

### 2. Check Docker Containers

```bash
# Connect to droplet
ssh root@64.226.103.218

# Check running containers
cd /opt/supplychainx
docker-compose ps

# Expected output:
# NAME                        STATUS              PORTS
# supplychainx-app-prod       Up (healthy)        0.0.0.0:8080->8080/tcp
# supplychainx-mysql-prod     Up (healthy)        0.0.0.0:3306->3306/tcp
```

### 3. View Application Logs

```bash
# On the droplet
cd /opt/supplychainx
docker-compose logs -f app

# Or check specific service
docker-compose logs -f mysql
```

### 4. Test API Endpoints

```bash
# Test API (replace with your actual endpoints)
curl http://64.226.103.218:8080/api/products
curl http://64.226.103.218:8080/api/suppliers
```

---

## 🛠️ Troubleshooting

### Issue 1: SSH Connection Failed

**Symptoms:** Jenkins can't connect to droplet

**Solutions:**
```bash
# Verify SSH key is added to Jenkins credentials
# Check droplet SSH key:
ssh root@64.226.103.218 cat ~/.ssh/authorized_keys

# Test connection manually:
ssh -i ~/.ssh/droplet_key root@64.226.103.218

# Check firewall:
ssh root@64.226.103.218 'ufw status'
```

### Issue 2: Docker Not Installed on Droplet

**Symptoms:** Docker commands fail

**Solutions:**
```bash
# The deployment script should auto-install Docker
# If it fails, install manually:
ssh root@64.226.103.218

curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
systemctl enable docker
systemctl start docker

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/download/v2.20.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
```

### Issue 3: Application Won't Start

**Symptoms:** Containers exit or health check fails

**Solutions:**
```bash
# Check logs
ssh root@64.226.103.218
cd /opt/supplychainx
docker-compose logs app

# Common issues:
# 1. Database not ready - wait longer
# 2. Wrong environment variables
# 3. Port already in use

# Restart services
docker-compose restart
```

### Issue 4: Can't Access Application

**Symptoms:** HTTP request timeouts

**Solutions:**
```bash
# Check firewall
ssh root@64.226.103.218 'ufw status'

# Open port 8080 if needed
ssh root@64.226.103.218 'ufw allow 8080/tcp'

# Check if app is listening
ssh root@64.226.103.218 'netstat -tuln | grep 8080'

# Test from droplet
ssh root@64.226.103.218 'curl http://localhost:8080/actuator/health'
```

### Issue 5: Database Connection Failed

**Symptoms:** Application logs show MySQL connection errors

**Solutions:**
```bash
# Check MySQL container
ssh root@64.226.103.218
cd /opt/supplychainx
docker-compose logs mysql

# Check MySQL is healthy
docker-compose ps

# Restart MySQL
docker-compose restart mysql

# Check environment variables
docker-compose exec app env | grep SPRING_DATASOURCE
```

---

## 🔄 Rollback Procedure

If deployment fails and you need to rollback:

### Quick Rollback

```bash
# Connect to droplet
ssh root@64.226.103.218
cd /opt/supplychainx

# Pull previous version
docker pull protocol404/supplychainx:PREVIOUS_BUILD_NUMBER

# Update docker-compose.yaml to use previous version
# Then restart
docker-compose down
docker-compose up -d
```

### From Jenkins

1. Find the last successful build number
2. Go to that build
3. Click "Rebuild"
4. Or manually deploy:

```bash
export DOCKER_IMAGE="protocol404/supplychainx:PREVIOUS_BUILD_NUMBER"
./deploy-to-droplet.sh
```

---

## 📊 Monitoring

### Application Health

```bash
# Health check endpoint
curl http://64.226.103.218:8080/actuator/health

# Detailed health
curl http://64.226.103.218:8080/actuator/health/readiness
curl http://64.226.103.218:8080/actuator/health/liveness
```

### Resource Usage

```bash
# Connect to droplet
ssh root@64.226.103.218

# Check Docker resource usage
docker stats

# Check disk space
df -h

# Check memory
free -h

# Check system load
uptime
```

### Logs

```bash
# Application logs
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose logs -f --tail=100 app'

# MySQL logs
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose logs -f --tail=100 mysql'

# All logs
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose logs -f --tail=100'
```

---

## 🔐 Security Best Practices

### 1. SSH Security

```bash
# On droplet, edit SSH config
ssh root@64.226.103.218
nano /etc/ssh/sshd_config

# Recommended settings:
PermitRootLogin prohibit-password
PasswordAuthentication no
PubkeyAuthentication yes

# Restart SSH
systemctl restart sshd
```

### 2. Firewall Configuration

```bash
# Allow only necessary ports
ufw default deny incoming
ufw default allow outgoing
ufw allow 22/tcp
ufw allow 8080/tcp
ufw enable
```

### 3. Database Security

- Keep database internal (don't expose port 3306 publicly)
- Use strong passwords
- Regular backups

### 4. Application Security

- Use HTTPS (add nginx reverse proxy)
- Keep Docker images updated
- Regular security updates

---

## 📝 Maintenance

### Regular Tasks

```bash
# Clean up Docker
ssh root@64.226.103.218 'docker system prune -a -f'

# Update system
ssh root@64.226.103.218 'apt-get update && apt-get upgrade -y'

# Backup database
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose exec mysql mysqldump -u root -proot supplychainx > backup_$(date +%Y%m%d).sql'

# Check disk space
ssh root@64.226.103.218 'df -h'
```

---

## 📚 Next Steps

1. ✅ Set up SSL/TLS with Let's Encrypt
2. ✅ Configure nginx as reverse proxy
3. ✅ Set up automated backups
4. ✅ Configure monitoring (Prometheus/Grafana)
5. ✅ Set up log aggregation (ELK stack)
6. ✅ Implement blue-green deployment

---

## 📞 Support

For issues or questions:
- Check logs first
- Review troubleshooting section
- Contact DevOps team

---

**Last Updated:** November 16, 2025  
**Version:** 1.0  
**Status:** ✅ Production Ready
