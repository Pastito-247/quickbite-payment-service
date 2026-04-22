FROM maven:3.9.6-eclipse-temurin-17 AS build
WORKDIR /app
COPY payment-service/pom.xml .
COPY payment-service/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/payment-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
