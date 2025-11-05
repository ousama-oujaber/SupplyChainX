# SupplyChainX – Complete Monolithic System
## Integrated Supply Chain Management

### 🎯 Context
SupplyChainX is a company specializing in complete supply chain management, covering the entire process from the procurement of raw materials to the delivery of finished products to customers.

The objective is to provide a centralized, reliable, and intuitive solution capable of efficiently managing:
- Procurement and supplier relations
- Production and resource planning
- Delivery and tracking of finished products

### 💻 Developer's Mission
As a Java developer, you will be responsible for designing and developing a **monolithic** application with **Spring Boot**, following object-oriented programming best practices and a **multi-layer architecture (MVC: Repository / Service / Controller)**.

The application must:
- Automate business processes
- Centralize data management
- Ensure complete traceability of flows within the supply chain

## 📋 Key Objectives / Features

### Module 1: Procurement

#### Supplier Management
- Add a supplier
- Modify an existing supplier
- Delete a supplier only if they have no active orders
- View the complete list of suppliers with pagination
- Search for a supplier by name or supplier code

#### Raw Material Management
- Add a raw material
- Modify a raw material
- Delete a raw material if it is not in use
- View the complete list of raw materials with pagination
- View materials with stock levels below the critical threshold

#### Supply Order Management
- Create a supply order
- Modify an existing order
- Delete an order if it has not yet been delivered
- View the complete list of orders with pagination
- Track order status: *Pending, In Progress, Received*

---

### Module 2: Production

#### Finished Product Management
- Add a finished product
- Modify the information of an existing product
- Delete a product if there are no associated production orders
- View the complete list of products with pagination
- Search for a product by name or reference

#### Production Order Management
- Create a production order
- Modify an existing order
- Cancel an order if it has not started
- View the complete list of orders with pagination
- Track order status: *Pending, In Production, Completed, Blocked*

#### Scheduling / Planning Management
- Check the availability of raw materials before launching an order
- Calculate the estimated production time for each order
---

### Module 3: Delivery & Distribution

#### Customer Management
- Add a customer
- Modify an existing customer
- Delete a customer only if they have no active orders
- View the complete list of customers with pagination
- Search for a customer by name or reference

#### Customer Order Management
- Create a customer order
- Modify an existing order
- Cancel an order if it has not been shipped
- View the complete list of orders with pagination
- Track order status: *In Preparation, In Transit, Delivered*

#### Delivery Management
- Create a delivery for an order
- Associate an existing order with a delivery
    - Assign a vehicle and a driver (optional)
    - Define the expected date and possibly the cost
    - Initialize the status to *Planned*
    - Calculate the total cost of each delivery
## ⚙️ Global Business Rules

### Procurement
- A raw material can have multiple suppliers.
- A supply order is associated with a single supplier.
- Deleting a supplier is only possible if they have no active orders.
- A low stock level for a raw material triggers an alert (using a scheduler + sending an email message) as a bonus feature (SMTP).

### Production
- Each production order consumes materials according to the **BOM (Bill of Materials)**.
- A product can only be manufactured if all required materials are available.
- Deleting a product is only possible if it has no associated orders.
- Priority orders are processed before standard orders.

### Delivery
- A customer can have multiple orders.
- Each customer order is associated with a single delivery.
- A delivery is only possible if the product is in stock.

### User Management
- A user has a unique role that determines their permissions:

**Procurement Module:**
- PROCUREMENT_MANAGER
- PURCHASING_MANAGER
- LOGISTICS_SUPERVISOR

**Production Module:**
- PRODUCTION_MANAGER
- PLANNER
- PRODUCTION_SUPERVISOR

**Delivery Module:**
- SALES_MANAGER
- LOGISTICS_MANAGER
- DELIVERY_SUPERVISOR

**Administration:**
- ADMIN (full access)

## 🗂️ Entity Modeling

### Common Entities

#### User

| Field     | Type   | Description                |
|-----------|--------|----------------------------|
| idUser    | Long   | Unique identifier          |
| firstName | String | First name                 |
| lastName  | String | Last name                  |
| email     | String | Unique email               |
| password  | String | Password (encrypted)       |
| role      | Enum   | User role                  |

---

