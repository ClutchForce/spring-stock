# stream-processor/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the parent POM
COPY pom.xml .

# Copy all module POMs (needed for dependency resolution)
COPY market-simulator/pom.xml ./market-simulator/
COPY stream-processor/pom.xml ./stream-processor/
COPY dashboard-backend/pom.xml ./dashboard-backend/

# Copy stream-processor source code
COPY stream-processor/src ./stream-processor/src

# Build only the stream-processor module (but parent POM is available)
RUN mvn clean package -pl stream-processor -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/stream-processor/target/*.jar app.jar

# Stream processor doesn't need a web port, but we'll expose one for health checks if needed
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]