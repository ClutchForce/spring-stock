# dashboard-backend/Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Copy the parent POM
COPY pom.xml .

# Copy all module POMs (needed for dependency resolution)
COPY market-simulator/pom.xml ./market-simulator/
COPY stream-processor/pom.xml ./stream-processor/
COPY dashboard-backend/pom.xml ./dashboard-backend/

# Copy dashboard-backend source code
COPY dashboard-backend/src ./dashboard-backend/src

# Build only the dashboard-backend module (but parent POM is available)
RUN mvn clean package -pl dashboard-backend -am -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the built JAR from the build stage
COPY --from=build /build/dashboard-backend/target/*.jar app.jar

# Render provides PORT environment variable
EXPOSE ${PORT:-8082}

# Use PORT from environment
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT:-8082} -jar app.jar"]