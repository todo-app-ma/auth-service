# Stage 1: Build the app
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Install git (optional, only if Maven plugins need it)
RUN apt-get update && apt-get install -y git

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# Stage 2: Run the app
FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081 9091

ENTRYPOINT ["java", "-jar", "app.jar"]