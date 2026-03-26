# Build stage
FROM maven:3.8.4-openjdk-17-slim AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml and download dependencies (cached unless pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jdk-slim

# Create non-root user for security
RUN groupadd -r springboot && \
    useradd -r -g springboot springboot

# Set working directory
WORKDIR /app

# Copy JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown springboot:springboot app.jar

# Switch to non-root user
USER springboot

# Expose port
EXPOSE 8080

# Health check (optional but good for deployment platforms)
HEALTHCHECK --interval=30s --timeout=3s --start-period=15s --retries=3 \
  CMD java -cp app.jar org.springframework.boot.actuate.health.HealthEndpoint || exit 1

# Run the application
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]