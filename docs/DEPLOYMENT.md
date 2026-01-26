# SupplyChainX Deployment Guide

This guide provides comprehensive instructions for deploying the SupplyChainX backend application using Docker and CI/CD.

## Table of Contents
- [Prerequisites](#prerequisites)
- [CI/CD Setup](#cicd-setup)
- [Local Docker Deployment](#local-docker-deployment)
- [Production Deployment](#production-deployment)
- [Troubleshooting](#troubleshooting)

## Prerequisites

### Required Accounts
- [Docker Hub](https://hub.docker.com) account
- GitHub repository with admin access

### Local Development Tools
- Docker and Docker Compose installed
- Git

## CI/CD Setup

### 1. Docker Hub Access Token

1. Log into [Docker Hub](https://hub.docker.com)
2. Navigate to **Account Settings** → **Security** → **New Access Token**
3. Create a new access token:
   - **Description:** `github-actions-supplychainx`
   - **Access permissions:** Read, Write, Delete
4. **Save the token securely** (you'll only see it once)

### 2. Configure GitHub Secrets

1. Go to your GitHub repository
2. Navigate to **Settings** → **Secrets and variables** → **Actions**
3. Click **New repository secret** and add the following:

| Secret Name | Value | Description |
|------------|-------|-------------|
| `DOCKER_HUB_USERNAME` | Your Docker Hub username | Used to authenticate with Docker Hub |
| `DOCKER_HUB_ACCESS_TOKEN` | The access token from step 1 | Access token for pushing images |

### 3. Trigger the Workflow

The GitHub Actions workflow will automatically trigger on:

- **Push to main branch**: Builds and pushes image with `latest` tag
- **Pull requests**: Builds the image (doesn't push)
- **Git tags** (e.g., `v1.0.0`): Builds and pushes versioned images
- **Manual trigger**: Via GitHub Actions UI

#### Manual Workflow Trigger
1. Go to **Actions** tab in your GitHub repository
2. Select **Docker Build and Push** workflow
3. Click **Run workflow** → **Run workflow**

#### Creating a Release
```bash
# Tag a new version
git tag -a v1.0.0 -m "Release version 1.0.0"
git push origin v1.0.0
```

This will create images tagged as:
- `<username>/supplychainx:1.0.0`
- `<username>/supplychainx:1.0`
- `<username>/supplychainx:1`
- `<username>/supplychainx:latest`

## Local Docker Deployment

### Using Docker Compose (Development)

The `compose.yaml` file includes all required services (MySQL, PHPMyAdmin, Keycloak, and the app).

```bash
# Start all services
docker compose up --build

# Start in detached mode
docker compose up -d --build

# View logs
docker compose logs -f app

# Stop all services
docker compose down

# Stop and remove volumes
docker compose down -v
```

### Using Pre-built Image from Docker Hub

1. **Pull the latest image:**
   ```bash
   docker pull <your-dockerhub-username>/supplychainx:latest
   ```

2. **Start required infrastructure** (MySQL, Keycloak):
   ```bash
   docker compose up mysql keycloak keycloak-db -d
   ```

3. **Run the application:**
   ```bash
   docker run -d \
     --name supplychainx-app \
     --network host \
     -e SPRING_DATASOURCE_URL=jdbc:mysql://127.0.0.1:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
     -e SPRING_DATASOURCE_USERNAME=scx_user \
     -e SPRING_DATASOURCE_PASSWORD=scx_pass \
     -e SPRING_LIQUIBASE_URL=jdbc:mysql://127.0.0.1:3306/supplychainx?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC \
     -e SPRING_LIQUIBASE_USER=scx_user \
     -e SPRING_LIQUIBASE_PASSWORD=scx_pass \
     -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=http://localhost:8090/realms/supplychainx \
     <your-dockerhub-username>/supplychainx:latest
   ```

## Production Deployment

### Environment Variables

The following environment variables must be configured for production:

#### Database Configuration
```bash
SPRING_DATASOURCE_URL=jdbc:mysql://<db-host>:<db-port>/<db-name>
SPRING_DATASOURCE_USERNAME=<db-username>
SPRING_DATASOURCE_PASSWORD=<db-password>

SPRING_LIQUIBASE_URL=jdbc:mysql://<db-host>:<db-port>/<db-name>
SPRING_LIQUIBASE_USER=<db-username>
SPRING_LIQUIBASE_PASSWORD=<db-password>
```

#### Keycloak Configuration
```bash
SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=https://<keycloak-host>/realms/supplychainx
```

#### Email Configuration (if using)
```bash
SPRING_MAIL_HOST=<smtp-host>
SPRING_MAIL_PORT=<smtp-port>
SPRING_MAIL_USERNAME=<email-username>
SPRING_MAIL_PASSWORD=<email-password>
```

### Docker Run Command (Production)

```bash
docker run -d \
  --name supplychainx-app \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=<your-db-url> \
  -e SPRING_DATASOURCE_USERNAME=<your-db-user> \
  -e SPRING_DATASOURCE_PASSWORD=<your-db-pass> \
  -e SPRING_LIQUIBASE_URL=<your-db-url> \
  -e SPRING_LIQUIBASE_USER=<your-db-user> \
  -e SPRING_LIQUIBASE_PASSWORD=<your-db-pass> \
  -e SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=<your-keycloak-url> \
  --restart unless-stopped \
  <your-dockerhub-username>/supplychainx:latest
```

### Using Docker Compose (Production)

Create a `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  app:
    image: <your-dockerhub-username>/supplychainx:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_DATASOURCE_URL=${DB_URL}
      - SPRING_DATASOURCE_USERNAME=${DB_USER}
      - SPRING_DATASOURCE_PASSWORD=${DB_PASS}
      - SPRING_LIQUIBASE_URL=${DB_URL}
      - SPRING_LIQUIBASE_USER=${DB_USER}
      - SPRING_LIQUIBASE_PASSWORD=${DB_PASS}
      - SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI=${KEYCLOAK_URL}
    restart: unless-stopped
    depends_on:
      - mysql
      - keycloak
  
  # Add your production MySQL and Keycloak services here
```

Then deploy:
```bash
docker compose -f docker-compose.prod.yml up -d
```

## Troubleshooting

### GitHub Actions Build Fails

**Issue:** Workflow fails with authentication error
```
Error: denied: requested access to the resource is denied
```

**Solution:**
- Verify `DOCKER_HUB_USERNAME` and `DOCKER_HUB_ACCESS_TOKEN` secrets are correctly set
- Ensure the access token has Read, Write, Delete permissions
- Check that the token hasn't expired

---

**Issue:** Build times out or fails during Maven build
```
Error: The operation was canceled
```

**Solution:**
- This is often due to GitHub Actions timeout (default 6 hours for free tier)
- Check your `pom.xml` for any problematic dependencies
- Consider using the `-DskipTests` flag (already included in Dockerfile)

### Docker Image Issues

**Issue:** Container exits immediately after starting
```
docker: Error response from daemon: OCI runtime create failed
```

**Solution:**
- Check container logs: `docker logs <container-id>`
- Verify environment variables are set correctly
- Ensure database is accessible from the container

---

**Issue:** Cannot connect to database
```
java.sql.SQLException: Access denied for user
```

**Solution:**
- Verify database credentials are correct
- Check database host is reachable from container
- For `localhost`, use `host.docker.internal` on Docker Desktop or `--network host` mode

### Multi-platform Build Issues

**Issue:** ARM64 build fails
```
Error: failed to solve: process "/bin/sh -c ./mvnw dependency:go-offline" did not complete successfully
```

**Solution:**
- The workflow builds for both `linux/amd64` and `linux/arm64`
- If you only need x86_64, remove `platforms` from the workflow or change to `linux/amd64` only

## Monitoring and Logs

### View Application Logs
```bash
# Docker Compose
docker compose logs -f app

# Standalone container
docker logs -f supplychainx-app
```

### Health Check
```bash
# Check if application is running
curl http://localhost:8080/actuator/health

# Or if using Spring Boot Actuator endpoints
curl http://localhost:8080/actuator/info
```

## Additional Resources

- [Docker Documentation](https://docs.docker.com/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
- [Keycloak Documentation](https://www.keycloak.org/documentation)

## Support

For issues related to:
- **Application bugs**: Open an issue in the GitHub repository
- **Docker/deployment**: Check the troubleshooting section above
- **Keycloak configuration**: See the main [README.md](../README.md)
