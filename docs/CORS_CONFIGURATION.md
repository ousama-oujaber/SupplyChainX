# CORS Configuration - Environment Variables

## ✅ CORS is Now Dynamic!

CORS origins are **no longer hardcoded** in the source code. They're configured via environment variables, so your production IPs stay secure and out of GitHub.

---

## Configuration

### Development (Default)
By default, only localhost is allowed:
```properties
cors.allowed-origins=http://localhost:4200,https://localhost:4200
```

### Production (Via Environment Variable)
Set `CORS_ALLOWED_ORIGINS` when deploying:

```bash
CORS_ALLOWED_ORIGINS=http://localhost:4200,https://localhost:4200
```

---

## How It Works

### 1. application.properties
```properties
cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:4200,https://localhost:4200}
```

- Reads from environment variable `CORS_ALLOWED_ORIGINS`
- Falls back to localhost if not set

### 2. SecurityConfig.java
```java
@Value("${cors.allowed-origins}")
private String allowedOrigins;

configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
```

- Reads the property
- Splits by comma to create a list

### 3. Docker Compose
```yaml
environment:
  CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-http://localhost:4200}
```

- Passes from `.env` file to container

---

## Setting Up for Production

### 1. Create/Edit `.env` File

On your backend-1 droplet:

```bash
nano ~/supplychainx/.env
```

Add:
```bash
# CORS Configuration
CORS_ALLOWED_ORIGINS=http://localhost,https://localhost,http://localhost,https://localhost
```

### 2. When You Add a Domain

Simply update the env variable:
```bash
CORS_ALLOWED_ORIGINS=http://localhost,https://localhost,http://localhost,https://localhost,https://yourdomain.com,https://www.yourdomain.com
```

### 3. Restart Application

```bash
docker compose restart app
```

No code changes No rebuild needed! ✅

---

## For Your VPC Setup

### Origins to Include

Based on your architecture, include:

1. **Proxy Server** (public entry point):
   - `http://localhost`
   - `https://localhost`

2. **Frontend Server** (where Angular runs):
   - `http://localhost`
   - `https://localhost`

3. **Your Domain** (when configured):
   - `https://yourdomain.com`
   - `https://www.yourdomain.com`

4. **Development** (keep for testing):
   - `http://localhost:4200`
   - `https://localhost:4200`

---

## Example .env File

```bash
# Docker Hub
DOCKER_HUB_USERNAME=ousama-oujaber
IMAGE_TAG=backend

# Database (DigitalOcean Managed)
DB_HOST=private-backend-1-do-user-12345.db.ondigitalocean.com
DB_PORT=25060
DB_NAME=defaultdb
DB_USER=doadmin
DB_PASSWORD=your-strong-password
DB_SSL=true

# Keycloak
KEYCLOAK_URL=http://localhost:8090/realms/supplychainx

# Application
APP_PORT=8080

# CORS - Add all your origins here
CORS_ALLOWED_ORIGINS=http://localhost:4200,https://localhost:4200,http://localhost,https://localhost,http://localhost,https://localhost
```

---

## Testing CORS

### From Browser (Frontend)
Your Angular app should now work without CORS errors when making requests to:
- `http://localhost/api/...`

### Manual Test
```bash
curl -H "Origin: http://localhost" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: Content-Type" \
     -X OPTIONS \
     http://165.22.84.200:8080/actuator/health \
     -v
```

Look for:
```
< Access-Control-Allow-Origin: http://localhost
< Access-Control-Allow-Credentials: true
```

---

## Security Benefits

✅ **Production IPs not in GitHub** - Kept in environment variables
✅ **Easy to update** - Just edit `.env` and restart
✅ **Environment-specific** - Different CORS for dev/staging/prod
✅ **No code changes** - Update CORS without rebuilding
✅ **Still secure** - Specific origins only (no wildcard `*`)

---

## Files Modified

1. [`SecurityConfig.java`](file:///home/protocol/IdeaProjects/docker/dep/SupplyChainX/src/main/java/com/protocol/supplychainx/config/SecurityConfig.java) - Reads from application.properties
2. [`application.properties`](file:///home/protocol/IdeaProjects/docker/dep/SupplyChainX/src/main/resources/application.properties) - Defines cors.allowed-origins
3. [`docker-compose.external-db.yml`](file:///home/protocol/IdeaProjects/docker/dep/SupplyChainX/docker-compose.external-db.yml) - Passes environment variable
4. [`.env.external-db.example`](file:///home/protocol/IdeaProjects/docker/dep/SupplyChainX/.env.external-db.example) - Example configuration

---

**Your production IPs are now secure and configurable! 🎉**
