# Dockerfile — uscite-service
# Mono-repo: il build avviene dalla radice per avere il parent pom e bard-common.

# ---- Stage 1: build ----
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

# Copia prima i pom (parent + moduli): il parent elenca TUTTI i moduli,
# quindi Maven esige che i pom di tutti siano presenti.
COPY pom.xml pom.xml
COPY bard-common/pom.xml bard-common/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY ballerini-service/pom.xml ballerini-service/pom.xml
COPY presenza-service/pom.xml presenza-service/pom.xml
COPY uscite-service/pom.xml uscite-service/pom.xml
COPY gateway/pom.xml gateway/pom.xml
COPY eureka-service/pom.xml eureka-service/pom.xml

RUN mvn -B -pl uscite-service -am dependency:go-offline

COPY bard-common/src bard-common/src
COPY uscite-service/src uscite-service/src

RUN mvn -B -pl uscite-service -am clean package -DskipTests

# ---- Stage 2: agente OpenTelemetry ----
FROM otel/autoinstrumentation-java:latest AS otel-agent

# ---- Stage 3: runtime ----
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S bard && adduser -S bard -G bard

WORKDIR /app

COPY --from=otel-agent /javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=build /workspace/uscite-service/target/*.jar /app/application.jar

RUN chown -R bard:bard /app
USER bard

EXPOSE 8083

ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "/app/application.jar"]
