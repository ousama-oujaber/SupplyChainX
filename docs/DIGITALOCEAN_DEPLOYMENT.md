# DigitalOcean Deployment Guide

This guide walks you through deploying the SupplyChainX backend application to a DigitalOcean droplet using Docker.

## Prerequisites

- A DigitalOcean droplet (Ubuntu 22.04 recommended, minimum 2GB RAM)
- SSH access to your droplet
- A domain name (optional, but recommended for production)
- Your Docker Hub image is published (e.g., `<username>/supplychainx:backend`)

---

## Quick Start (TL;DR)

```bash
# 1. SSH into your droplet
ssh root@your-droplet-ip

# 2. Install Docker and Docker Compose
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
apt-get install -y docker-compose-plugin

# 3. Create deployment directory
mkdir -p ~/supplychainx
cd ~/supplychainx

# 4. Create docker-compose.yml (see below)
# 5. Create .env file with your credentials
# 6. Run the application
docker compose up -d
```

---

## Step-by-Step Deployment

### Step 1: Set Up Your Droplet

#### 1.1 Create a Droplet

1. Log into [DigitalOcean](https://cloud.digitalocean.com/)
2. Click **Create** → **Droplets**
3. Choose:
   - **Image**: Ubuntu 22.04 LTS
   - **Plan**: Basic (2GB RAM, 1 vCPU minimum)
   - **Datacenter**: Choose closest to your users
   - **Authentication**: SSH keys (recommended) or password
4. Click **Create Droplet**

#### 1.2 Connect to Your Droplet

```bash
ssh root@your-droplet-ip
```

### Step 2: Install Docker and Docker Compose

```bash
# Update package list
apt-get update

# Install Docker using the official script
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh

# Install Docker Compose Plugin
apt-get install -y docker-compose-plugin

# Verify installation
docker --version
docker compose version

# (Optional) Enable Docker to start on boot
systemctl enable docker
```

### Step 3: Create Deployment Structure

```bash
# Create application directory
mkdir -p ~/supplychainx
cd ~/supplychainx

# Create directories for persistent data
mkdir -p mysql-data
mkdir -p keycloak-data
```

### Step 4: Create Production Docker Compose File

Create a `docker-compose.yml` file:

```bash
nano docker-compose.yml
```

Paste the following content:

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: supplychainx-mysql
    restart: unless-stopped
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - ./mysql-data:/var/lib/mysql
    ports:
      - "3306:3306"
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost", "-u", "${MYSQL_USER}", "-p${MYSQL_PASSWORD}"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak-db:
    image: postgres:15
    container_name: supplychainx-keycloak-db
    restart: unless-stopped
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: ${KEYCLOAK_DB_PASSWORD}
    volumes:
      - ./keycloak-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U keycloak"]
      interval: 10s
      timeout: 5s
      retries: 5

  keycloak:
    image: quay.io/keycloak/keycloak:latest
    container_name: supplychainx-keycloak
    restart: unless-stopped
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://keycloak-db:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: ${KEYCLOAK_DB_PASSWORD}
      KC_HOSTNAME: ${KEYCLOAK_HOSTNAME}
      KC_HOSTNAME_PORT: ${KEYCLOAK_PORT:-8090}
      KC_HTTP_ENABLED: "true"
      KC_HOSTNAME_STRICT_HTTPS: "false"
      KEYCLOAK_ADMIN: ${KEYCLOAK_ADMIN_USER}
      KEYCLOAK_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD}
    command: start-dev
    ports:
      - "${KEYCLOAK_PORT:-8090}:8080"
    depends_on:
      keycloak-db:
        condition: service_healthy

  app:
    image: ${DOCKER_HUB_USERNAME}/supplychainx:${IMAGE_TAG:-backend}
    container_name: supplychainx-app
    restart: unless-stopped
    environment:
      SPRING_DATASOURCE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${MYSQL_USER}
      SPRING_DATASOURCE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_LIQUIBASE_URL: jdbc:mysql://mysql:3306/${MYSQL_DATABASE}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_LIQUIBASE_USER: ${MYSQL_USER}
      SPRING_LIQUIBASE_PASSWORD: ${MYSQL_PASSWORD}
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: http://${KEYCLOAK_HOSTNAME}:${KEYCLOAK_PORT:-8090}/realms/supplychainx
    ports:
      - "${APP_PORT:-8080}:8080"
    depends_on:
      mysql:
        condition: service_healthy
      keycloak:
        condition: service_started

volumes:
  mysql-data:
  keycloak-data:
```

Save and exit (`Ctrl+X`, then `Y`, then `Enter`)

### Step 5: Create Environment Variables File

Create a `.env` file with your configuration:

```bash
nano .env
```

Paste the following (replace with your actual values):

```bash
# Docker Hub Configuration
DOCKER_HUB_USERNAME=your-dockerhub-username
IMAGE_TAG=backend

# MySQL Configuration
MYSQL_ROOT_PASSWORD=your-strong-root-password
MYSQL_DATABASE=supplychainx
MYSQL_USER=scx_user
MYSQL_PASSWORD=your-strong-mysql-password

# Keycloak Database
KEYCLOAK_DB_PASSWORD=your-strong-keycloak-db-password

# Keycloak Configuration
KEYCLOAK_HOSTNAME=your-droplet-ip-or-domain
KEYCLOAK_PORT=8090
KEYCLOAK_ADMIN_USER=admin
KEYCLOAK_ADMIN_PASSWORD=your-strong-keycloak-admin-password

# Application Configuration
APP_PORT=8080
```

**Important:** Replace all placeholder passwords with strong, unique passwords!

Save and exit (`Ctrl+X`, then `Y`, then `Enter`)

### Step 6: Configure Firewall

```bash
# Allow SSH (important - don't lock yourself out!)
ufw allow 22/tcp

