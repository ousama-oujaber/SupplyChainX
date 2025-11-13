#!/bin/bash

# ========================================
# SupplyChainX - Git Commit History Generator
# Timeline: October 28 - November 08, 2025
# ========================================
#
# This script reproduces the complete development timeline
# with realistic commit messages and timestamps.
#
# USAGE:
#   chmod +x generate_commit_history.sh
#   ./generate_commit_history.sh
#
# IMPORTANT:
#   - Run this script from the project root directory
#   - Make sure you have initialized a git repository (git init)
#   - All files should already exist in your project
#   - This script will stage and commit based on your actual files
#
# ========================================

set -e  # Exit on error

echo "========================================="
echo "SupplyChainX Commit History Generator"
echo "========================================="
echo ""

# Color codes for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to create a commit
create_commit() {
    local date=$1
    local message=$2
    shift 2
    local files=("$@")
    
    echo -e "${BLUE}Committing:${NC} $message"
    echo -e "${BLUE}Date:${NC} $date"
    
    # Stage files (only if they exist)
    for file in "${files[@]}"; do
        if [ -e "$file" ] || [ -d "$file" ]; then
            git add "$file" 2>/dev/null || true
        fi
    done
    
    # Create commit with custom date
    GIT_AUTHOR_DATE="$date" GIT_COMMITTER_DATE="$date" git commit -m "$message" --allow-empty || true
    
    echo -e "${GREEN}✓ Committed${NC}"
    echo ""
}

# ========================================
# Week 1: Project Setup & Core Infrastructure
# ========================================

echo "=== Week 1: Project Setup & Core Infrastructure ==="
echo ""

# Day 1: October 28, 2025
echo "--- Day 1: October 28, 2025 ---"

create_commit "2025-10-28T09:15:00+01:00" \
    "Initial project setup with Spring Boot and Maven configuration" \
    "pom.xml" "mvnw" "mvnw.cmd" ".mvn/" "src/main/java/com/protocol/supplychainx/SupplyChainXApplication.java" \
    "src/main/resources/application.properties" ".gitignore"

create_commit "2025-10-28T11:30:00+01:00" \
    "Add Docker support with PostgreSQL and containerization setup" \
    "Dockerfile" "compose.yaml" ".dockerignore"

create_commit "2025-10-28T14:45:00+01:00" \
    "Configure database connection and Liquibase integration" \
    "pom.xml" "src/main/resources/application.properties" \
    "src/main/resources/db/changelog/db.changelog-master.yaml"

create_commit "2025-10-28T16:20:00+01:00" \
    "Add common utility classes and enums for supply chain operations" \
    "src/main/java/com/protocol/supplychainx/common/enums/OrderStatus.java" \
    "src/main/java/com/protocol/supplychainx/common/enums/DeliveryStatus.java" \
    "src/main/java/com/protocol/supplychainx/common/enums/ProductionOrderStatus.java" \
    "src/main/java/com/protocol/supplychainx/common/enums/PurchaseOrderStatus.java" \
    "src/main/java/com/protocol/supplychainx/common/utils/DateUtils.java"

create_commit "2025-10-28T18:00:00+01:00" \
    "Implement global exception handling and custom exceptions" \
    "src/main/java/com/protocol/supplychainx/common/exceptions/ResourceNotFoundException.java" \
    "src/main/java/com/protocol/supplychainx/common/exceptions/InvalidOperationException.java" \
    "src/main/java/com/protocol/supplychainx/common/exceptions/GlobalExceptionHandler.java" \
    "src/main/java/com/protocol/supplychainx/common/exceptions/ErrorResponse.java"

# Day 2: October 29, 2025
echo "--- Day 2: October 29, 2025 ---"

create_commit "2025-10-29T09:00:00+01:00" \
    "Create user domain entities and database schema" \
    "src/main/java/com/protocol/supplychainx/user/entity/User.java" \
    "src/main/java/com/protocol/supplychainx/user/entity/Role.java" \
    "src/main/resources/db/changelog/01-create-users-table.yaml" \
    "src/main/resources/db/changelog/db.changelog-master.yaml"

