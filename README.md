# Java Spring Notes App

A comprehensive, production-ready Notes/Task Management REST API built with Spring Boot 3, featuring JWT authentication, role-based authorization, Redis caching, and comprehensive testing.

## 🌟 Features

### 📝 Core Functionality

- **CRUD Operations**: Create, read, update, and delete notes
- **User Management**: User registration and authentication
- **Advanced Filtering**: Filter notes by status, priority, category, and date range
- **Search**: Full-text search across note titles and content
- **Statistics**: Comprehensive note statistics and analytics
- **Note Management**: Archive, complete, and reactivate notes

### 🔐 Security & Authentication

- **JWT Authentication**: Secure token-based authentication
- **Role-based Authorization**: User and Admin roles with different permissions
- **Password Encryption**: BCrypt password hashing
- **Spring Security**: Comprehensive security configuration

### ⚡ Performance & Scalability

- **Redis Caching**: High-performance caching layer
- **Pagination**: Efficient data retrieval with sorting
- **Database Optimization**: JPA/Hibernate with optimized queries
- **Connection Pooling**: PostgreSQL with connection pooling

### 🛠️ Development & Operations

- **Docker Support**: Complete containerization with docker-compose
- **Database Migration**: Flyway for version-controlled database changes
- **API Documentation**: OpenAPI 3.0 (Swagger) documentation
- **Comprehensive Testing**: Unit, integration, and service layer tests
- **Health Checks**: Application health monitoring
- **Environment Configuration**: Multiple environment support (dev, prod)

## 🏗️ Tech Stack

### Backend Framework

- **Spring Boot 3.5.4** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Data persistence layer
- **Spring Data Redis** - Caching layer

### Database & Storage

- **PostgreSQL** - Primary database (production)
- **H2 Database** - In-memory database (testing)
- **Redis** - Caching and session storage
- **Flyway** - Database migration tool

### Build & Deployment

- **Maven** - Dependency management and build tool
- **Docker & Docker Compose** - Containerization
- **Java 17** - Runtime environment

### Documentation & Testing

- **SpringDoc OpenAPI** - API documentation
- **JUnit 5** - Unit testing framework
- **Testcontainers** - Integration testing
- **Lombok** - Code generation

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Maven 3.6 or higher
- Docker and Docker Compose (for containerized setup)
- PostgreSQL (for local development)
- Redis (for caching)

### 1. Clone the Repository

```bash
git clone https://github.com/hakimasyrofi/java-spring-notes-app.git
cd java-spring-notes-app
```

### 2. Environment Setup

#### Option A: Docker Compose (Recommended)

```bash
# Copy environment file
cp .env.dev .env

# Start all services
docker-compose up -d

# View logs
docker-compose logs -f notes-api
```

#### Option B: Local Development

```bash
# Install dependencies
./mvnw clean install

# Configure environment variables
export JWT_SECRET=your-secret-key-here
export JWT_EXPIRATION=86400000
export DB_USERNAME=your-db-username
export DB_PASSWORD=your-db-password
export DB_NAME=notesdb

# Run the application
./mvnw spring-boot:run
```

### 3. Access the Application

- **API Base URL**: http://localhost:8080
- **Swagger Documentation**: http://localhost:8080/swagger-ui.html
- **Database Admin (Adminer)**: http://localhost:8081 (Docker only)
- **Health Check**: http://localhost:8080/actuator/health

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Specific Test Categories

```bash
# Unit tests only
./mvnw test -Dtest="*Test"

# Integration tests only
./mvnw test -Dtest="*IntegrationTest"

# Service layer tests
./mvnw test -Dtest="*ServiceTest"
```

### Test Coverage

The project includes comprehensive testing:

- **Unit Tests**: Service layer, utilities, and components
- **Integration Tests**: Full API endpoint testing
- **Controller Tests**: Web layer testing with MockMvc
- **Repository Tests**: Data layer testing

## 🐳 Docker Deployment

### Development Environment

```bash
# Start development environment
docker-compose up -d

# Scale API instances
docker-compose up -d --scale notes-api=3
```

### Production Environment

```bash
# Use production configuration
cp .env.prod .env
docker-compose --env-file .env.prod -f docker-compose.yml up -d
```

### Docker Services

- **notes-api**: Spring Boot application
- **postgres**: PostgreSQL database
- **redis**: Redis cache
- **adminer**: Database administration tool (optional)

## 📊 Monitoring & Health Checks

### Health Endpoints

- **Application Health**: `/actuator/health`
- **Application Info**: `/actuator/info`
- **Custom Health Checks**: Database, Redis connectivity

### Logging

Structured logging with different levels:

- Application logs: `com.spring.notes.app`
- SQL logs: Hibernate SQL queries
- Security logs: Authentication/authorization events

## 🔒 Security Features

### Authentication Flow

1. User registers with username, email, and password
2. User logs in with credentials
3. Server returns JWT token
4. Client includes token in Authorization header
5. Server validates token for protected endpoints

### Authorization Levels

- **PUBLIC**: Registration, login, home page
- **USER**: CRUD operations on own notes
- **ADMIN**: Manage all notes, view statistics, Redis management

### Security Headers

- CORS configuration
- CSRF protection
- Security headers (XSS, clickjacking protection)

## 🚀 Performance Features

### Caching Strategy

- **Redis**: User sessions, frequently accessed data
- **JPA Second Level Cache**: Entity caching
- **Query Result Caching**: Expensive queries

### Database Optimization

- **Indexes**: Optimized database indexes
- **Connection Pooling**: HikariCP connection pool
- **Lazy Loading**: Efficient entity relationships

## 🛠️ Development

### Code Quality

- **Lombok**: Reduces boilerplate code
- **Validation**: Bean validation with custom messages
- **Exception Handling**: Global exception handler
- **API Responses**: Standardized response format

### Project Structure

```
src/
├── main/
│   ├── java/com/spring/notes/app/
│   │   ├── config/          # Configuration classes
│   │   ├── controller/      # REST controllers
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── entity/         # JPA entities
│   │   ├── exception/      # Custom exceptions
│   │   ├── repository/     # Data repositories
│   │   └── service/        # Business logic
│   └── resources/
│       ├── db/migration/   # Flyway migrations
│       └── application.properties
└── test/                   # Test classes
```

## 👨‍💻 Author

**Hakim Asyrofi**

- GitHub: [@hakimasyrofi](https://github.com/hakimasyrofi)

---

**Happy Coding! 🚀**
