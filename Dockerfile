# Production Dockerfile for SupplyChainX
# Build the JAR locally first: ./mvnw clean package -DskipTests
# Then run: docker compose up --build

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY target/SupplyChainX-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]