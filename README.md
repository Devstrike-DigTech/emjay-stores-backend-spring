# Emjay Stores Backend

A comprehensive backend system for beauty retail operations built with Spring Boot, Kotlin, and Clean Architecture.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
│         (Controllers, Request/Response Models)               │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
│        (Use Cases, DTOs, Services, Mappers)                  │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│         (Entities, Repository Interfaces)                    │
│              [Business Logic Core]                           │
└─────────────────────────────────────────────────────────────┘
                            ↑
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                        │
│    (JPA Entities, Repository Impl, Security, Config)         │
└─────────────────────────────────────────────────────────────┘
```

## 📦 Technology Stack

- **Framework**: Spring Boot 3.2.1
- **Language**: Kotlin 1.9.21
- **Database**: PostgreSQL 15+
- **Security**: Spring Security + JWT
- **Build Tool**: Gradle with Kotlin DSL
- **API Documentation**: SpringDoc OpenAPI 3 (Swagger)
- **Database Migration**: Flyway
- **Testing**: JUnit 5, MockK, Testcontainers

## 🚀 Phase 1 Features

### Authentication & Authorization
- [x] JWT-based authentication
- [x] Role-based access control (Admin, Manager, Staff)
- [x] User registration and login
- [x] Refresh token mechanism
- [x] Password management (reset, change)

### Inventory Management
- [x] Product catalog with SKU management
- [x] Real-time stock tracking
- [x] Low stock and out-of-stock alerts
- [x] Product categorization
- [x] Supplier management
- [x] Stock adjustment logging

## 📁 Project Structure

```
emjay-backend/
├── src/
│   ├── main/
│   │   ├── kotlin/com/emjay/backend/
│   │   │   ├── domain/                    # Core business logic
│   │   │   │   ├── entity/                # Domain entities
│   │   │   │   │   ├── user/              # User, UserRole, RefreshToken
│   │   │   │   │   ├── product/           # Product, ProductStatus
│   │   │   │   │   ├── category/          # Category
│   │   │   │   │   ├── supplier/          # Supplier
│   │   │   │   │   └── inventory/         # StockAdjustment
│   │   │   │   ├── repository/            # Repository interfaces (ports)
│   │   │   │   │   ├── user/
│   │   │   │   │   ├── product/
│   │   │   │   │   ├── category/
│   │   │   │   │   ├── supplier/
│   │   │   │   │   └── inventory/
│   │   │   │   └── exception/             # Domain exceptions
│   │   │   ├── application/               # Application layer
│   │   │   │   ├── dto/                   # Data Transfer Objects
│   │   │   │   ├── usecase/               # Use case implementations
│   │   │   │   ├── service/               # Application services
│   │   │   │   └── mapper/                # Entity/DTO mappers
│   │   │   ├── infrastructure/            # Infrastructure layer
│   │   │   │   ├── persistence/           # Database implementations
│   │   │   │   │   ├── entity/            # JPA entities
│   │   │   │   │   └── repository/        # JPA repositories
│   │   │   │   ├── security/              # Security implementations
│   │   │   │   │   ├── jwt/               # JWT utilities
│   │   │   │   │   └── filter/            # Security filters
│   │   │   │   └── config/                # Configuration classes
│   │   │   └── presentation/              # Presentation layer
│   │   │       ├── controller/            # REST controllers
│   │   │       ├── request/               # Request models
│   │   │       ├── response/              # Response models
│   │   │       └── exception/             # Exception handlers
│   │   └── resources/
│   │       ├── application.yml            # Main configuration
│   │       ├── application-dev.yml        # Development config
│   │       ├── application-prod.yml       # Production config
│   │       └── db/migration/              # Flyway migrations
│   └── test/                              # Test sources
├── build.gradle.kts                       # Gradle build configuration
├── settings.gradle.kts                    # Gradle settings
├── gradle.properties                      # Gradle properties
└── README.md                              # This file
```

## 🔧 Setup & Installation

### Prerequisites
- JDK 17 or higher
- PostgreSQL 15+
- Gradle 8.x (or use included wrapper)

### Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE emjay_db;
CREATE USER emjay_user WITH PASSWORD 'emjay_pass';
GRANT ALL PRIVILEGES ON DATABASE emjay_db TO emjay_user;
```

