# Stage 1: Build the application (uses a Maven image with JDK 17)
# This stage compiles the code and creates the JAR file
FROM maven:3.9.5-eclipse-temurin-17 AS build

# Set the working directory inside the container
WORKDIR /app

# Copy the project files (pom.xml, source code, and resources)
COPY pom.xml .
COPY src src

# Build the project (creates the executable JAR in /app/target/)
RUN mvn clean package -DskipTests

# Stage 2: Create a minimal runtime image (uses a smaller JRE image)
# This results in a smaller, faster, and more secure final image
FROM eclipse-temurin:17-jre-alpine

# Set the working directory
WORKDIR /app

# Copy the generated JAR (vehicle-0.0.1-SNAPSHOT.jar) from the 'build' stage
# This line is now confirmed to be correct based on your pom.xml
COPY --from=build /app/target/vehicle-0.0.1-SNAPSHOT.jar app.jar

# Spring Boot default port is 8080. Render will handle the port mapping.
EXPOSE 8080

# The command that runs your application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]