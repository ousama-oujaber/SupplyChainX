# Deploy Backend to DigitalOcean with External Database

This guide helps you deploy **only the SupplyChainX backend** to a DigitalOcean droplet when you already have:
- ✅ A droplet with Docker installed
- ✅ A separate database server (MySQL)
- ✅ (Optional) A separate Keycloak server

---

## Quick Deployment Steps

### Step 1: Gather Your Database Information

You'll need:
- **Database Host**: IP address or hostname of your database server
- **Database Port**: Usually `3306` for MySQL
- **Database Name**: The database name (e.g., `supplychainx`)
- **Database Username**: Your database user
- **Database Password**: Your database password
- **Keycloak URL**: If using external Keycloak (e.g., `http://keycloak-server:8090/realms/supplychainx`)

---

### Step 2: SSH Into Your Droplet

```bash
ssh root@your-droplet-ip
```

---

### Step 3: Create Deployment Directory

```bash
# Create directory for your app
mkdir -p ~/supplychainx-backend
cd ~/supplychainx-backend
```

---

### Step 4: Create Environment File

Create a `.env` file with your database credentials:

```bash
nano .env
```

Add the following (replace with your actual values):

```bash
# Your Docker Hub username
DOCKER_HUB_USERNAME=your-dockerhub-username

# Image tag to use
IMAGE_TAG=backend

# Database Configuration (External DB)
DB_HOST=your-database-ip-or-hostname
DB_PORT=3306
DB_NAME=supplychainx
DB_USER=your-db-username
DB_PASSWORD=your-db-password

# Keycloak Configuration (if using external Keycloak)
KEYCLOAK_URL=http://your-keycloak-server:8090/realms/supplychainx

# Application Port
APP_PORT=8080
```

Save and exit (`Ctrl+X`, `Y`, `Enter`)

---

### Step 5: Create Simple Docker Compose File

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
      # Database Configuration
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      
      # Liquibase Configuration (same as datasource)
      SPRING_LIQUIBASE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
      SPRING_LIQUIBASE_USER: ${DB_USER}
      SPRING_LIQUIBASE_PASSWORD: ${DB_PASSWORD}
      
      # Keycloak Configuration
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: ${KEYCLOAK_URL}
```

Save and exit (`Ctrl+X`, `Y`, `Enter`)

---

### Step 6: Configure Firewall (Optional but Recommended)

```bash
# Allow SSH
ufw allow 22/tcp

# Allow your application port
ufw allow 8080/tcp

# Enable firewall
ufw --force enable
```

---

### Step 7: Deploy the Application

```bash
# Pull the latest image from Docker Hub
docker compose pull

# Start the application
docker compose up -d

# Check if it's running
docker compose ps
```

---

### Step 8: Verify Deployment

```bash
# Check application logs
docker compose logs -f app

# Test the application
curl http://localhost:8080/actuator/health
```

You should see a response like:
```json
{"status":"UP"}
```

---

## Alternative: Using Docker Run (Without Docker Compose)

If you prefer to use `docker run` directly:

```bash
# Pull the image
docker pull your-dockerhub-username/supplychainx:backend

# Run the container
docker run -d \
  --name supplychainx-app \
  --restart unless-stopped \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL="jdbc:mysql://YOUR_DB_HOST:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e SPRING_DATASOURCE_USERNAME="your-db-user" \
  -e SPRING_DATASOURCE_PASSWORD="your-db-password" \
  -e SPRING_LIQUIBASE_URL="jdbc:mysql://YOUR_DB_HOST:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC" \
  -e SPRING_LIQUIBASE_USER="your-db-user" \
  -e SPRING_LIQUIBASE_PASSWORD="your-db-password" \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI="http://your-keycloak:8090/realms/supplychainx" \
  your-dockerhub-username/supplychainx:backend

# View logs
docker logs -f supplychainx-app
```

---

## Database Connection Checklist

Before deploying, ensure:

- ✅ **Database exists**: The database `supplychainx` is created
- ✅ **User has permissions**: Your database user has full permissions on the database
- ✅ **Network access**: Your droplet can reach the database server (test with `telnet db-host 3306`)
- ✅ **Firewall rules**: Database server allows connections from your droplet's IP
- ✅ **Tables will be created**: Liquibase will auto-create tables on first run

### Test Database Connection

From your droplet, test the connection:

```bash
# Install MySQL client if not available
apt-get install -y mysql-client

# Test connection
mysql -h YOUR_DB_HOST -P 3306 -u YOUR_DB_USER -p YOUR_DB_NAME
```

If this works, your app will be able to connect!

---

## Updating the Application

When you push new code and it builds to Docker Hub:

```bash
cd ~/supplychainx-backend

# Pull latest image
docker compose pull

# Restart with new image
docker compose up -d

# Or if using docker run:
docker stop supplychainx-app
docker rm supplychainx-app
docker pull your-dockerhub-username/supplychainx:backend
# Then run the docker run command again
```

---

## Useful Commands

### View Logs
```bash
# Real-time logs
docker compose logs -f app

# Last 100 lines
docker compose logs --tail=100 app
```

### Restart Application
```bash
docker compose restart app
```

### Stop Application
```bash
docker compose down
```

### Check Application Status
```bash
docker compose ps
```

### Access Container Shell
```bash
docker exec -it supplychainx-app sh
```

---

## Troubleshooting

### Cannot Connect to Database

**Error in logs:**
```
Unable to acquire JDBC Connection
```

**Solutions:**
1. Verify database credentials in `.env` file
2. Check database server is running: `systemctl status mysql`
3. Test connection from droplet: `mysql -h DB_HOST -u DB_USER -p`
4. Check firewall on database server allows your droplet's IP
5. Verify database user has proper permissions

### Keycloak Connection Issues

**Error in logs:**
```
Unable to obtain OIDC metadata
```

**Solutions:**
1. Verify Keycloak URL is accessible from your droplet
2. Test: `curl http://keycloak-server:8090/realms/supplychainx/.well-known/openid-configuration`
3. Ensure Keycloak server is running
4. Check firewall allows access to Keycloak port

### Application Keeps Restarting

```bash
# Check why it's failing
docker logs supplychainx-app

# Common causes:
# - Database connection issues
# - Wrong environment variables
# - Port already in use
```

### Port Already in Use

```bash
# Check what's using port 8080
netstat -tlnp | grep 8080

# Kill the process or use a different port
# Change APP_PORT in .env file
```

---

## Security Best Practices

1. ✅ **Use SSL for database connection** (in production)
2. ✅ **Use environment variables** (never hardcode passwords)
3. ✅ **Restrict database access** (firewall rules)
4. ✅ **Use strong passwords**
5. ✅ **Keep Docker updated**: `apt-get update && apt-get upgrade`
6. ✅ **Use HTTPS** for your API (Nginx + Let's Encrypt)

---

## Access Your Application

Once deployed:
- **API Base URL**: `http://your-droplet-ip:8080`
- **Swagger UI**: `http://your-droplet-ip:8080/swagger-ui.html`
- **Health Check**: `http://your-droplet-ip:8080/actuator/health`

---

## Next Steps

1. ✅ Set up a domain name pointing to your droplet
2. ✅ Configure Nginx as reverse proxy
3. ✅ Add SSL/HTTPS with Let's Encrypt
4. ✅ Set up monitoring and alerts
5. ✅ Configure automated backups

You're all set! Your backend is now running on DigitalOcean connected to your external database! 🚀