### Procurement Module

#### Supplier

| Field      | Type      | Description                    |
|------------|-----------|--------------------------------|
| idSupplier | Long      | Unique identifier              |
| name       | String    | Supplier name                  |
| contact    | String    | Contact information            |
| rating     | Double    | Reliability score              |
| leadTime   | Integer   | Average lead time in days      |
| orders     | OneToMany | List of associated orders      |

#### RawMaterial

| Field      | Type       | Description                    |
|------------|------------|--------------------------------|
| idMaterial | Long       | Unique identifier              |
| name       | String     | Material name                  |
| stock      | Integer    | Available quantity             |
| stockMin   | Integer    | Critical minimum threshold     |
| unit       | String     | Unit of measurement            |
| suppliers  | ManyToMany | Associated suppliers           |

#### SupplyOrder

| Field     | Type      | Description                         |
|-----------|-----------|-------------------------------------|
| idOrder   | Long      | Unique identifier                   |
| supplier  | ManyToOne | Associated supplier                 |
| materials | ManyToMany| Ordered materials                   |
| orderDate | LocalDate | Creation date                       |
| status    | Enum      | PENDING, IN_PROGRESS, RECEIVED      |

---

### Production Module

#### Product (Finished Product)

| Field          | Type    | Description                   |
|----------------|---------|-------------------------------|
| idProduct      | Long    | Unique identifier             |
| name           | String  | Product name                  |
| productionTime | Integer | Manufacturing time (hours)    |
| cost           | Double  | Unit cost                     |
| stock          | Integer | Quantity in stock             |

#### BillOfMaterial (BOM)

| Field    | Type      | Description                |
|----------|-----------|----------------------------|
| idBOM    | Long      | Unique identifier          |
| product  | ManyToOne | Associated finished product|
| material | ManyToOne | Raw material used          |
| quantity | Integer   | Required quantity          |

#### ProductionOrder

| Field     | Type      | Description                               |
|-----------|-----------|-------------------------------------------|
| idOrder   | Long      | Unique identifier                         |
| product   | ManyToOne | Manufactured product                      |
| quantity  | Integer   | Ordered quantity                          |
| status    | Enum      | PENDING, IN_PRODUCTION, COMPLETED, BLOCKED|
| startDate | LocalDate | Start date                                |
| endDate   | LocalDate | Estimated end date                        |

---

### Delivery Module

#### Customer

| Field    | Type      | Description               |
|----------|-----------|---------------------------|
| idCustomer| Long    | Unique identifier         |
| name     | String    | Customer name             |
| address  | String    | Full address              |
| city     | String    | City                      |
| orders   | OneToMany | Associated orders         |

#### Order (Customer Order)

| Field    | Type      | Description                             |
|----------|-----------|-----------------------------------------|
| idOrder  | Long      | Unique identifier                       |
| customer | ManyToOne | Associated customer                     |
| product  | ManyToOne | Ordered product                         |
| quantity | Integer   | Ordered quantity                        |
| status   | Enum      | IN_PREPARATION, IN_TRANSIT, DELIVERED   |

#### Delivery

| Field        | Type      | Description                 |
|--------------|-----------|-----------------------------|
| idDelivery   | Long      | Unique identifier           |
| order        | OneToOne  | Associated order            |
| vehicle      | String    | Assigned vehicle            |
| driver       | String    | Assigned driver             |
| status       | Enum      | PLANNED, IN_PROGRESS, DELIVERED |
| deliveryDate | LocalDate | Planned delivery date       |
| cost         | Double    | Cost of delivery            |

## 🛠️ Technical Architecture

### Backend - Monolithic Application

- **Spring Boot 3.x**: Development of a complete REST API
- **Spring Data JPA**: Persistence management
- **Hibernate**: ORM
- **Liquibase**: Database migrations
- **MySQL / PostgreSQL**: Single relational database
- **MVC Architecture**: Repository / Service / Controller
- **DTO / MapStruct**: Separation between API and entities
- **Swagger / OpenAPI**: Endpoint documentation
- **Validation**: `@Valid`, Bean Validation
- **Exception Handling**: `@ExceptionHandler`, `@ControllerAdvice`
- **Spring AOP**: Implementation of simulated security by checking the email and password transmitted in the HTTP request headers.

