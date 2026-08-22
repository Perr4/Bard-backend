# Immagine temporanea usata per compilare il progetto con Maven e Java 21.
FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

# Cartella di lavoro utilizzata durante la compilazione.
WORKDIR /workspace

# Copia il POM del singolo microservizio.
COPY pom.xml pom.xml

# Scarica preventivamente le dipendenze Maven.
RUN mvn dependency:go-offline -B

# Copia il codice sorgente del singolo microservizio.
COPY src src

# Compila l'applicazione senza eseguire i test.
RUN mvn clean package -DskipTests -B

# Immagine dalla quale viene prelevato l'agente OpenTelemetry.
FROM otel/autoinstrumentation-java:latest AS otel-agent

# Immagine finale contenente solamente Java e l'applicazione.
FROM eclipse-temurin:21-jre-alpine

# Crea un utente non amministratore.
RUN addgroup -S chronos && adduser -S chronos -G chronos

# Cartella di lavoro dell'applicazione.
WORKDIR /app

# Copia l'agente OpenTelemetry.
COPY --from=otel-agent /javaagent.jar /app/opentelemetry-javaagent.jar

# Copia il JAR generato durante la compilazione.
COPY --from=build /workspace/target/*.jar /app/application.jar

# Assegna i file all'utente chronos.
RUN chown -R chronos:chronos /app

# Esegue il container senza privilegi amministrativi.
USER chronos

# Avvia Spring Boot insieme all'agente OpenTelemetry.
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "/app/application.jar"]