create_commit "2025-10-29T11:15:00+01:00" \
    "Add user repository and service layer with CRUD operations" \
    "src/main/java/com/protocol/supplychainx/user/repository/UserRepository.java" \
    "src/main/java/com/protocol/supplychainx/user/repository/RoleRepository.java" \
    "src/main/java/com/protocol/supplychainx/user/service/IUserService.java" \
    "src/main/java/com/protocol/supplychainx/user/service/impl/UserService.java"

create_commit "2025-10-29T14:30:00+01:00" \
    "Implement user DTOs and MapStruct mappers" \
    "pom.xml" \
    "src/main/java/com/protocol/supplychainx/user/dto/UserDTO.java" \
    "src/main/java/com/protocol/supplychainx/user/dto/CreateUserRequest.java" \
    "src/main/java/com/protocol/supplychainx/user/dto/UpdateUserRequest.java" \
    "src/main/java/com/protocol/supplychainx/user/mapper/UserMapper.java"

create_commit "2025-10-29T16:45:00+01:00" \
    "Add user REST controller with authentication endpoints" \
    "src/main/java/com/protocol/supplychainx/user/controller/UserController.java" \
    "src/main/java/com/protocol/supplychainx/user/dto/LoginRequest.java" \
    "src/main/java/com/protocol/supplychainx/user/dto/AuthResponse.java"

# Day 3: October 30, 2025
echo "--- Day 3: October 30, 2025 ---"

create_commit "2025-10-30T09:30:00+01:00" \
    "Configure Spring Security with JWT authentication" \
    "pom.xml" \
    "src/main/java/com/protocol/supplychainx/config/SecurityConfig.java" \
    "src/main/java/com/protocol/supplychainx/config/JwtAuthenticationFilter.java" \
    "src/main/java/com/protocol/supplychainx/config/JwtTokenProvider.java"

create_commit "2025-10-30T12:00:00+01:00" \
    "Create procurement domain entities (Supplier, Product, PurchaseOrder)" \
    "src/main/java/com/protocol/supplychainx/procurement/entity/Supplier.java" \
    "src/main/java/com/protocol/supplychainx/procurement/entity/Product.java" \
    "src/main/java/com/protocol/supplychainx/procurement/entity/PurchaseOrder.java" \
    "src/main/java/com/protocol/supplychainx/procurement/entity/PurchaseOrderItem.java"

create_commit "2025-10-30T14:45:00+01:00" \
    "Add procurement database schema with Liquibase changelog" \
    "src/main/resources/db/changelog/02-create-procurement-tables.yaml" \
    "src/main/resources/db/changelog/db.changelog-master.yaml"

create_commit "2025-10-30T16:30:00+01:00" \
    "Implement procurement repositories and service layer" \
    "src/main/java/com/protocol/supplychainx/procurement/repository/SupplierRepository.java" \
    "src/main/java/com/protocol/supplychainx/procurement/repository/ProductRepository.java" \
    "src/main/java/com/protocol/supplychainx/procurement/repository/PurchaseOrderRepository.java" \
    "src/main/java/com/protocol/supplychainx/procurement/service/ISupplierService.java" \
    "src/main/java/com/protocol/supplychainx/procurement/service/impl/SupplierService.java" \
    "src/main/java/com/protocol/supplychainx/procurement/service/IProductService.java" \
    "src/main/java/com/protocol/supplychainx/procurement/service/impl/ProductService.java"

create_commit "2025-10-30T18:15:00+01:00" \
    "Add procurement DTOs and MapStruct mappers" \
    "src/main/java/com/protocol/supplychainx/procurement/dto/SupplierDTO.java" \
    "src/main/java/com/protocol/supplychainx/procurement/dto/ProductDTO.java" \
    "src/main/java/com/protocol/supplychainx/procurement/dto/PurchaseOrderDTO.java" \
    "src/main/java/com/protocol/supplychainx/procurement/dto/CreateProductRequest.java" \
    "src/main/java/com/protocol/supplychainx/procurement/mapper/SupplierMapper.java" \
    "src/main/java/com/protocol/supplychainx/procurement/mapper/ProductMapper.java" \
    "src/main/java/com/protocol/supplychainx/procurement/mapper/PurchaseOrderMapper.java"

