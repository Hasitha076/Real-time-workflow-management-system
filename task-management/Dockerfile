# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/project-management-1.0-SNAPSHOT.jar app.jar

# Expose the application port
EXPOSE 8082

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
