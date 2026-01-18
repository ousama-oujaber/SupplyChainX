# SupplyChainX API Documentation

> **Version:** 1.0.0  
> **Base URL:** `http://localhost:8080`  
> **Format:** JSON  
> **Interactive Docs:** [Swagger UI](/swagger-ui.html) · [OpenAPI JSON](/api-docs)

---

## Table of Contents

1. [API Overview](#1-api-overview)
2. [Authentication & Authorization](#2-authentication--authorization)
3. [Common Patterns](#3-common-patterns)
4. [Error Handling](#4-error-handling)
5. [User Management API](#5-user-management-api)
6. [Procurement Module](#6-procurement-module)
7. [Production Module](#7-production-module)
8. [Delivery Module](#8-delivery-module)
9. [Frontend Integration Guide](#9-frontend-integration-guide)
10. [Environment Configuration](#10-environment-configuration)

---

## 1. API Overview

### Base URL

| Environment | URL |
|-------------|-----|
| Development | `http://localhost:8080` |
| Production  | `https://api.supplychainx.com` |

### API Versioning

Currently, the API does not use URL-based versioning. All endpoints are served from the root path. Future versions may introduce `/api/v2/` prefixes.

### Data Format

- **Request Content-Type:** `application/json`
- **Response Content-Type:** `application/json`
- **Date Format:** `YYYY-MM-DD` (ISO 8601)
- **DateTime Format:** `YYYY-MM-DDTHH:mm:ss`

### Required Headers

```http
Content-Type: application/json
Accept: application/json
```

---

## 2. Authentication & Authorization

The API uses **Keycloak OAuth2** with JWT tokens for authentication.

### Keycloak Server

| Setting | Value |
|---------|-------|
| URL | `http://localhost:8090` |
| Realm | `supplychainx` |
| Client ID | `supplychainx-api` |
| Admin Console | `http://localhost:8090` (admin/admin) |

### Getting a JWT Token

```bash
curl -X POST "http://localhost:8090/realms/supplychainx/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=supplychainx-api" \
  -d "username=admin" \
  -d "password=admin123" \
  -d "grant_type=password"
```

**Response:**
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI...",
  "expires_in": 3600,
  "token_type": "Bearer"
}
```

### Using the Token

Include the token in the `Authorization` header:

```http
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI...
```

### Test Accounts

#### Admin Account (Full Access)

| Field | Value |
|-------|-------|
| Username | `admin` |
| Password | `admin123` |
| Role | `ADMIN` |

#### Procurement Module Accounts

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `procurement.manager` | `test123` | GESTIONNAIRE_APPROVISIONNEMENT | Procurement R/W |
| `purchasing.manager` | `test123` | RESPONSABLE_ACHATS | Procurement Full |

#### Production Module Accounts

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `production.manager` | `test123` | CHEF_PRODUCTION | Production Full |
| `planner` | `test123` | PLANIFICATEUR | Production R/W |
| `production.supervisor` | `test123` | SUPERVISEUR_PRODUCTION | Production R/W |

#### Delivery Module Accounts

| Username | Password | Role | Access |
|----------|----------|------|--------|
| `sales.manager` | `test123` | GESTIONNAIRE_COMMERCIAL | Delivery R/W |
| `logistics.manager` | `test123` | RESPONSABLE_LOGISTIQUE | Delivery Full |
| `delivery.supervisor` | `test123` | SUPERVISEUR_LIVRAISONS | Delivery R/W |
| `logistics.supervisor` | `test123` | SUPERVISEUR_LOGISTIQUE | Read Only |

### Role-Based Access Matrix

| Endpoint | ADMIN | Procurement Roles | Production Roles | Delivery Roles |
|----------|-------|-------------------|------------------|----------------|
| `/api/users/**` | ✅ Full | ❌ | ❌ | ❌ |
| `/api/procurement/**` | ✅ Full | ✅ R/W | 📖 Read | 📖 Read |
| `/api/production/**` | ✅ Full | 📖 Read | ✅ R/W | 📖 Read |
| `/api/delivery/**` | ✅ Full | ❌ | 📖 Read | ✅ R/W |

### HTTP Status Codes for Auth

| Code | Meaning |
|------|---------|
| `401` | Unauthorized - Missing or invalid token |
| `403` | Forbidden - Token valid but insufficient permissions |

---

## 3. Common Patterns

### Pagination

All list endpoints support pagination via query parameters:

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `page` | integer | `0` | Page number (0-based) |
| `size` | integer | `10` | Items per page |
| `sortBy` | string | varies | Field to sort by |
| `direction` | string | `ASC` | Sort direction (`ASC` or `DESC`) |

**Example Request:**
```http
GET /api/users?page=0&size=10&sortBy=id&direction=ASC
```

**Paginated Response Structure:**
```json
{
  "content": [...],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": { "sorted": true, "direction": "ASC" }
  },
  "totalElements": 100,
  "totalPages": 10,
  "first": true,
  "last": false,
  "empty": false
}
```

### Search Endpoints

Most modules provide search functionality:

```http
GET /api/{module}/search?name=<query>&page=0&size=10
```

---

## 4. Error Handling

### Standard Error Response

```json
{
  "timestamp": "2026-01-14T22:00:00.000",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for field 'name'",
  "path": "/api/users"
}
```

### HTTP Status Codes

| Code | Meaning | When Used |
|------|---------|-----------|
| `200` | OK | Successful GET/PUT/PATCH |
| `201` | Created | Successful POST |
| `204` | No Content | Successful DELETE |
| `400` | Bad Request | Validation errors |
| `404` | Not Found | Resource doesn't exist |
| `409` | Conflict | Business rule violation |
| `500` | Server Error | Unexpected errors |

### Validation Error Example

```json
{
  "timestamp": "2026-01-14T22:00:00.000",
  "status": 400,
  "error": "Validation Failed",
  "message": "name: Material name cannot be blank; stock: Stock must be at least 0"
}
```

---

## 5. User Management API

**Base Path:** `/api/users`

### Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/api/users` | Create a new user |
| `PUT` | `/api/users/{id}` | Update an existing user |
| `GET` | `/api/users/{id}` | Get user by ID |
| `GET` | `/api/users/email/{email}` | Get user by email |
| `GET` | `/api/users` | Get all users (paginated) |
| `GET` | `/api/users/search` | Search users by name |
| `DELETE` | `/api/users/{id}` | Delete a user |

### User Object Schema

```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "role": "ADMIN"
}
```

### Validation Rules

| Field | Rules |
|-------|-------|
| `firstName` | Required, 2-50 characters |
| `lastName` | Required, 2-50 characters |
| `email` | Required, valid email format, unique |
| `password` | Required, 8-100 characters |
| `role` | Required, must be valid enum value |

### Create User

```http
POST /api/users
Content-Type: application/json

{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "role": "GESTIONNAIRE_APPROVISIONNEMENT"
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "firstName": "John",
  "lastName": "Doe",
  "email": "john.doe@example.com",
  "password": "securePassword123",
  "role": "GESTIONNAIRE_APPROVISIONNEMENT"
}
```

---

## 6. Procurement Module

### 6.1 Suppliers

**Base Path:** `/api/procurement/suppliers`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/suppliers` | Create supplier |
| `PUT` | `/suppliers/{id}` | Update supplier |
| `GET` | `/suppliers/{id}` | Get supplier by ID |
| `GET` | `/suppliers` | List all suppliers |
| `GET` | `/suppliers/search` | Search by name |
| `DELETE` | `/suppliers/{id}` | Delete supplier |

#### Supplier Schema

```json
{
  "idSupplier": 1,
  "name": "ABC Materials Corp",
  "contact": "contact@abc-materials.com",
  "rating": 4.5,
  "leadTime": 7,
  "activeOrdersCount": 3,
  "materials": [
    {
      "supplierId": 1,
      "materialId": 1,
      "materialName": "Steel Sheets",
      "unitPrice": 25.50,
      "minOrderQuantity": 100,
      "leadTimeDays": 5,
      "isPreferred": true
    }
  ]
}
```

#### Validation Rules

| Field | Rules |
|-------|-------|
| `name` | Required, 2-100 characters |
| `contact` | Required, max 200 characters |
| `rating` | 0-5 |
| `leadTime` | Required, minimum 1 day |

#### Create Supplier Example

```http
POST /api/procurement/suppliers
Content-Type: application/json

{
  "name": "ABC Materials Corp",
  "contact": "contact@abc-materials.com",
  "rating": 4.5,
  "leadTime": 7
}
```

---

### 6.2 Raw Materials

**Base Path:** `/api/procurement/raw-materials`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/raw-materials` | Create material |
| `PUT` | `/raw-materials/{id}` | Update material |
| `GET` | `/raw-materials/{id}` | Get material by ID |
| `GET` | `/raw-materials` | List all materials |
| `GET` | `/raw-materials/search` | Search by name |
| `GET` | `/raw-materials/below-minimum` | Materials below min stock (paginated) |
| `GET` | `/raw-materials/below-minimum/all` | All materials below min stock |
| `DELETE` | `/raw-materials/{id}` | Delete material |
| `GET` | `/raw-materials/{id}/suppliers` | Get suppliers for material |
| `POST` | `/raw-materials/{id}/suppliers` | Add supplier relationship |
| `PUT` | `/raw-materials/{materialId}/suppliers/{supplierId}` | Update supplier relationship |
| `DELETE` | `/raw-materials/{materialId}/suppliers/{supplierId}` | Remove supplier relationship |

#### Raw Material Schema

```json
{
  "idMaterial": 1,
  "name": "Steel Sheets",
  "stock": 500,
  "stockMin": 100,
  "unit": "kg",
  "isBelowMinimum": false,
  "suppliers": [
    {
      "supplierId": 1,
      "supplierName": "ABC Materials Corp",
      "materialId": 1,
      "unitPrice": 25.50,
      "minOrderQuantity": 100,
      "leadTimeDays": 5,
      "isPreferred": true
    }
  ]
}
```

#### Supplier-Material Relationship Schema

```json
{
  "supplierId": 1,
  "materialId": 1,
  "unitPrice": 25.50,
  "minOrderQuantity": 100,
  "leadTimeDays": 5,
  "isPreferred": true
}
```

#### Create Material Example

```http
POST /api/procurement/raw-materials
Content-Type: application/json

{
  "name": "Steel Sheets",
  "stock": 500,
  "stockMin": 100,
  "unit": "kg"
}
```

#### Add Supplier to Material

```http
POST /api/procurement/raw-materials/1/suppliers
Content-Type: application/json

{
  "supplierId": 1,
  "unitPrice": 25.50,
  "minOrderQuantity": 100,
  "leadTimeDays": 5,
  "isPreferred": true
}
```

---

### 6.3 Supply Orders

**Base Path:** `/api/procurement/supply-orders`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/supply-orders` | Create order |
| `PUT` | `/supply-orders/{id}` | Update order |
| `GET` | `/supply-orders/{id}` | Get order by ID |
| `GET` | `/supply-orders` | List all orders |
| `GET` | `/supply-orders/by-status` | Filter by status |
| `GET` | `/supply-orders/by-supplier/{id}` | Orders by supplier |
| `PATCH` | `/supply-orders/{id}/status` | Update order status |
| `DELETE` | `/supply-orders/{id}` | Delete order |

#### Order Status Values

| Status | Description |
|--------|-------------|
| `EN_ATTENTE` | Pending |
| `EN_COURS` | In progress |
| `RECUE` | Received (**auto-updates stock**) |

#### Supply Order Schema

```json
{
  "idOrder": 1,
  "supplierId": 1,
  "supplierName": "ABC Materials Corp",
  "items": [
    {
      "idItem": 1,
      "materialId": 1,
      "materialName": "Steel Sheets",
      "quantity": 100,
      "unitPrice": 25.50,
      "subTotal": 2550.00
    }
  ],
  "orderDate": "2026-01-14",
  "status": "EN_ATTENTE",
  "expectedDeliveryDate": "2026-01-28"
}
```

#### Create Supply Order

```http
POST /api/procurement/supply-orders
Content-Type: application/json

{
  "supplierId": 1,
  "items": [
    {
      "materialId": 1,
      "quantity": 100,
      "unitPrice": 25.50
    }
  ],
  "orderDate": "2026-01-14",
  "status": "EN_ATTENTE",
  "expectedDeliveryDate": "2026-01-28"
}
```

#### Update Order Status (Mark as Received)

```http
PATCH /api/procurement/supply-orders/1/status?status=RECUE
```

> **Important:** When status changes to `RECUE`, the system automatically adds the ordered quantities to the raw material stock.

---

## 7. Production Module

### 7.1 Products

**Base Path:** `/api/production/products`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/products` | Create product |
| `PUT` | `/products/{id}` | Update product |
| `GET` | `/products/{id}` | Get product by ID |
| `GET` | `/products` | List all products |
| `GET` | `/products/search` | Search by name |
| `DELETE` | `/products/{id}` | Delete product |

#### Product Schema

```json
{
  "idProduct": 1,
  "name": "Laptop Stand",
  "productionTime": 120,
  "cost": 45.00,
  "stock": 50
}
```

#### Create Product Example

```http
POST /api/production/products
Content-Type: application/json

{
  "name": "Laptop Stand",
  "productionTime": 120,
  "cost": 45.00,
  "stock": 0
}
```

---

### 7.2 Bill of Materials (BOM)

**Base Path:** `/api/production/bom`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/bom` | Create BOM entry |
| `PUT` | `/bom/{id}` | Update BOM entry |
| `GET` | `/bom/{id}` | Get BOM by ID |
| `GET` | `/bom` | List all BOMs |
| `GET` | `/bom/product/{productId}` | Get BOMs for product |
| `GET` | `/bom/product/{productId}/paginated` | BOMs for product (paginated) |
| `GET` | `/bom/product/{productId}/check-availability` | Check materials availability |
| `DELETE` | `/bom/{id}` | Delete BOM entry |

#### BOM Schema

```json
{
  "idBOM": 1,
  "productId": 1,
  "productName": "Laptop Stand",
  "materialId": 1,
  "materialName": "Steel Sheets",
  "quantity": 2
}
```

#### Check Materials Availability

```http
GET /api/production/bom/product/1/check-availability?quantity=10
```

**Response:** `true` or `false`

---

### 7.3 Production Orders

**Base Path:** `/api/production/orders`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/orders` | Create production order |
| `PUT` | `/orders/{id}` | Update order |
| `GET` | `/orders/{id}` | Get order by ID |
| `GET` | `/orders` | List all orders |
| `GET` | `/orders/by-status` | Filter by status |
| `GET` | `/orders/by-product/{id}` | Orders by product |
| `GET` | `/orders/priority` | Priority orders only |
| `PATCH` | `/orders/{id}/status` | Update status |
| `DELETE` | `/orders/{id}/cancel` | Cancel order |
| `GET` | `/orders/estimate` | Calculate production time |

#### Production Order Status

| Status | Description |
|--------|-------------|
| `EN_ATTENTE` | Pending |
| `EN_PRODUCTION` | In production |
| `TERMINE` | Completed |
| `BLOQUE` | Blocked |

#### Production Order Schema

```json
{
  "idOrder": 1,
  "productId": 1,
  "productName": "Laptop Stand",
  "quantity": 100,
  "status": "EN_ATTENTE",
  "startDate": "2026-01-20",
  "endDate": "2026-01-25",
  "isPriority": false
}
```

---

## 8. Delivery Module

### 8.1 Customers

**Base Path:** `/api/delivery/customers`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/customers` | Create customer |
| `PUT` | `/customers/{id}` | Update customer |
| `GET` | `/customers/{id}` | Get customer by ID |
| `GET` | `/customers` | List all customers |
| `GET` | `/customers/search` | Search by name |
| `DELETE` | `/customers/{id}` | Delete customer |

#### Customer Schema

```json
{
  "idCustomer": 1,
  "name": "Tech Solutions Inc",
  "address": "123 Main Street",
  "city": "New York"
}
```

---

### 8.2 Customer Orders

**Base Path:** `/api/delivery/orders`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/orders` | Create customer order |
| `PUT` | `/orders/{id}` | Update order |
| `GET` | `/orders/{id}` | Get order by ID |
| `GET` | `/orders` | List all orders |
| `GET` | `/orders/customer/{id}` | Orders by customer |
| `GET` | `/orders/status/{status}` | Filter by status |
| `DELETE` | `/orders/{id}` | Cancel order |

#### Customer Order Status

| Status | Description |
|--------|-------------|
| `EN_PREPARATION` | Being prepared |
| `EN_ROUTE` | In transit |
| `LIVREE` | Delivered |

#### Customer Order Schema

```json
{
  "idOrder": 1,
  "customerId": 1,
  "customerName": "Tech Solutions Inc",
  "productId": 1,
  "productName": "Laptop Stand",
  "quantity": 50,
  "status": "EN_PREPARATION"
}
```

---

### 8.3 Deliveries

**Base Path:** `/api/delivery/deliveries`

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/deliveries` | Create delivery |
| `PUT` | `/deliveries/{id}` | Update delivery |
| `GET` | `/deliveries/{id}` | Get delivery by ID |
| `GET` | `/deliveries/order/{orderId}` | Get delivery by order |
| `GET` | `/deliveries` | List all deliveries |
| `GET` | `/deliveries/status/{status}` | Filter by status |
| `POST` | `/deliveries/{id}/calculate-cost` | Calculate delivery cost |
| `DELETE` | `/deliveries/{id}` | Delete delivery |

#### Delivery Status

| Status | Description |
|--------|-------------|
| `PLANIFIEE` | Scheduled |
| `EN_COURS` | In progress |
| `LIVREE` | Delivered |

#### Delivery Schema

```json
{
  "idDelivery": 1,
  "orderId": 1,
  "vehicle": "Van-001",
  "driver": "John Smith",
  "status": "PLANIFIEE",
  "deliveryDate": "2026-01-20",
  "cost": 150.00
}
```

---

## 9. Frontend Integration Guide

### Angular HttpClient Example

```typescript
// Service
@Injectable({ providedIn: 'root' })
export class SupplierService {
  private apiUrl = 'http://localhost:8080/api/procurement/suppliers';

  constructor(private http: HttpClient) {}

  getSuppliers(page = 0, size = 10): Observable<Page<Supplier>> {
    return this.http.get<Page<Supplier>>(
      `${this.apiUrl}?page=${page}&size=${size}`
    );
  }

  createSupplier(supplier: Supplier): Observable<Supplier> {
    return this.http.post<Supplier>(this.apiUrl, supplier);
  }
}
```

### React Fetch Example

```typescript
// API Hook
const useSuppliers = (page: number = 0) => {
  const [data, setData] = useState<Page<Supplier> | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch(`http://localhost:8080/api/procurement/suppliers?page=${page}`)
      .then(res => res.json())
      .then(setData)
      .finally(() => setLoading(false));
  }, [page]);

  return { data, loading };
};
```

### Token Handling Best Practices

1. Store JWT tokens in `httpOnly` cookies (preferred) or `localStorage`
2. Include token in `Authorization` header for all API calls
3. Implement token refresh logic before expiration
4. Clear tokens on logout

### Common Frontend Mistakes to Avoid

| Mistake | Solution |
|---------|----------|
| Not handling 404 errors | Display "Not Found" message |
| Ignoring pagination | Always pass `page` and `size` params |
| Sending wrong date format | Use `YYYY-MM-DD` format |
| Missing `Content-Type` header | Always include `application/json` |
| Not handling loading states | Show spinners during API calls |

---

## 10. Environment Configuration

### Development

```env
API_BASE_URL=http://localhost:8080
```

### Production

```env
API_BASE_URL=https://api.supplychainx.com
```

### CORS Configuration

The backend is configured to allow requests **only from Angular**:
- `http://localhost:4200`
- `https://localhost:4200`

> **Note:** Other origins (including React on port 3000) are blocked.

### API Health Check

```http
GET /actuator/health
```

```json
{
  "status": "UP"
}
```

---

## Quick Reference

### All Endpoints by Module

| Module | Base Path | Key Actions |
|--------|-----------|-------------|
| Users | `/api/users` | CRUD + Search |
| Suppliers | `/api/procurement/suppliers` | CRUD + Search |
| Raw Materials | `/api/procurement/raw-materials` | CRUD + Stock Alerts + Supplier Links |
| Supply Orders | `/api/procurement/supply-orders` | CRUD + Status Update (auto-stock) |
| Products | `/api/production/products` | CRUD + Search |
| Bill of Materials | `/api/production/bom` | CRUD + Availability Check |
| Production Orders | `/api/production/orders` | CRUD + Status + Priority |
| Customers | `/api/delivery/customers` | CRUD + Search |
| Customer Orders | `/api/delivery/orders` | CRUD + Status Filter |
| Deliveries | `/api/delivery/deliveries` | CRUD + Cost Calculation |

---

*Documentation generated for SupplyChainX v1.0.0*
