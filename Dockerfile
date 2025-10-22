# Stage 1: Build the application
FROM maven:3.9.4-eclipse-temurin-17 AS builder

# Set working directory inside the container
WORKDIR /app

# Copy Maven descriptor and download dependencies first (for better caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the rest of the source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:17-jdk-jammy

# Set working directory
WORKDIR /app

# Copy only the packaged jar from the builder stage
COPY --from=builder /app/target/*.jar app.jar

# Expose the application port (use variable for flexibility)
EXPOSE 8080

# Run the Spring Boot app
ENTRYPOINT ["java", "-jar", "app.jar"]
