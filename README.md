## NEWWORK HR Backend Service

A modern and simple Java Spring Boot backend service. This project is designed to manage employee profiles, feedback, and absence requests with a role-based permission system.

The application is fully containerized with Docker for consistent and reliable deployments.

### Considerations
- This project is a simpler version of the SPA (React + Django) Newwork HR application, focusing solely on the backend service implemented in Java with Spring Boot.
- The API implementation leverages Spring Boot and Spring Data JPA to accelerate endpoint implementation.
- User authentication is handled via Spring Security with stateless JWT for API access.
- Focused on Profile and Absence management with basic CRUD operations.

### Backend Tech Stack
- Core: Java 21, Spring Boot 3.5.8
- Data: Spring Data JPA (Hibernate), PostgreSQL 17
- Cache: Redis (very simple implemention)
- Security: Spring Security (Stateless JWT)
- Migrations: Flyway
- Testing: JUnit 5, MockMvc, Testcontainers (Integration), ArchUnit (Architecture enforcement)
- API Documentation: SpringDoc OpenAPI (Swagger UI)
- Dependency Management: Maven

### Pre-requisites
- Docker Compose
---

## Setup Instructions
1. **Clone the Repository**
    ```bash
      git clone https://github.com/RicardoMiranda7/newwork_java.git
      cd newwork/backend
    ```
2. **Default Logins**

   The application is seeded with demo data. You can log in with:
    - Manager: manager@example.com / password123
    - Employee: john.smith@example.com / password123
    - Co-worker: john.doe@example.com / password123

3. **API Documentation**
    - A Postman collection is available in the `docs/` directory for testing the API endpoints.
    - Once the backend is running, access the API documentation at:
      ```html
      http://localhost:8080/swagger-ui/index.html
      ```
---

## Running the Application
### Production Environment (Unified Build)
1. **Build and Run**
   - From the project root, run
    ```bash
    docker-compose up --build
    ```

3. **Access the Application**
   - API Documentation (Swagger): http://localhost:8080/swagger-ui/index.html

4. **Stop the Application**
   - To stop the application, run:
    ```bash
    docker-compose down
    ```
---

### Development Environment (Services Run in IDEs)
1. **Start the db**
    - From the project root, run:
        ```bash
        docker-compose -f docker-compose.dev.yml up
        ```

2. **Start the backend from your IDE (run migrations, seed demo data and the API app)**
    - Open the backend/ directory in your Java IDE (e.g., IDEA).
    - Run a Spring Boot development server (available on package `com.newwork.backend`)

3. **Access the Application**
    - The backend is now running at http://localhost:8080/
    - API Documentation (Swagger): http://localhost:8080/swagger-ui/index.html
    - Actuator Endpoints: http://localhost:8080/actuator

4. **Stop the Application**
    - Close the Spring Boot application from your IDE
    - To stop the db, run:
       ```bash
       docker-compose -f docker-compose.dev.yml down
       ```
