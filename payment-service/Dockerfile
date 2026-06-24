FROM eclipse-temurin:11-jre

WORKDIR /app
COPY payment-service/target/payment-service-1.0.0.jar app.jar

EXPOSE 8081 9011
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