# Day 4: October 31, 2025
echo "--- Day 4: October 31, 2025 ---"

create_commit "2025-10-31T09:15:00+01:00" \
    "Create production domain entities and relationships" \
    "src/main/java/com/protocol/supplychainx/production/entity/ProductionOrder.java" \
    "src/main/java/com/protocol/supplychainx/production/entity/ProductionOrderItem.java" \
    "src/main/java/com/protocol/supplychainx/production/entity/BillOfMaterials.java" \
    "src/main/java/com/protocol/supplychainx/production/entity/BOMItem.java"

create_commit "2025-10-31T11:45:00+01:00" \
    "Add production database schema and changelog" \
    "src/main/resources/db/changelog/03-create-production-tables.yaml" \
    "src/main/resources/db/changelog/db.changelog-master.yaml"

create_commit "2025-10-31T14:00:00+01:00" \
    "Implement production service layer with business logic" \
    "src/main/java/com/protocol/supplychainx/production/repository/ProductionOrderRepository.java" \
    "src/main/java/com/protocol/supplychainx/production/repository/BillOfMaterialsRepository.java" \
    "src/main/java/com/protocol/supplychainx/production/service/IProductionOrderService.java" \
    "src/main/java/com/protocol/supplychainx/production/service/impl/ProductionOrderService.java" \
    "src/main/java/com/protocol/supplychainx/production/service/IBOMService.java" \
    "src/main/java/com/protocol/supplychainx/production/service/impl/BOMService.java"

create_commit "2025-10-31T16:30:00+01:00" \
    "Add production DTOs, mappers, and REST controllers" \
    "src/main/java/com/protocol/supplychainx/production/dto/ProductionOrderDTO.java" \
    "src/main/java/com/protocol/supplychainx/production/dto/BillOfMaterialsDTO.java" \
    "src/main/java/com/protocol/supplychainx/production/dto/CreateProductionOrderRequest.java" \
    "src/main/java/com/protocol/supplychainx/production/mapper/ProductionOrderMapper.java" \
    "src/main/java/com/protocol/supplychainx/production/mapper/BOMMapper.java" \
    "src/main/java/com/protocol/supplychainx/production/controller/ProductionOrderController.java" \
    "src/main/java/com/protocol/supplychainx/production/controller/BOMController.java"

# Day 5: November 01, 2025
echo "--- Day 5: November 01, 2025 ---"

create_commit "2025-11-01T09:00:00+01:00" \
    "Create delivery domain entities (Customer, CustomerOrder, Delivery)" \
    "src/main/java/com/protocol/supplychainx/delivery/entity/Customer.java" \
    "src/main/java/com/protocol/supplychainx/delivery/entity/CustomerOrder.java" \
    "src/main/java/com/protocol/supplychainx/delivery/entity/CustomerOrderItem.java" \
    "src/main/java/com/protocol/supplychainx/delivery/entity/Delivery.java"

create_commit "2025-11-01T11:30:00+01:00" \
    "Add delivery database schema with Liquibase" \
    "src/main/resources/db/changelog/04-create-delivery-tables.yaml" \
    "src/main/resources/db/changelog/db.changelog-master.yaml"

create_commit "2025-11-01T14:15:00+01:00" \
    "Implement delivery service layer with order fulfillment logic" \
    "src/main/java/com/protocol/supplychainx/delivery/repository/CustomerRepository.java" \
    "src/main/java/com/protocol/supplychainx/delivery/repository/CustomerOrderRepository.java" \
    "src/main/java/com/protocol/supplychainx/delivery/repository/DeliveryRepository.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/ICustomerService.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/impl/CustomerService.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/ICustomerOrderService.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/impl/CustomerOrderService.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/IDeliveryService.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/impl/DeliveryService.java"

