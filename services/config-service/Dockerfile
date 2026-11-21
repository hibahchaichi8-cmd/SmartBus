FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 mvn -B package

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/config-service-1.0.0.jar app.jar

USER 10001
EXPOSE 8888

ENTRYPOINT ["java", "-jar", "app.jar"]