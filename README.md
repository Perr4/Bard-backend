# BARD Backend

Mono-repo Maven multi-module dei microservizi BARD.

## Moduli

| Modulo | Descrizione | Porta |
|---|---|---|
| `auth-service` | Autenticazione, JWT, ruoli | 7999 |
| `ballerini-service` | Anagrafica ballerini (profilo utente) | 8081 |
| `presenza-service` | Gestione presenze | 8082 |
| `gateway` | API Gateway (WebFlux) | 8080 |
| `eureka-service` | Service discovery | 8761 |

## Stack unificato

- **Java 21**
- **Spring Boot 4.1.1**
- **Spring Cloud 2025.1.3** (Eureka)
- **PostgreSQL** (schema `bard`, vedi `sql/schema.sql`)

## Build e test

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# tutta la repo
mvn -B test

# un singolo modulo
mvn -B test -pl auth-service
```

## Struttura

```
Bard-backend/
├── pom.xml            # parent Maven condiviso
├── auth-service/
├── ballerini-service/
├── presenza-service/
├── gateway/
├── eureka-service/
├── sql/               # schema database (riferimento)
└── docs/              # documentazione di progetto
```

## Avvio

1. PostgreSQL + Kafka
2. `eureka-service`
3. `auth-service`, `ballerini-service`, `presenza-service`
4. `gateway`