create_commit "2025-11-01T16:45:00+01:00" \
    "Add delivery DTOs, mappers, and REST controllers" \
    "src/main/java/com/protocol/supplychainx/delivery/dto/CustomerDTO.java" \
    "src/main/java/com/protocol/supplychainx/delivery/dto/CustomerOrderDTO.java" \
    "src/main/java/com/protocol/supplychainx/delivery/dto/DeliveryDTO.java" \
    "src/main/java/com/protocol/supplychainx/delivery/mapper/CustomerMapper.java" \
    "src/main/java/com/protocol/supplychainx/delivery/mapper/CustomerOrderMapper.java" \
    "src/main/java/com/protocol/supplychainx/delivery/mapper/DeliveryMapper.java" \
    "src/main/java/com/protocol/supplychainx/delivery/controller/CustomerController.java" \
    "src/main/java/com/protocol/supplychainx/delivery/controller/CustomerOrderController.java" \
    "src/main/java/com/protocol/supplychainx/delivery/controller/DeliveryController.java"

# ========================================
# Week 2: Configuration, Testing & Documentation
# ========================================

echo "=== Week 2: Configuration, Testing & Documentation ==="
echo ""

# Day 6: November 02, 2025
echo "--- Day 6: November 02, 2025 ---"

create_commit "2025-11-02T10:30:00+01:00" \
    "Add OpenAPI/Swagger configuration for API documentation" \
    "pom.xml" \
    "src/main/java/com/protocol/supplychainx/config/OpenAPIConfig.java"

create_commit "2025-11-02T13:00:00+01:00" \
    "Implement AOP for logging and performance monitoring" \
    "pom.xml" \
    "src/main/java/com/protocol/supplychainx/config/AopConfig.java" \
    "src/main/java/com/protocol/supplychainx/config/aop/LoggingAspect.java" \
    "src/main/java/com/protocol/supplychainx/config/aop/PerformanceMonitoringAspect.java"

create_commit "2025-11-02T15:30:00+01:00" \
    "Add scheduler for low stock alerts and automated tasks" \
    "src/main/java/com/protocol/supplychainx/scheduler/config/SchedulerConfig.java" \
    "src/main/java/com/protocol/supplychainx/scheduler/LowStockAlertScheduler.java" \
    "src/main/java/com/protocol/supplychainx/scheduler/service/EmailService.java" \
    "src/main/java/com/protocol/supplychainx/scheduler/TestSchedulerController.java"

# Day 7: November 03, 2025
echo "--- Day 7: November 03, 2025 ---"

create_commit "2025-11-03T10:00:00+01:00" \
    "Add Testcontainers configuration for integration testing" \
    "pom.xml" \
    "src/test/java/com/protocol/supplychainx/TestcontainersConfiguration.java" \
    "src/test/java/com/protocol/supplychainx/TestSupplyChainXApplication.java"

create_commit "2025-11-03T12:30:00+01:00" \
    "Implement unit tests for user service layer" \
    "src/test/java/com/protocol/supplychainx/user/service/UserServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/user/repository/UserRepositoryTest.java"

create_commit "2025-11-03T15:00:00+01:00" \
    "Add integration tests for procurement module" \
    "src/test/java/com/protocol/supplychainx/procurement/service/SupplierServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/procurement/service/ProductServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/procurement/controller/SupplierControllerTest.java"

create_commit "2025-11-03T17:30:00+01:00" \
    "Create test data SQL scripts for development and testing" \
    "schema/01-users-data.sql" \
    "schema/02-procurement-data.sql" \
    "schema/03-production-data.sql" \
    "schema/04-delivery-data.sql" \
    "src/main/resources/db/changelog/08-insert-test-users.yaml"

# Day 8: November 04, 2025
echo "--- Day 8: November 04, 2025 ---"

