# market-simulator/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the parent POM
COPY pom.xml .

# Copy all module POMs (needed for dependency resolution)
COPY market-simulator/pom.xml ./market-simulator/
COPY stream-processor/pom.xml ./stream-processor/
COPY dashboard-backend/pom.xml ./dashboard-backend/

# Copy market-simulator source code
COPY market-simulator/src ./market-simulator/src

# Build only the market-simulator module (but parent POM is available)
RUN mvn clean package -pl market-simulator -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/market-simulator/target/*.jar app.jar

# Market simulator doesn't need a web port, but we'll expose one for health checks if needed
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]