# BARD — Documentazione del backend

Documentazione di progetto del backend **BARD** (CHRONOS).
Per la guida rapida all'avvio si veda anche [`../README.md`](../README.md).

## Indice

- [Che cos'è](#che-cosè)
- [Architettura](#architettura)
- [Flusso delle informazioni](#flusso-delle-informazioni)
- [I moduli](#i-moduli)
- [Modello dei dati](#modello-dei-dati)
- [Tecnologie](#tecnologie)
- [Uso](#uso)
- [Sviluppi futuri](#sviluppi-futuri)

---

## Che cos'è

Il backend BARD è un insieme di **5 microservizi Spring Boot** che gestiscono:

- **identità e accesso** — registrazione, login, token JWT, ruoli;
- **anagrafica dei ballerini** — il profilo dei destinatari delle attività;
- **presenze** — registrazione, aggiornamento, lettura e cancellazione.

I servizi sono esposti da un **API Gateway** e si registrano su un server **Eureka**;
i dati vivono in un unico **PostgreSQL** (schema `bard`).

Il tutto vive in una **mono-repo Maven multi-module** con parent condiviso
`io.bard:bard-parent` e stack unificato: **Java 21**, **Spring Boot 4.1.1**.

---

## Architettura

Il diagramma completo è in [`architecture.html`](architecture.html) (apribile in un browser):
mostra componenti, rotte e flusso delle informazioni.

In sintesi:

| Strato | Componente | Tecnologia |
|---|---|---|
| Client | `bard-front` | Angular 21 |
| Ingresso | `api-gateway` | Spring Cloud Gateway, WebFlux |
| Registro | `eureka-service` | Netflix Eureka |
| Servizi | `auth-service`, `ballerini-service`, `presenza-service` | Spring Boot MVC |
| Condiviso | `bard-common` | entità JPA |
| Dati | PostgreSQL | schema `bard` |
| Messagistica | Kafka | configurato, non attivo |
| Osservabilità | OpenTelemetry | OTLP → `:4318` |

---

## Flusso delle informazioni

1. Il **client** (Angular) chiama sempre `api-gateway` su `:8080`; nessun servizio
   è raggiungibile direttamente dall'esterno.
2. Il **gateway** estrae il token `Bearer` e lo verifica con la chiave HMAC condivisa
   (`CHRONOS_JWT_SECRET`). Solo `/api/auth/login` e `/api/auth/register` sono pubblici.
3. Il **gateway** interroga **Eureka** per ottenere l'indirizzo dell'istanza del
   servizio e instrada con `lb://<nome-servizio>`.
4. Il **microservizio** esegue la logica e persiste su **PostgreSQL**.
5. **`ballerini-service`** non possiede i dati anagrafici: per comporre il DTO di un
   ballerino chiama internamente **`auth-service`**
   (`GET /api/auth/internal/users/{id}`).

```
Client ──HTTPS──► api-gateway ──discovery──► eureka-service
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
  auth-service  ballerini-service  presenza-service
        │              │              │
        │              └──HTTP────────┘  (anagrafica)
        ▼              ▼              ▼
        └──────── PostgreSQL (bard) ────┘
```

---

## I moduli

| Modulo | Porta | Ruolo |
|---|---|---|
| `api-gateway` | 8080 | routing, validazione JWT, CORS |
| `eureka-service` | 8761 | service discovery |
| `auth-service` | 7999 | autenticazione, JWT, ruoli, anagrafica |
| `ballerini-service` | 8081 | CRUD ballerini + composizione anagrafica |
| `presenza-service` | 8082 | CRUD presenze, osservabilità |
| `bard-common` | — | entità JPA condivise (`Presenza`, `Sezione`) |

### Rotte del gateway

| Prefisso | Destinazione |
|---|---|
| `/api/auth/**` | `lb://AUTH-SERVICE` |
| `/api/ballerino/**` | `lb://ballerini-service` |
| `/api/presenza/**` | `lb://presenza-service` |

---

## Modello dei dati

Un unico database, schema `bard` (vedi `sql/schema.sql`).

| Tabella | Servizio proprietario | Contenuto |
|---|---|---|
| `auth_users` | auth-service | email, password (BCrypt), nome, cognome, data di nascita |
| `auth_roles` | auth-service | ruoli (`STUDENT` di default) |
| `auth_user_roles` | auth-service | associazione utente ↔ ruolo |
| `ballerini` | ballerini-service | `user_id` (riferimento logico), sezione, età, sesso |
| `presenza` | presenza-service | `presenzaId`, `date`, `ballerino_id` |

**Vincolo architetturale**: `ballerini.user_id` è un **riferimento logico** a
`auth_users.id`, non una foreign key fisica: i due servizi hanno contesti JPA
separati e nessuno dei due deve scrivere nella tabella dell'altro.

---

## Tecnologie

| Area | Tecnologia |
|---|---|
| Linguaggio | Java 21 |
| Framework | Spring Boot 4.1.1 |
| Cloud | Spring Cloud 2025.1.3 (Eureka) |
| Web | Spring WebFlux (gateway), Spring WebMVC (servizi) |
| Sicurezza | Spring Security + JWT (jjwt) |
| Persistenza | Spring Data JPA + Hibernate |
| Database | PostgreSQL |
| Validazione | Bean Validation (Hibernate Validator) |
| Mapping | ModelMapper, Lombok |
| API docs | springdoc-openapi |
| Osservabilità | OpenTelemetry (OTLP) |
| Messagistica | Apache Kafka |
| Build | Maven multi-module |

---

## Uso

### Prerequisiti

- Java 21, Maven 3.9+
- PostgreSQL con lo schema `bard` creato da `sql/schema.sql`

### Avvio

```bash
cd Bard-backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

mvn spring-boot:run -pl eureka-service      # 1. registry
mvn spring-boot:run -pl auth-service        # 2. servizi
mvn spring-boot:run -pl ballerini-service
mvn spring-boot:run -pl presenza-service
mvn spring-boot:run -pl gateway             # 3. ingresso
```

### Configurazione

Variabili d'ambiente (tutte con default in `application.yml` / `application.yaml`):

| Variabile | Default | Usata da |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://192.168.1.22/postgres` | auth, ballerini, presenza |
| `DB_USERNAME` / `DB_PASSWORD` | `bard` / `0000` | auth, ballerini, presenza |
| `EUREKA_URL` | `http://localhost:8761/eureka/` | tutti |
| `CHRONOS_JWT_SECRET` | segreto di sviluppo | auth, gateway |
| `CHRONOS_JWT_EXPIRATION_MINUTES` | `120` | auth |
| `CHRONOS_FRONTEND_ORIGIN` | `http://localhost:4200` | gateway (CORS) |
| `KAFKA_BOOTSTRAP_SERVERS` | `localhost:9092` | auth |

> La `CHRONOS_JWT_SECRET` deve essere identica in `auth-service` e `api-gateway`:
> la prima firma il token, il secondo lo verifica.

---

## Sviluppi futuri

- **Composizione DTO e N+1**: `ballerini-service` oggi fa una chiamata HTTP a
  `auth-service` per ogni ballerino restituito. Piano in
  `piani/2026-11-06_235000-dto-compositi-n-plus-1.md`.
- **Due tipi di presenza**: estensione prevista, da progettare.
- **Eventi Kafka**: il publisher è presente ma disattivato
  (`UserRegisteredEventPublisher` commentato in `AuthController`).
