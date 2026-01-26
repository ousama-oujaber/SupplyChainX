# Deploy Backend to DigitalOcean VPC Architecture

This guide is tailored for your specific DigitalOcean VPC setup with separate proxy, frontend, backend, and managed database.

## Your Architecture

![VPC Architecture](file:///home/protocol/.gemini/antigravity/brain/f360ac2f-24a1-48c0-ab9b-88683c1246df/uploaded_media_1769434355192.png)

```
Internet
   ↓
proxy-1 (134.209.249.179 - PUBLIC)
   ↓ (VPC: 10.114.16.0/20)
frontend-1 (10.114.16.5 - PRIVATE)
   ↓
backend-1 (10.114.16.3 - PRIVATE)
   ↓
Managed DB (backend-1 - PRIVATE)
```

### Your Resources
- **VPC Network**: 10.114.16.0/20 (4,086 addresses)
- **proxy-1**: 10.114.16.2 (Public: 134.209.249.179)
- **frontend-1**: 10.114.16.5 (Public: 157.230.115.167)
- **backend-1**: 10.114.16.3 (Public: 165.22.84.200)
- **Managed Database**: backend-1 (Private connection string)

---

## Deployment Steps

### Step 1: Get Database Connection Details

1. Log into DigitalOcean Dashboard
2. Go to **Databases** → Click **backend-1**
3. Find the **Private Connection Details**:
   - **Host**: Will be something like `private-backend-1-do-user-xxxxx.db.ondigitalocean.com`
   - **Port**: Usually `25060` for MySQL
   - **Database**: Your database name
   - **User**: Database username
   - **Password**: Database password

> **Important**: Use the **PRIVATE** connection string, not the public one!

---

### Step 2: SSH Into backend-1 Droplet

```bash
# SSH using the public IP (you can still access it for configuration)
ssh root@165.22.84.200

# Or use the DigitalOcean console
```

---

### Step 3: Create Deployment Directory

```bash
mkdir -p ~/supplychainx
cd ~/supplychainx
```

---

### Step 4: Create Environment File

Create `.env` file with your database credentials:

```bash
nano .env
```

Add this content (replace with your actual values from Step 1):

```bash
# Docker Hub Configuration
DOCKER_HUB_USERNAME=your-dockerhub-username
IMAGE_TAG=backend

# Managed Database Configuration (USE PRIVATE CONNECTION)
DB_HOST=private-backend-1-do-user-xxxxx.db.ondigitalocean.com
DB_PORT=25060
DB_NAME=defaultdb
DB_USER=doadmin
DB_PASSWORD=your-database-password

# Keycloak Configuration
# Option 1: If Keycloak is on proxy-1 (accessible via public IP)
KEYCLOAK_URL=http://134.209.249.179:8090/realms/supplychainx

# Option 2: If Keycloak is on backend-1 (use private IP)
# KEYCLOAK_URL=http://10.114.16.3:8090/realms/supplychainx

# Option 3: If Keycloak is external
# KEYCLOAK_URL=http://your-keycloak-server:8090/realms/supplychainx

# Application Configuration
APP_PORT=8080

# SSL/TLS for Database (Recommended for managed DB)
DB_SSL=true
```

**Important Notes:**
- Use the **PRIVATE** database host (starts with `private-`)
- DigitalOcean Managed Databases use port `25060` (not 3306)
- Default database name is usually `defaultdb`
- Default user is `doadmin`

Save and exit (`Ctrl+X`, `Y`, `Enter`)

---

### Step 5: Create Docker Compose File

```bash
nano docker-compose.yml
```

Add this content:

```yaml
version: '3.8'

services:
  app:
    image: ${DOCKER_HUB_USERNAME}/supplychainx:${IMAGE_TAG:-backend}
    container_name: supplychainx-app
    restart: unless-stopped
    ports:
      - "${APP_PORT:-8080}:8080"
    environment:
      # Database Configuration (Managed Database via Private Connection)
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=${DB_SSL:-true}&requireSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      
      # Liquibase Configuration
      SPRING_LIQUIBASE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=${DB_SSL:-true}&requireSSL=false&serverTimezone=UTC
      SPRING_LIQUIBASE_USER: ${DB_USER}
      SPRING_LIQUIBASE_PASSWORD: ${DB_PASSWORD}
      
      # Keycloak Configuration
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: ${KEYCLOAK_URL}
    networks:
      - app-network

networks:
  app-network:
    driver: bridge
```

Save and exit (`Ctrl+X`, `Y`, `Enter`)

---

### Step 6: Test Database Connection (Optional)

Before deploying, verify you can connect to the database:

```bash
# Install MySQL client
apt-get update
apt-get install -y mysql-client

# Test connection using private host
source .env
mysql -h ${DB_HOST} -P ${DB_PORT} -u ${DB_USER} -p${DB_PASSWORD} ${DB_NAME}
```

If you can connect, you're good to go!

---

### Step 7: Deploy the Backend

```bash
# Pull the latest image
docker compose pull

# Start the application
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f app
```

---

### Step 8: Verify Backend is Running

From the backend-1 droplet:

```bash
# Check health endpoint
curl http://localhost:8080/actuator/health

# Or check from VPC
curl http://10.114.16.3:8080/actuator/health
```

You should see:
```json
{"status":"UP"}
```

---

### Step 9: Configure proxy-1 to Forward Traffic

Now you need to configure your proxy-1 to forward API requests to backend-1.

SSH into **proxy-1**:

```bash
ssh root@134.209.249.179
```

If you're using **Nginx** on proxy-1, create/update the configuration:

```bash
nano /etc/nginx/sites-available/api
```

Add this configuration:

```nginx
# Backend API Proxy
server {
    listen 80;
    server_name your-domain.com;  # or use IP: 134.209.249.179

    # Proxy to backend-1 via private VPC network
    location /api {
        proxy_pass http://10.114.16.3:8080;  # Backend private IP
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # CORS headers (if needed)
        add_header Access-Control-Allow-Origin *;
    }

    # Health check endpoint
    location /health {
        proxy_pass http://10.114.16.3:8080/actuator/health;
    }
}
```

Enable and test:

```bash
# Enable the site
ln -s /etc/nginx/sites-available/api /etc/nginx/sites-enabled/

# Test configuration
nginx -t

# Reload Nginx
systemctl reload nginx
```

---

### Step 10: Test the Complete Flow

From your local machine:

```bash
# Test via proxy (public IP)
curl http://134.209.249.179/api/health

# Or with domain
curl http://your-domain.com/api/health
```

---

## Network Flow Diagram

```
Client Request
     ↓
Internet (http://134.209.249.179/api/...)
     ↓
proxy-1 (134.209.249.179)
     ↓ [Nginx forwards to]
backend-1 (10.114.16.3:8080) ← Uses PRIVATE IP
     ↓ [Connects to]
Managed DB (private-backend-1-xxx.db.ondigitalocean.com:25060)
```

**All VPC communication uses private IPs - Fast and secure!**

---

## Important Configuration Notes

### 1. Database Connection

✅ **DO**:
- Use the **private** connection string from DigitalOcean
- Use port `25060` (DigitalOcean's MySQL port)
- Enable SSL: `useSSL=true`
- Database name is usually `defaultdb`

❌ **DON'T**:
- Don't use the public database connection (slower, less secure)
- Don't use port 3306 (that's for standard MySQL, not managed DB)

### 2. Firewall Configuration

Your backend-1 droplet firewall should:
- ✅ Allow incoming on port 8080 from VPC network (10.114.16.0/20)
- ✅ Allow outgoing to database port 25060
- ❌ Don't expose port 8080 to the public internet (use proxy instead)

### 3. VPC Benefits

Using the VPC network:
- ✅ **Faster**: No public internet routing
- ✅ **Secure**: Traffic stays within DigitalOcean's network
- ✅ **Free**: No bandwidth charges for VPC traffic
- ✅ **Private**: Database never exposed to internet

---

## Updating the Application

When you push new code and the CI/CD builds a new image:

```bash
# SSH into backend-1
ssh root@165.22.84.200

# Navigate to app directory
cd ~/supplychainx

# Pull latest image and restart
docker compose pull
docker compose up -d

# Check logs
docker compose logs -f app
```

---

## Monitoring and Logs

### View Application Logs
```bash
# On backend-1 droplet
cd ~/supplychainx
docker compose logs -f app

# Last 100 lines
docker compose logs --tail=100 app
```

### Check Database Connection
```bash
# View connection status in logs
docker compose logs app | grep -i "database\|connection\|mysql"
```

### Monitor Resource Usage
```bash
# Check container stats
docker stats supplychainx-app

# Check disk space
df -h

# Check memory
free -h
```

---

## Troubleshooting

### Cannot Connect to Database

**Error**: `Communications link failure`

**Solutions**:
1. Verify you're using the **PRIVATE** connection string
2. Check port is `25060` not `3306`
3. Verify database is in the same region as backend-1
4. Check database user password is correct
5. Ensure database allows connections from VPC

```bash
# Test from backend-1 droplet
telnet private-backend-1-xxx.db.ondigitalocean.com 25060
```

### Backend Not Accessible from Proxy

**Error**: `Connection refused` when proxy tries to reach backend

**Solutions**:
1. Verify backend is running: `docker compose ps`
2. Check app is listening on 8080: `netstat -tlnp | grep 8080`
3. Verify private IP is correct: `ip addr show`
4. Test from proxy-1: `curl http://10.114.16.3:8080/actuator/health`

### Application Keeps Restarting

```bash
# Check why it's failing
docker compose logs app

# Common causes:
# - Database connection failure
# - Wrong environment variables
# - Liquibase migration errors
```

---

## Security Best Practices

1. ✅ **Use VPC for all internal communication**
2. ✅ **Only proxy-1 should be publicly accessible**
3. ✅ **Remove public IPs from backend-1 and frontend-1** (optional, for max security)
4. ✅ **Use DigitalOcean Firewall rules** to restrict access
5. ✅ **Enable SSL on database connections**
6. ✅ **Use strong passwords** for database
7. ✅ **Regular backups** of managed database
8. ✅ **Monitor logs** for suspicious activity

---

## DigitalOcean Firewall Rules

Create firewall for **backend-1**:

**Inbound Rules**:
- SSH (22) from your IP or via proxy-1
- HTTP (8080) from VPC network (10.114.16.0/20)

**Outbound Rules**:
- All protocols to VPC network (10.114.16.0/20)
- HTTPS (443) to internet (for Docker Hub)
- MySQL (25060) to managed database

---

## Next Steps

1. ✅ Set up SSL/HTTPS on proxy-1 with Let's Encrypt
2. ✅ Configure automated backups for managed database
3. ✅ Set up monitoring (DigitalOcean Monitoring)
4. ✅ Configure log aggregation
5. ✅ Set up alerts for downtime
6. ✅ Remove public IPs from backend-1 and frontend-1 (optional)

---

## Your Deployment is Complete! 🚀

Your backend is now running on **backend-1** (10.114.16.3), connected to your managed database via private network, and accessible through **proxy-1** (134.209.249.179).

**Access URLs**:
- Public API: `http://134.209.249.179/api/...`
- Health Check: `http://134.209.249.179/health`
- Swagger UI: `http://134.209.249.179/api/swagger-ui.html`
