# Quick Deployment on backend-1 Droplet

## Step-by-Step Deployment Guide

Follow these exact commands on your **backend-1** droplet (165.22.84.200).

---

## Step 1: SSH to Your Droplet

```bash
ssh root@165.22.84.200
```

---

## Step 2: Create Deployment Directory

```bash
mkdir -p ~/supplychainx
cd ~/supplychainx
```

---

## Step 3: Create docker-compose.yml File

Copy and paste this entire command:

```bash
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
      # Database Configuration
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME}?useSSL=${DB_SSL:-false}&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      
      # Liquibase Configuration
      SPRING_LIQUIBASE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT:-3306}/${DB_NAME}?useSSL=${DB_SSL:-false}&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_LIQUIBASE_USER: ${DB_USER}
      SPRING_LIQUIBASE_PASSWORD: ${DB_PASSWORD}
      
      # Keycloak Configuration
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: ${KEYCLOAK_URL}
      
      # CORS Configuration
      CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS}
EOF
```

---

## Step 4: Create .env File (IMPORTANT - Fill in Your Values!)

```bash
nano .env
```

Copy and paste this, then **REPLACE with your actual values**:

```bash
# ===================================
# Docker Hub Configuration
# ===================================
DOCKER_HUB_USERNAME=ousama-oujaber
IMAGE_TAG=backend

# ===================================
# Database Configuration
# ===================================
# Get these from DigitalOcean Dashboard → Databases → backend-1 → Connection Details (PRIVATE)
DB_HOST=private-backend-1-do-user-XXXXX-xxx.db.ondigitalocean.com
DB_PORT=25060
DB_NAME=defaultdb
DB_USER=doadmin
DB_PASSWORD=REPLACE_WITH_YOUR_DATABASE_PASSWORD
DB_SSL=true

# ===================================
# Keycloak Configuration
# ===================================
# Where is Keycloak running? Use private IP if on same VPC
KEYCLOAK_URL=http://10.114.16.2:8090/realms/supplychainx

# ===================================
# Application Configuration
# ===================================
APP_PORT=8080

# ===================================
# CORS Configuration
# ===================================
# Your proxy and frontend IPs
CORS_ALLOWED_ORIGINS=http://134.209.249.179,https://134.209.249.179,http://157.230.115.167,https://157.230.115.167,http://localhost:4200,https://localhost:4200
```

**IMPORTANT**: 
- Replace `XXXXX` in DB_HOST with your actual database host (from DigitalOcean)
- Replace `REPLACE_WITH_YOUR_DATABASE_PASSWORD` with your real password
- Press `Ctrl+X`, then `Y`, then `Enter` to save

---

## Step 5: Get Your Database Connection Details

1. Open DigitalOcean Dashboard
2. Go to **Databases** → Click **backend-1**
3. Click **Connection Details**
4. Select **Connection String** → Choose **Private** connection
5. Copy the values:
   - **Host**: Starts with `private-backend-1-...`
   - **Port**: Should be `25060`
   - **User**: Usually `doadmin`
   - **Password**: Click to reveal
   - **Database**: Usually `defaultdb`

Update your `.env` file with these values:
```bash
nano .env
```

---

## Step 6: Pull the Docker Image

```bash
docker pull ousama-oujaber/supplychainx:backend
```

---

## Step 7: Start the Application

```bash
docker compose up -d
```

---

## Step 8: Check if It's Running

```bash
# Check container status
docker compose ps

# View logs
docker compose logs -f app

# Test health endpoint
curl http://localhost:8080/actuator/health
```

You should see:
```json
{"status":"UP"}
```

Press `Ctrl+C` to stop viewing logs.

---

## Step 9: Test from VPC

From any machine in your VPC (or from proxy-1):

```bash
curl http://10.114.16.3:8080/actuator/health
```

---

## ✅ You're Done!

Your backend is now running on **backend-1** (10.114.16.3:8080)

---

## Updating the Application

When you push new code and GitHub Actions builds a new image:

```bash
# SSH to droplet
ssh root@165.22.84.200

# Navigate to directory
cd ~/supplychainx

# Pull latest image
docker compose pull

# Restart with new image
docker compose up -d

# Check logs
docker compose logs -f app
```

---

## Useful Commands

```bash
# View logs (real-time)
docker compose logs -f app

# View last 100 lines
docker compose logs --tail=100 app

# Restart application
docker compose restart app

# Stop application
docker compose down

# Check container status
docker compose ps

# Check resource usage
docker stats supplychainx-app
```

---

## Troubleshooting

### Cannot connect to database

```bash
# Check database connection details
cat .env

# Test database connection
apt-get install -y mysql-client
mysql -h YOUR_DB_HOST -P 25060 -u doadmin -p
```

### Container keeps restarting

```bash
# Check logs for errors
docker compose logs app

# Common issues:
# - Wrong database credentials
# - Database host unreachable
# - Wrong database port (should be 25060 for DO Managed DB)
```

### Port already in use

```bash
# Check what's using port 8080
netstat -tlnp | grep 8080

# Kill the process or change APP_PORT in .env
```

---

## Files on Your Droplet

```
~/supplychainx/
├── docker-compose.yml  (Created in Step 3)
└── .env                (Created in Step 4 - Contains secrets!)
```

**Important**: The `.env` file contains sensitive information. Keep it secure!

---

## Next Steps

1. ✅ Configure **proxy-1** to forward traffic to `http://10.114.16.3:8080`
2. ✅ Set up domain and SSL
3. ✅ Configure automated deployments
4. ✅ Set up monitoring

Your backend is ready to receive traffic from proxy-1! 🚀
