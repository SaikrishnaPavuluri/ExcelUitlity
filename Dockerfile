# Use a base image with Java 17
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the application JAR file to the container
COPY target/SpringBoot-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application is running on
EXPOSE 8081

# Set the command to run the application
CMD ["java", "-jar", "app.jar"]