2. For development:
```sql
CREATE DATABASE emjay_dev_db;
CREATE USER emjay_dev WITH PASSWORD 'emjay_dev';
GRANT ALL PRIVILEGES ON DATABASE emjay_dev_db TO emjay_dev;
```

### Running the Application

1. **Development Mode**:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

2. **Production Mode**:
```bash
./gradlew bootRun --args='--spring.profiles.active=prod'
```

3. **Build JAR**:
```bash
./gradlew build
java -jar build/libs/emjay-backend-0.0.1-SNAPSHOT.jar
```

### Environment Variables

```bash
# Database
export DB_USERNAME=emjay_user
export DB_PASSWORD=emjay_pass

# JWT (IMPORTANT: Change in production!)
export JWT_SECRET=your-secure-secret-key-minimum-256-bits
export JWT_ACCESS_EXPIRATION=900000    # 15 minutes
export JWT_REFRESH_EXPIRATION=604800000 # 7 days

# Server
export SERVER_PORT=8080

# CORS
export CORS_ORIGINS=http://localhost:3000,http://localhost:4200
```

## 📚 API Documentation

Once the application is running, access the Swagger UI at:
- **Development**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs

## 🔐 Security

### User Roles
- **ADMIN**: Full system access, user management, system configuration
- **MANAGER**: Inventory management, staff scheduling, order processing
- **STAFF**: Personal profile, clock-in/out, assigned tasks

### JWT Configuration
- Access tokens expire in 15 minutes
- Refresh tokens expire in 7 days
- Tokens are signed using RS256 algorithm
- Failed login attempts are tracked

## 🧪 Testing

Run all tests:
```bash
./gradlew test
```

Run tests with coverage:
```bash
./gradlew test jacocoTestReport
```

## 🔧 Troubleshooting

### PostgreSQL Permission Denied Error

If you encounter `ERROR: permission denied for schema public`, this is due to PostgreSQL 15+ security changes.

**Quick Fix:**
```bash
# Run the setup script
chmod +x setup-db.sh
./setup-db.sh
```

**Manual Fix:**
```sql
-- Connect as postgres superuser
psql -U postgres -d emjay_dev_db

-- Grant permissions
GRANT ALL ON SCHEMA public TO emjay_dev;
ALTER SCHEMA public OWNER TO emjay_dev;
```

### Database Connection Issues

1. **Check PostgreSQL is running:**
   ```bash
   pg_isready
   ```

2. **Verify database exists:**
   ```bash
   psql -U postgres -c "\l" | grep emjay
   ```

3. **Test connection:**
   ```bash
   psql -U emjay_dev -d emjay_dev_db -c "SELECT version();"
   ```

### Port Already in Use

If port 8080 is busy:
```bash
# Change port in application-dev.yml or use environment variable
export SERVER_PORT=8081
./gradlew bootRun --args='--spring.profiles.active=dev'
```

## 📝 Domain Models

### User
- Represents system users (Admin, Manager, Staff)
- Includes authentication and authorization
- Tracks user activity and verification status

### Product
- Core inventory item
- Includes pricing, stock levels, and status
- Linked to categories and suppliers

### Category
- Hierarchical product categorization
- Supports parent-child relationships

### Supplier
- Supplier information and contact details
- Linked to products

### StockAdjustment
- Audit trail for inventory changes
- Tracks additions, deductions, sales, and returns

## 🗄️ Database Schema

### Tables (Phase 1)
- `users` - System users
- `refresh_tokens` - JWT refresh tokens
- `products` - Product catalog
- `categories` - Product categories
- `suppliers` - Product suppliers
- `stock_adjustments` - Inventory change log

## 🔄 Next Steps

### Phase 2: Staff Management
- Staff profiles and employment details
- Shift scheduling system
- Clock-in/out tracking
- Leave request management
- Performance metrics

### Phase 3: E-commerce
- Customer-facing product catalog
- Shopping cart functionality
- Order processing
- Payment integration

## 🤝 Contributing

1. Follow Clean Architecture principles
2. Write tests for new features
3. Follow Kotlin coding conventions
4. Update documentation

## 📄 License

Copyright © 2026 Emjay Stores. All rights reserved.
