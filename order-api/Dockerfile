FROM eclipse-temurin:11-jre

WORKDIR /app
COPY order-api/target/order-api-1.0.0.jar app.jar

EXPOSE 8080 9010
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
