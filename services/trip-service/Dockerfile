# syntax=docker/dockerfile:1
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY src src
RUN --mount=type=cache,id=smartbus-organized-maven,target=/root/.m2,sharing=locked mvn -B verify
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN mkdir -p /var/lib/smartbus/keys && chown -R 10001:10001 /var/lib/smartbus
COPY --from=build /build/target/*-1.0.0.jar app.jar
USER 10001:10001
ENTRYPOINT ["java","-jar","app.jar"]
