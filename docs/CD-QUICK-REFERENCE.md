# CD Deployment - Quick Reference

## 🚀 Quick Start

### 1. One-Command Setup

```bash
cd /home/protocol/IdeaProjects/docker/prod/SupplyChainX
./docs/script/setup-cd.sh
```

This script will:
- ✅ Generate SSH key pair
- ✅ Copy public key to droplet
- ✅ Test SSH connection
- ✅ Display private key for Jenkins
- ✅ Optionally prepare droplet (install Docker, setup firewall)

---

## 📋 Manual Setup (Alternative)

### Step 1: Generate SSH Key

```bash
ssh-keygen -t rsa -b 4096 -C "jenkins@supplychainx" -f ~/.ssh/supplychainx_droplet_key -N ""
```

### Step 2: Copy to Droplet

```bash
ssh-copy-id -i ~/.ssh/supplychainx_droplet_key.pub root@64.226.103.218
# Password: 3R5rCr3}m8UZ~4t
```

### Step 3: Test Connection

```bash
ssh -i ~/.ssh/supplychainx_droplet_key root@64.226.103.218
```

### Step 4: Add to Jenkins

1. Go to: `Jenkins → Manage Jenkins → Manage Credentials`
2. Click: `(global)` → `Add Credentials`
3. Configure:
   - **Kind:** SSH Username with private key
   - **ID:** `droplet-ssh-key`
   - **Username:** `root`
   - **Private Key:** Enter directly
   - Paste content of `~/.ssh/supplychainx_droplet_key`
4. Click `Create`

---

## 🔧 Jenkins Configuration Checklist

- [ ] SSH credentials added (ID: `droplet-ssh-key`)
- [ ] Docker Hub credentials exist (ID: `dockerhub-credentials`)
- [ ] SSH Agent plugin installed
- [ ] Jenkinsfile updated with CD stages
- [ ] Main branch configured in Jenkins

---

## 📦 Deployment Files

| File | Purpose |
|------|---------|
| `Jenkinsfile` | CI/CD pipeline with Deploy stage |
| `deploy-to-droplet.sh` | Automated deployment script |
| `docker-compose.prod.yaml` | Production Docker configuration |
| `docs/script/setup-cd.sh` | One-command setup script |

---

## 🎯 Deployment Flow

```
Push to main → Jenkins Webhook → CI Pipeline → Build Docker Image → 
Push to Docker Hub → Deploy to Droplet → Smoke Tests → ✅ Done!
```

### Stages

1. **Checkout** - Pull code from GitHub
2. **Build** - Compile with Maven
3. **Test** - Run unit tests
4. **Code Quality** - SonarQube analysis
5. **Package** - Create JAR file
6. **Build Docker Image** - Create container image
7. **Push Docker Image** - Upload to Docker Hub
8. **Deploy to Production** - Deploy to droplet (main branch only)
9. **Smoke Test** - Verify deployment (main branch only)

---

## ✅ Verification Commands

### Check Application

```bash
# Health check
curl http://64.226.103.218:8080/actuator/health

# Expected: {"status":"UP"}
```

### Check Containers on Droplet

```bash
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose ps'
```

### View Logs

```bash
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose logs -f app'
```

---

## 🔄 Manual Deployment

If you need to deploy without Jenkins:

```bash
cd /home/protocol/IdeaProjects/docker/prod/SupplyChainX

export DROPLET_IP="64.226.103.218"
export DROPLET_USER="root"
export DOCKER_IMAGE="protocol404/supplychainx:latest"

./deploy-to-droplet.sh
```

---

## 🐛 Quick Troubleshooting

### Issue: SSH Connection Failed

```bash
# Test SSH manually
ssh -i ~/.ssh/supplychainx_droplet_key root@64.226.103.218

# Check droplet authorized_keys
ssh root@64.226.103.218 'cat ~/.ssh/authorized_keys'
```

### Issue: Application Not Responding

```bash
# Check if app is running
ssh root@64.226.103.218 'docker ps'

# Check logs
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose logs app'

# Restart app
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose restart app'
```

### Issue: Health Check Failed

```bash
# Wait 60 seconds for app to start
sleep 60

# Then check
curl http://64.226.103.218:8080/actuator/health

# Check app logs
ssh root@64.226.103.218 'cd /opt/supplychainx && docker-compose logs --tail=50 app'
```

---

## 📊 Droplet Information

| Property | Value |
|----------|-------|
| **IP Address** | 64.226.103.218 |
| **Username** | root |
| **Application URL** | http://64.226.103.218:8080 |
| **Health Endpoint** | http://64.226.103.218:8080/actuator/health |
| **Deploy Directory** | /opt/supplychainx |

---

## 🔐 Security Notes

- ✅ SSH key authentication (no password)
- ✅ Firewall configured (ports 22, 8080)
- ✅ Database not exposed externally
- ⚠️ Consider adding HTTPS/SSL
- ⚠️ Consider setting up non-root user

---

## 📚 Documentation

- **Full Guide:** `docs/CD-DEPLOYMENT-GUIDE.md`
- **Setup Script:** `docs/script/setup-cd.sh`
- **Deployment Script:** `deploy-to-droplet.sh`
- **Docker Compose:** `docker-compose.prod.yaml`

---

## 🎯 Next Steps After Setup

1. **Run setup script:**
   ```bash
   ./docs/script/setup-cd.sh
   ```

2. **Add credentials to Jenkins:**
   - Follow the on-screen instructions

3. **Commit and push:**
   ```bash
   git add .
   git commit -m "Add CD deployment to DigitalOcean droplet"
   git push origin feature/cicd-pipeline
   ```

4. **Merge to main:**
   - Create pull request
   - Review and merge
   - Watch automatic deployment!

5. **Verify deployment:**
   ```bash
   curl http://64.226.103.218:8080/actuator/health
   ```

---

**Last Updated:** November 16, 2025  
**Status:** Ready for Setup  
**Droplet:** 64.226.103.218
