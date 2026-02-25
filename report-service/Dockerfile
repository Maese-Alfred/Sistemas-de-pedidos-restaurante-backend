FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy entire project (important for multi-module)
COPY . .

# Build only report-service but keep module graph intact
RUN mvn clean package -DskipTests -pl report-service -am

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

COPY --from=build /app/report-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
