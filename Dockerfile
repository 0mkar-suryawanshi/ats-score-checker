# Step 3.1: Specify base image
FROM eclipse-temurin:21-jre 

# Step 3.2: Set working directory inside container
WORKDIR /app

# Step 3.3: Copy the JAR file from your target folder
COPY src/target/*.jar app.jar

# Step 3.4: Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Step 3.5: Command to run the application
ENTRYPOINT ["java", "-jar", "app.jar"]