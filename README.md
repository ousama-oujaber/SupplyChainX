# SupplyChainX

> A comprehensive Supply Chain Management System built with Spring Boot, featuring Keycloak OAuth2 authentication and role-based access control.

## 🚀 Quick Start

### Prerequisites
- Java 17+
- Docker & Docker Compose
- Maven (or use included wrapper)

### Start Services

```bash
# Start all containers (MySQL, Keycloak, PhpMyAdmin)
docker compose up -d

# Wait for Keycloak to be ready (~30 seconds)
docker logs -f supplychainx-keycloak

# Run the application
./mvnw spring-boot:run
```

### Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| API | http://localhost:8080 | JWT Token |
| Swagger UI | http://localhost:8080/swagger-ui.html | - |
| Keycloak Admin | http://localhost:8090 | admin / admin |
| PhpMyAdmin | http://localhost:8081 | scx_user / scx_pass |

---

## 🔐 Test Accounts

All test accounts use the same client: `supplychainx-api`

### Admin Account

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |
| Role | `ADMIN` |
| Access | **Full access to all modules** |

### Procurement Module

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `procurement.manager` | `test123` | GESTIONNAIRE_APPROVISIONNEMENT | Procurement R/W |
| `purchasing.manager` | `test123` | RESPONSABLE_ACHATS | Procurement Full |

### Production Module

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `production.manager` | `test123` | CHEF_PRODUCTION | Production Full |
| `planner` | `test123` | PLANIFICATEUR | Production R/W |
| `production.supervisor` | `test123` | SUPERVISEUR_PRODUCTION | Production R/W |

### Delivery Module

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `sales.manager` | `test123` | GESTIONNAIRE_COMMERCIAL | Delivery R/W |
| `logistics.manager` | `test123` | RESPONSABLE_LOGISTIQUE | Delivery Full |
| `delivery.supervisor` | `test123` | SUPERVISEUR_LIVRAISONS | Delivery R/W |
| `logistics.supervisor` | `test123` | SUPERVISEUR_LOGISTIQUE | Read Only |

---

## 🔑 Getting a JWT Token

```bash
# Get token from Keycloak
curl -X POST "http://localhost:8090/realms/supplychainx/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=supplychainx-api" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password"

# Use the token
curl -H "Authorization: Bearer <access_token>" \
  http://localhost:8080/api/users
```

---

## 📦 Modules

| Module | Base Path | Description |
|--------|-----------|-------------|
| Users | `/api/users` | User management (Admin only) |
| Procurement | `/api/procurement/**` | Suppliers, Raw Materials, Supply Orders |
| Production | `/api/production/**` | Products, BOM, Production Orders |
| Delivery | `/api/delivery/**` | Customers, Orders, Deliveries |

---

## 🔒 CORS Configuration

The API only accepts requests from:
- `http://localhost:4200` (Angular)
- `https://localhost:4200` (Angular HTTPS)

---

## 📖 Documentation

- [API Documentation](docs/API_DOCUMENTATION.md)
- [Swagger UI](http://localhost:8080/swagger-ui.html)

---

## 🛠️ Development

```bash
# Compile
./mvnw compile

# Run tests
./mvnw test

# Package
./mvnw package -DskipTests
```