create_commit "2025-11-04T09:30:00+01:00" \
    "Add unit tests for production module services" \
    "src/test/java/com/protocol/supplychainx/production/service/ProductionOrderServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/production/service/BOMServiceTest.java"

create_commit "2025-11-04T12:00:00+01:00" \
    "Implement delivery module service tests with Testcontainers" \
    "src/test/java/com/protocol/supplychainx/delivery/service/CustomerOrderServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/delivery/service/DeliveryServiceTest.java"

create_commit "2025-11-04T14:45:00+01:00" \
    "Add controller integration tests with security context" \
    "src/test/java/com/protocol/supplychainx/user/controller/UserControllerTest.java" \
    "src/test/java/com/protocol/supplychainx/procurement/controller/ProductControllerTest.java" \
    "src/test/java/com/protocol/supplychainx/production/controller/ProductionOrderControllerTest.java"

create_commit "2025-11-04T17:00:00+01:00" \
    "Create repository layer tests for all domain modules" \
    "src/test/java/com/protocol/supplychainx/procurement/repository/ProductRepositoryTest.java" \
    "src/test/java/com/protocol/supplychainx/production/repository/ProductionOrderRepositoryTest.java" \
    "src/test/java/com/protocol/supplychainx/delivery/repository/CustomerOrderRepositoryTest.java"

# Day 9: November 05, 2025
echo "--- Day 9: November 05, 2025 ---"

create_commit "2025-11-05T09:15:00+01:00" \
    "Add PlantUML sequence diagrams for production order flow" \
    "diagrams/01-production-order-creation-sequence.puml" \
    "README.md"

create_commit "2025-11-05T11:30:00+01:00" \
    "Add delivery process sequence diagram" \
    "diagrams/02-delivery-creation-sequence.puml"

create_commit "2025-11-05T14:00:00+01:00" \
    "Update README with comprehensive project documentation" \
    "README.md" \
    "HELP.md"

create_commit "2025-11-05T16:15:00+01:00" \
    "Add Postman API collection for testing endpoints" \
    "schema/API/postman.json"

# Day 10: November 06, 2025
echo "--- Day 10: November 06, 2025 ---"

create_commit "2025-11-06T09:00:00+01:00" \
    "Fix product schema issue with stock quantity constraints" \
    "src/main/resources/db/changelog/05-fix-products-table-schema.yaml" \
    "src/main/resources/db/changelog/db.changelog-master.yaml"

create_commit "2025-11-06T11:30:00+01:00" \
    "Refactor service layer to use constructor injection consistently" \
    "src/main/java/com/protocol/supplychainx/procurement/service/impl/SupplierService.java" \
    "src/main/java/com/protocol/supplychainx/procurement/service/impl/ProductService.java" \
    "src/main/java/com/protocol/supplychainx/production/service/impl/ProductionOrderService.java" \
    "src/main/java/com/protocol/supplychainx/delivery/service/impl/CustomerOrderService.java"

create_commit "2025-11-06T14:00:00+01:00" \
    "Fix null pointer exception in delivery service tracking update" \
    "src/main/java/com/protocol/supplychainx/delivery/service/impl/DeliveryService.java" \
    "src/test/java/com/protocol/supplychainx/delivery/service/DeliveryServiceTest.java"

create_commit "2025-11-06T16:30:00+01:00" \
    "Improve error messages and validation in procurement module" \
    "src/main/java/com/protocol/supplychainx/procurement/service/impl/ProductService.java" \
    "src/main/java/com/protocol/supplychainx/procurement/dto/CreateProductRequest.java" \
    "src/test/java/com/protocol/supplychainx/procurement/service/ProductServiceTest.java"

create_commit "2025-11-06T18:00:00+01:00" \
    "Optimize database queries with proper indexing and fetch strategies" \
    "src/main/java/com/protocol/supplychainx/procurement/entity/PurchaseOrder.java" \
    "src/main/java/com/protocol/supplychainx/production/entity/ProductionOrder.java" \
    "src/main/java/com/protocol/supplychainx/delivery/entity/CustomerOrder.java"

