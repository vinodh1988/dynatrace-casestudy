FROM maven:3.9.9-eclipse-temurin-11 AS build

WORKDIR /workspace
COPY pom.xml .
COPY order-api/pom.xml order-api/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
RUN mvn -q -pl payment-service -am dependency:go-offline

COPY payment-service/src payment-service/src
RUN mvn -q -pl payment-service -am -DskipTests package

FROM eclipse-temurin:11-jre

WORKDIR /app
COPY --from=build /workspace/payment-service/target/payment-service-1.0.0.jar app.jar

EXPOSE 8081 9011
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
