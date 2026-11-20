# BARD — Backend

Backend della piattaforma **BARD** (CHRONOS): autenticazione, anagrafica ballerini e
gestione delle presenze, orchestrato da un API Gateway e un service registry.

**Mono-repo Maven multi-module** — **Java 21** · **Spring Boot 4.1.1** · **PostgreSQL**.

---

## Indice

- [Architettura](#architettura)
- [Moduli](#moduli)
- [Rotte ed endpoint](#rotte-ed-endpoint)
- [Tecnologie](#tecnologie)
- [Requisiti](#requisiti)
- [Avvio rapido (Docker Compose)](#avvio-rapido-docker-compose)
- [Avvio manuale (Maven)](#avvio-manuale-maven)
- [Variabili d'ambiente](#variabili-dambiente)
- [Test](#test)
- [Osservabilità](#osservabilità)
- [Sicurezza](#sicurezza)
- [Struttura del repository](#struttura-del-repository)

---

## Architettura

```
                     ┌─────────────────┐
                     │   bard-front    │  Angular 21 (:4200)
                     └────────┬────────┘
                              │ HTTP / JSON
                              ▼
                     ┌─────────────────┐
                     │   api-gateway   │  Spring Cloud Gateway · WebFlux (:8080)
                     │  ─────────────  │
                     │ • valida JWT    │
                     │ • routing lb:// │◄──────────────┐ service discovery
                     │ • CORS          │               │
                     └────────┬────────┘               │
               ┌──────────────┼──────────────┐         │
               ▼              ▼              ▼         │
        ┌────────────┐ ┌────────────┐ ┌────────────┐   │
        │   auth     │ │ ballerini  │ │  presenza  │   │
        │  (:7999)   │ │  (:8081)   │ │  (:8082)   │   │
        └─────┬──────┘ └─────┬──────┘ └─────┬──────┘   │
              │              │              │          │
              │  anagrafica  │              │          │
              │◄─────────────┘              │          │
              ▼                              ▼          │
        ┌──────────────────────────────────────────┐   │
        │        PostgreSQL · schema `bard`         │   │
        │  auth_users · auth_roles · ballerini · presenza │
        └──────────────────────────────────────────┘   │
                              ▲                        │
                              │ registrazione         │
                     ┌────────┴────────┐               │
                     │ eureka-service  │  Netflix Eureka (:8761)
                     └─────────────────┘               │
                                                       │
   tutti i servizi ──OTLP──► otel-collector ──┬────────┘
                                             ▼
                     ┌──────────────────────────────────────────┐
                     │ Prometheus (:9090) · Tempo (:3200)        │
                     │ Loki (:3100) · Grafana (:3001)            │
                     └──────────────────────────────────────────┘
```

### Come funziona

1. **Ingresso unico** — il client parla solo con `api-gateway` (8080).
2. **Autenticazione** — il gateway verifica il `Bearer` token con la stessa chiave HMAC
   usata da `auth-service` (`CHRONOS_JWT_SECRET`). Login e registrazione sono pubblici.
3. **Service discovery** — il gateway chiede a `eureka-service` l'istanza attiva e
   instrada con `lb://<nome-servizio>`.
4. **Persistenza** — i microservizi scrivono su PostgreSQL (schema `bard`).
5. **Composizione** — `ballerini-service` completa il profilo chiamando internamente
   `auth-service` (`/api/auth/internal/users/{id}`).

---

## Moduli

| Modulo | Ruolo | Porta | Responsabilità |
|---|---|---|---|
| `gateway` | Ingresso | 8080 | Routing, validazione JWT, CORS |
| `eureka-service` | Registro | 8761 | Service discovery (`lb://`) |
| `auth-service` | Identità | 7999 | Registrazione, login, JWT, ruoli, anagrafica |
| `ballerini-service` | Profilo | 8081 | CRUD ballerini + composizione anagrafica |
| `presenza-service` | Presenze | 8082 | CRUD presenze |
| `bard-common` | Condiviso | — | Entità JPA usate da più servizi |

---

## Rotte ed endpoint

### Rotte esposte dal gateway

| Prefisso | Destinazione |
|---|---|
| `/api/auth/**` | `lb://AUTH-SERVICE` |
| `/api/ballerino/**` | `lb://ballerini-service` |
| `/api/presenza/**` | `lb://presenza-service` |

### Endpoint

| Servizio | Metodo | Path | Descrizione |
|---|---|---|---|
| auth | POST | `/api/auth/register` | Registrazione (pubblico) |
| auth | POST | `/api/auth/login` | Login → JWT (pubblico) |
| auth | POST | `/api/auth/token` | Introspezione token (pubblico) |
| auth | GET | `/api/auth/me` | Profilo dell'utente corrente |
| auth | GET | `/api/auth/internal/users/{id}` | Anagrafica (solo rete interna) |
| ballerini | POST | `/api/ballerino` | Crea ballerino |
| ballerini | GET | `/api/ballerino` | Lista ballerini |
| ballerini | GET | `/api/ballerino/{id}` | Dettaglio ballerino |
| ballerini | PUT | `/api/ballerino` | Aggiorna ballerino |
| ballerini | DELETE | `/api/ballerino/{id}` | Elimina ballerino |
| presenza | POST | `/api/presenza` | Registra presenza |
| presenza | PUT | `/api/presenza` | Aggiorna presenza |
| presenza | GET | `/api/presenza` | Tutte le presenze |
| presenza | GET | `/api/presenza/{id}` | Presenze di un ballerino |
| presenza | DELETE | `/api/presenza/{id}` | Elimina presenza |

---

## Tecnologie

| Area | Tecnologia | Dove |
|---|---|---|
| Linguaggio | **Java 21** | tutti |
| Framework | **Spring Boot 4.1.1** | tutti |
| Cloud | **Spring Cloud** (Eureka, Gateway) | gateway, eureka, client |
| Web | **Spring WebFlux** / **WebMVC** | gateway / auth, ballerini, presenza |
| Service discovery | **Netflix Eureka** | eureka-service |
| Sicurezza | **Spring Security + JWT** (jjwt) | gateway, auth |
| Persistenza | **Spring Data JPA + Hibernate** | auth, ballerini, presenza |
| Database | **PostgreSQL** (schema `bard`) | servizi dati |
| Validazione | **Bean Validation** | DTO in ingresso |
| Mapping | **ModelMapper**, **Lombok** | ballerini, entità |
| API docs | **springdoc-openapi** (Swagger UI) | servizi |
| Osservabilità | **OpenTelemetry** (OTLP) + Collector | tutti |
| Build | **Maven** multi-module | mono-repo |

---

## Requisiti

- **Java 21** (`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`)
- **Maven** 3.9+
- **Docker** + **Docker Compose** (per l'avvio completo)
- **PostgreSQL** raggiungibile, con lo schema `bard`

> ⚠️ **Script SQL**: `docker-compose.yml` monta `sql/schema.sql` e
> `sql/init/00-create-schema.sql`. Questa cartella **non è inclusa nel repository**
> (dati sensibili). Chi clona il repo deve ricreare gli script prima di avviare.

---

## Avvio rapido (Docker Compose)

```bash
cd Bard-backend
docker compose up --build
```

Ordine di avvio garantito dagli healthcheck:
`postgres → eureka-service → auth-service → ballerini/presenza-service → gateway`.

Porte esposte sull'host:

| Servizio | Porta |
|---|---|
| api-gateway | 8080 |
| pg-view | 8083 |
| OTLP (ingresso) | 4317 / 4318 |
| Prometheus | 9090 |
| Loki | 3100 |
| Tempo | 3200 |
| Grafana | 3001 |

Comandi:

```bash
docker compose down        # ferma (conserva i volumi)
docker compose down -v     # ferma e cancella i volumi
docker compose ps          # stato dei servizi
```

---

## Avvio manuale (Maven)

```bash
cd Bard-backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64

# 1. il registry parte per primo
mvn spring-boot:run -pl eureka-service

# 2. i servizi applicativi (terminali separati)
mvn spring-boot:run -pl auth-service
mvn spring-boot:run -pl ballerini-service
mvn spring-boot:run -pl presenza-service

# 3. il gateway per ultimo
mvn spring-boot:run -pl gateway
```

---

## Variabili d'ambiente

Elenco completo nel file **`.env`** (nomi delle variabili, senza valori).

| Variabile | Usata da |
|---|---|
| `DB_URL` | auth, ballerini, presenza |
| `DB_USERNAME` | auth, ballerini, presenza |
| `DB_PASSWORD` | auth, ballerini, presenza |
| `EUREKA_URL` | tutti i servizi |
| `AUTH_SERVICE_URL` | ballerini-service |
| `CHRONOS_JWT_SECRET` | auth-service, gateway |
| `CHRONOS_JWT_EXPIRATION_MINUTES` | auth-service |
| `CHRONOS_FRONTEND_ORIGIN` | gateway (CORS) |
| `OTEL_EXPORTER_OTLP_ENDPOINT` | tutti i servizi |
| `OTEL_EXPORTER_OTLP_PROTOCOL` | tutti i servizi |
| `OTEL_METRICS_EXPORTER` | tutti i servizi |
| `OTEL_TRACES_EXPORTER` | tutti i servizi |
| `OTEL_LOGS_EXPORTER` | tutti i servizi |
| `OTEL_SERVICE_NAME` | tutti i servizi |
| `OTEL_RESOURCE_ATTRIBUTES` | tutti i servizi |
| `LOKI_URL` | presenza-service |
| `PGVIEW_USERNAME` / `PGVIEW_PASSWORD` / `PGVIEW_SESSION_SECRET` | pg-view |
| `GRAFANA_ADMIN_USER` / `GRAFANA_ADMIN_PASSWORD` | Grafana |

> La stessa `CHRONOS_JWT_SECRET` **deve** essere condivisa tra `auth-service` (che firma)
> e `api-gateway` (che verifica): se differisce, ogni token viene rifiutato con 401.

---

## Test

```bash
cd Bard-backend
mvn -B test                 # tutti i moduli
mvn -B test -pl auth-service
```

Ogni endpoint nuovo o modificato va verificato con **tre chiamate**:
**senza token → 401**, **token falso → 401**, **token valido → 200**.

---

## Osservabilità

Tutti i microservizi includono l'**agente OpenTelemetry Java** e inviano metriche,
tracce e log via OTLP al collector, che li smista:

| Servizio | Ruolo | Porta |
|---|---|---|
| `otel-collector` | riceve OTLP, espone metriche Prometheus | 4317/4318 → 8889 |
| `prometheus` | metriche | 9090 |
| `tempo` | tracce | 3200 |
| `loki` | log | 3100 |
| `grafana` | dashboard | 3001 |

La dashboard **BARD — Overview** è provisionata e mostra, per microservizio:
richieste HTTP, errori 5xx, heap e CPU JVM, oltre a log e telemetria globale.

---

## Sicurezza

- **JWT HMAC (HS512)**, secreto condiviso `CHRONOS_JWT_SECRET`.
- **Ruoli**: `ADMIN`, `BALLERINO` (RBAC).
- Protezione al gateway; i microservizi sono aperti solo sulla rete interna.
- I valori di default in `docker-compose.yml` sono **solo per sviluppo**:
  in produzione cambiare `CHRONOS_JWT_SECRET`, `DB_PASSWORD`, `PGVIEW_PASSWORD`
  e le credenziali di Grafana.

---

## Struttura del repository

```
Bard-backend/
├── bard-common/        # entità JPA condivise
├── auth-service/       # identità, JWT, ruoli
├── ballerini-service/  # profilo ballerini
├── presenza-service/   # presenze
├── gateway/            # API gateway (WebFlux)
├── eureka-service/     # service registry
├── docker-compose.yml  # ambiente completo
├── sql/                # NON incluso nel repo (dati sensibili)
└── strumenti/          # NON incluso (config locale)
```