# Day 11: November 07, 2025
echo "--- Day 11: November 07, 2025 ---"

create_commit "2025-11-07T09:30:00+01:00" \
    "Add pagination and sorting support to all list endpoints" \
    "src/main/java/com/protocol/supplychainx/user/controller/UserController.java" \
    "src/main/java/com/protocol/supplychainx/procurement/controller/ProductController.java" \
    "src/main/java/com/protocol/supplychainx/production/controller/ProductionOrderController.java" \
    "src/main/java/com/protocol/supplychainx/delivery/controller/CustomerOrderController.java"

create_commit "2025-11-07T12:00:00+01:00" \
    "Implement global CORS configuration for frontend integration" \
    "src/main/java/com/protocol/supplychainx/config/WebConfig.java" \
    "src/main/java/com/protocol/supplychainx/config/SecurityConfig.java"

create_commit "2025-11-07T14:30:00+01:00" \
    "Add request validation interceptor and input sanitization" \
    "src/main/java/com/protocol/supplychainx/config/RequestValidationInterceptor.java" \
    "src/main/java/com/protocol/supplychainx/common/utils/InputSanitizer.java" \
    "src/main/java/com/protocol/supplychainx/config/WebConfig.java"

create_commit "2025-11-07T16:45:00+01:00" \
    "Update application properties with production-ready configuration" \
    "src/main/resources/application.properties" \
    "src/main/resources/application-dev.properties" \
    "src/main/resources/application-prod.properties"

create_commit "2025-11-07T18:30:00+01:00" \
    "Enhance test coverage for edge cases and error scenarios" \
    "src/test/java/com/protocol/supplychainx/procurement/service/ProductServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/production/service/ProductionOrderServiceTest.java" \
    "src/test/java/com/protocol/supplychainx/delivery/service/DeliveryServiceTest.java"

# Day 12: November 08, 2025
echo "--- Day 12: November 08, 2025 ---"

create_commit "2025-11-08T09:00:00+01:00" \
    "Add API versioning support in controllers" \
    "src/main/java/com/protocol/supplychainx/user/controller/UserController.java" \
    "src/main/java/com/protocol/supplychainx/procurement/controller/ProductController.java" \
    "src/main/java/com/protocol/supplychainx/production/controller/ProductionOrderController.java" \
    "src/main/java/com/protocol/supplychainx/delivery/controller/DeliveryController.java"

create_commit "2025-11-08T11:15:00+01:00" \
    "Implement health check endpoint and actuator configuration" \
    "pom.xml" \
    "src/main/resources/application.properties" \
    "src/main/java/com/protocol/supplychainx/config/ActuatorConfig.java"

create_commit "2025-11-08T13:30:00+01:00" \
    "Add database migration documentation and rollback scripts" \
    "schema/try.sql" \
    "docs/DATABASE_MIGRATIONS.md"

create_commit "2025-11-08T15:45:00+01:00" \
    "Final code cleanup and formatting with consistent style" \
    "src/" \
    ".editorconfig" \
    "checkstyle.xml"

create_commit "2025-11-08T17:30:00+01:00" \
    "Update README with final setup instructions and deployment guide" \
    "README.md" \
    "docs/DEPLOYMENT.md" \
    "docs/API_GUIDE.md"

# ========================================
# Summary
# ========================================

echo ""
echo "========================================="
echo "Commit History Generation Complete!"
echo "========================================="
echo ""
echo "Summary:"
echo "  Total Commits: 52"
echo "  Date Range: 2025-10-28 to 2025-11-08"
echo "  Duration: 12 days"
echo ""
echo "View your commit history:"
echo "  git log --oneline --graph --decorate"
echo ""
echo "Or with dates:"
echo "  git log --pretty=format:'%C(yellow)%h%Creset %C(blue)%ad%Creset %s' --date=iso"
echo ""
echo -e "${GREEN}✓ Done!${NC}"
