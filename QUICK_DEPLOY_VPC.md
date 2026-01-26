# Quick Deploy to Backend-1 (VPC Setup)

## Your Setup Summary
- **Backend Droplet**: backend-1 (10.114.16.3, Public: 165.22.84.200)
- **Proxy**: proxy-1 (10.114.16.2, Public: 134.209.249.179)
- **VPC Network**: 10.114.16.0/20
- **Managed DB**: backend-1 (private connection)

---

## Deployment Commands

### 1. SSH into backend-1
```bash
ssh root@165.22.84.200
```

### 2. Create Deployment Files
```bash
mkdir -p ~/supplychainx && cd ~/supplychainx

# Create .env file
cat > .env << 'EOF'
DOCKER_HUB_USERNAME=your-dockerhub-username
IMAGE_TAG=backend
DB_HOST=private-backend-1-do-user-xxxxx.db.ondigitalocean.com
DB_PORT=25060
DB_NAME=defaultdb
DB_USER=doadmin
DB_PASSWORD=your-database-password
KEYCLOAK_URL=http://134.209.249.179:8090/realms/supplychainx
APP_PORT=8080
DB_SSL=true
EOF

# Edit with your actual values
nano .env
```

### 3. Create docker-compose.yml
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
      SPRING_DATASOURCE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=${DB_SSL:-true}&requireSSL=false&serverTimezone=UTC
      SPRING_DATASOURCE_USERNAME: ${DB_USER}
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
      SPRING_LIQUIBASE_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/${DB_NAME}?useSSL=${DB_SSL:-true}&requireSSL=false&serverTimezone=UTC
      SPRING_LIQUIBASE_USER: ${DB_USER}
      SPRING_LIQUIBASE_PASSWORD: ${DB_PASSWORD}
      SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI: ${KEYCLOAK_URL}
EOF
```

### 4. Deploy
```bash
docker compose pull
docker compose up -d
docker compose logs -f app
```

### 5. Test
```bash
# From backend-1
curl http://localhost:8080/actuator/health

# From anywhere in VPC
curl http://10.114.16.3:8080/actuator/health
```

---

## Configure proxy-1 (Nginx)

SSH to proxy-1:
```bash
ssh root@134.209.249.179
```

Create Nginx config:
```bash
nano /etc/nginx/sites-available/api
```

Add:
```nginx
server {
    listen 80;
    server_name 134.209.249.179;

    location /api {
        proxy_pass http://10.114.16.3:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }
}
```

Enable:
```bash
ln -s /etc/nginx/sites-available/api /etc/nginx/sites-enabled/
nginx -t
systemctl reload nginx
```

Test:
```bash
curl http://134.209.249.179/api/health
```

---

## Important Notes

✅ **Database Connection**:
- Use PRIVATE connection string from DigitalOcean dashboard
- Port: 25060 (not 3306)
- Enable SSL: useSSL=true

✅ **Network**:
- All internal communication uses VPC (10.114.16.0/20)
- Only proxy-1 exposes services publicly

✅ **Update Application**:
```bash
cd ~/supplychainx
docker compose pull && docker compose up -d
```

---

## Full Documentation
See [DEPLOY_VPC_ARCHITECTURE.md](../docs/DEPLOY_VPC_ARCHITECTURE.md) for complete guide.
