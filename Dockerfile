FROM maven:4.0.0-rc-5-eclipse-temurin-25-alpine AS builder
LABEL authors="Dmitriy-Gorodilov"
WORKDIR /workspace
COPY pom.xml .
COPY src src
COPY docs/openapi.yaml src/main/resources/static/openapi.yaml
RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
LABEL authors="Dmitriy-Gorodilov"
WORKDIR /app
COPY --from=builder /workspace/target/*.jar ./app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]