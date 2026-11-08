# Dockerfile — gateway
# Mono-repo: il build avviene dalla radice per avere il parent pom.

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
COPY gateway/pom.xml gateway/pom.xml
COPY eureka-service/pom.xml eureka-service/pom.xml

RUN mvn -B -pl gateway -am dependency:go-offline

COPY gateway/src gateway/src

RUN mvn -B -pl gateway -am clean package -DskipTests

# ---- Stage 2: runtime ----
FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S bard && adduser -S bard -G bard

WORKDIR /app

COPY --from=build /workspace/gateway/target/*.jar /app/application.jar

RUN chown -R bard:bard /app
USER bard

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/application.jar"]