# Allow HTTP and HTTPS (for web traffic)
ufw allow 80/tcp
ufw allow 443/tcp

# Allow your application port
ufw allow 8080/tcp

# Allow Keycloak port
ufw allow 8090/tcp

# Enable firewall
ufw --force enable

# Check firewall status
ufw status
```

### Step 7: Deploy the Application

```bash
# Pull the latest images
docker compose pull

# Start all services
docker compose up -d

# Check if containers are running
docker compose ps

# View logs
docker compose logs -f app
```

### Step 8: Import Keycloak Realm Configuration

You need to configure Keycloak with your realm. You have two options:

#### Option A: Manual Configuration (Quick)
1. Access Keycloak: `http://your-droplet-ip:8090`
2. Login with your admin credentials
3. Create a new realm called `supplychainx`
4. Configure clients, roles, and users as needed

#### Option B: Import from Your Local Setup (Recommended)
```bash
# On your local machine, copy the realm config to the droplet
scp keycloak/supplychainx-realm.json root@your-droplet-ip:~/supplychainx/

# On the droplet, copy it into the Keycloak container
docker cp ~/supplychainx/supplychainx-realm.json supplychainx-keycloak:/tmp/

# Import the realm
docker exec supplychainx-keycloak /opt/keycloak/bin/kc.sh import --file /tmp/supplychainx-realm.json
```

### Step 9: Verify Deployment

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Check all services
docker compose ps

# View application logs
docker compose logs app

# View all logs
docker compose logs
```

### Step 10: Access Your Application

- **Application API**: `http://your-droplet-ip:8080`
- **Keycloak Admin**: `http://your-droplet-ip:8090`
- **API Documentation**: `http://your-droplet-ip:8080/swagger-ui.html`

---

## Production Enhancements

### 1. Set Up Nginx Reverse Proxy

Install Nginx:

```bash
apt-get install -y nginx
```

Create Nginx configuration:

```bash
nano /etc/nginx/sites-available/supplychainx
```

Add this configuration:

```nginx
server {
    listen 80;
    server_name your-domain.com;

    # Application
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Keycloak
    location /auth {
        proxy_pass http://localhost:8090;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

Enable the site:

```bash
ln -s /etc/nginx/sites-available/supplychainx /etc/nginx/sites-enabled/
nginx -t
systemctl restart nginx
```

### 2. Set Up SSL/HTTPS with Let's Encrypt

```bash
# Install Certbot
apt-get install -y certbot python3-certbot-nginx

# Get SSL certificate
certbot --nginx -d your-domain.com

# Auto-renewal is configured automatically
```

### 3. Set Up Automatic Updates

Create a script to pull and update images:

```bash
nano ~/supplychainx/update.sh
```

```bash
#!/bin/bash
cd ~/supplychainx
docker compose pull
docker compose up -d
docker image prune -f
```

Make it executable:

```bash
chmod +x ~/supplychainx/update.sh
```

Run manually when you want to update:

```bash
cd ~/supplychainx
./update.sh
```

---

## Useful Commands

### Managing Services

```bash
# Start services
docker compose up -d

# Stop services
docker compose down

# Restart a specific service
docker compose restart app

# View logs
docker compose logs -f app

# Update to latest images
docker compose pull && docker compose up -d

# Remove everything (including data)
docker compose down -v
```

### Database Backup

```bash
# Backup MySQL database
docker exec supplychainx-mysql mysqldump -u scx_user -p supplychainx > backup_$(date +%Y%m%d).sql

# Restore MySQL database
docker exec -i supplychainx-mysql mysql -u scx_user -p supplychainx < backup_20260126.sql
```

### Monitoring

```bash
# Check resource usage
docker stats

# Check disk usage
docker system df

# Clean up unused images
docker image prune -a
```

---

## Troubleshooting

### Application Won't Start

```bash
# Check logs
docker compose logs app

# Check if all services are healthy
docker compose ps

# Verify environment variables
docker compose config
```

### Cannot Connect to Database

```bash
# Check MySQL is running
docker compose ps mysql

# Test MySQL connection
docker exec -it supplychainx-mysql mysql -u scx_user -p

# Check network connectivity
docker exec supplychainx-app ping mysql
```

### Out of Disk Space

```bash
# Check disk usage
df -h

# Clean up Docker
docker system prune -a --volumes

# Check Docker disk usage
docker system df
```

### Performance Issues

```bash
# Check resource usage
docker stats

# Increase droplet size if needed (via DigitalOcean dashboard)

# Check application logs for errors
docker compose logs app | grep -i error
```

---

## Security Best Practices

1. ✅ **Use strong passwords** for all services
2. ✅ **Keep Docker and packages updated**:
   ```bash
   apt-get update && apt-get upgrade -y
   ```
3. ✅ **Use SSL/HTTPS** in production
4. ✅ **Restrict firewall rules** to only necessary ports
5. ✅ **Regular backups** of your database
6. ✅ **Use SSH keys** instead of passwords
7. ✅ **Don't expose MySQL/Postgres** to the internet (use internal Docker network)
8. ✅ **Monitor logs** regularly for suspicious activity

---

## Next Steps

1. ✅ Set up your domain name to point to your droplet
2. ✅ Configure SSL/HTTPS with Let's Encrypt
3. ✅ Set up monitoring (DigitalOcean Monitoring, or tools like Grafana)
4. ✅ Configure automated backups (DigitalOcean Backups or custom scripts)
5. ✅ Set up CI/CD to automatically deploy on push (GitHub Actions)

---

## Need Help?

- [DigitalOcean Documentation](https://docs.digitalocean.com/)
- [Docker Documentation](https://docs.docker.com/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

Your application is now running on DigitalOcean! 🚀
