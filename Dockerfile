# Use a lightweight builder image
FROM gradle:8.4-jdk17 AS build

# Set working directory
WORKDIR /app

# Copy only necessary files to leverage Docker cache
COPY build.gradle settings.gradle gradlew ./
COPY gradle gradle
COPY src src

# Build the application
RUN ./gradlew bootJar --no-daemon

# Use a smaller runtime image for the final build
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Expose the application port
EXPOSE 8080

# Copy the built JAR file from the builder image
COPY --from=build /app/build/libs/*.jar app.jar

# Set the entry point for the container
ENTRYPOINT ["java", "-jar", "app.jar"]