- **Unit Tests**: JUnit 5, Mockito
- **Integration Tests**: Spring Boot Test, TestContainers (bonus)

## 📁 Project Structure
```bash
supplychainx/
├─ src/
│  ├─ main/
│  │  ├─ java/com/supplychainx/
│  │  │  ├─ config/                # Spring config, Swagger, MapStruct, Mail, Security AOP
│  │  │  ├─ common/                # DTOs, exceptions, utils, enums
│  │  │  ├─ user/                  # user module (controller/service/repo)
│  │  │  ├─ procurement/           # suppliers, rawmaterials, supplyorders
│  │  │  ├─ production/            # products, bom, productionorders
│  │  │  ├─ delivery/              # customers, orders, deliveries
│  │  │  └─ scheduler/             # scheduled tasks (low stock)
│  │  └─ resources/
│  │     ├─ db/changelog/          # Liquibase changelogs
│  │     ├─ application.yml
│  │     └─ messages.properties
├─ docker/
│  └─ docker-compose.yml
├─ build.gradle (or pom.xml)
└─ README.md
```

## 📖 Complete User Stories

### Users (All Modules)
- **US1**: As an administrator, I want to create a user account with a specific role.
- **US2**: As an administrator, I want to modify the role of an existing user.

---

### Procurement Module

#### Suppliers
- **US3**: As a procurement manager, I want to add a supplier with all their information.
- **US4**: As a procurement manager, I want to modify an existing supplier.
- **US5**: As a procurement manager, I want to delete a supplier (if they have no active orders).
- **US6**: As a logistics supervisor, I want to view the list of all suppliers.
- **US7**: As a purchasing manager, I want to search for a supplier by name.

#### Raw Materials
- **US8**: As a procurement manager, I want to add a new raw material.
- **US9**: As a procurement manager, I want to modify an existing raw material.
- **US10**: As a procurement manager, I want to delete a raw material (if it is not used in any order).
- **US11**: As a logistics supervisor, I want to view the list of all raw materials.
- **US12**: As a logistics supervisor, I want to filter materials with stock levels below the critical threshold.

#### Supply Orders
- **US13**: As a purchasing manager, I want to create a supply order.
- **US14**: As a purchasing manager, I want to modify an existing order.
- **US15**: As a purchasing manager, I want to delete an order if it has not been delivered.
- **US16**: As a logistics supervisor, I want to view the list of all orders.
- **US17**: As a logistics supervisor, I want to track the status of orders.

---

### Production Module

#### Finished Products
- **US18**: As a production manager, I want to add a finished product with all its information.
- **US19**: As a production manager, I want to modify an existing product.
- **US20**: As a production manager, I want to delete a product (if no associated order exists).
- **US21**: As a production supervisor, I want to view the list of all products.
- **US22**: As a production supervisor, I want to search for a product by name.

#### Production Orders
- **US23**: As a production manager, I want to create a production order.
- **US24**: As a production manager, I want to modify an existing order.
- **US25**: As a production manager, I want to cancel an order if it has not started.
- **US26**: As a production supervisor, I want to view the list of all orders.
- **US27**: As a production supervisor, I want to track the status of orders.

#### Planning / Scheduling
- **US28**: As a planner, I want to check the availability of materials before launching an order.
- **US29**: As a planner, I want to calculate the estimated production time.

---

### Delivery Module

#### Customers
- **US30**: As a sales manager, I want to add a customer with all their information.
- **US31**: As a sales manager, I want to modify an existing customer.
- **US32**: As a sales manager, I want to delete a customer (if they have no active orders).
- **US33**: As a sales manager, I want to view the list of all customers.
- **US34**: As a sales manager, I want to search for a customer by name.

#### Customer Orders
- **US35**: As a sales manager, I want to create a customer order.
- **US36**: As a sales manager, I want to modify an existing order.
- **US37**: As a sales manager, I want to cancel an order if it has not been shipped.
- **US38**: As a delivery supervisor, I want to view the list of all orders.
- **US39**: As a delivery supervisor, I want to track the status of orders.

#### Deliveries
- **US40**: Create a delivery for an order and calculate its total cost.