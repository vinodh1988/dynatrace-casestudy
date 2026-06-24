FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /workspace
COPY pom.xml .
COPY order-api/pom.xml order-api/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
RUN mvn -q -pl order-api -am dependency:go-offline

COPY order-api/src order-api/src
RUN mvn -q -pl order-api -am -DskipTests package

FROM eclipse-temurin:11-jre

WORKDIR /app
COPY --from=build /workspace/order-api/target/order-api-1.0.0.jar app.jar

EXPOSE 8080 9010
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
