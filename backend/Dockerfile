# --- Stage 1: Build the Application ---
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Copy only the POM first to cache dependencies (optimizes build speed)
COPY pom.xml .
# Download dependencies (this layer will be cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B
COPY src ./src
# Package the application, skipping tests for faster build in this step
RUN mvn clean package -DskipTests

# --- Stage 2: Run the Application ---
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy the built JAR from the 'build' stage
# We rename it to 'app.jar' for simplicity
COPY --from=build /app/target/*.jar app.jar

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]