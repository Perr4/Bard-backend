# BARD — Backend a microservizi

Backend della piattaforma **BARD**, organizzato come insieme di microservizi Spring Boot
che comunicano attraverso un API Gateway e si registrano su un server Eureka.

## Architettura

```
                 ┌──────────────┐
   Client  ───►  │ api-gateway  │  (Spring Cloud Gateway, WebFlux, porta 8080)
                 └──────┬───────┘
                        │  service discovery (Eureka, porta 8761)
        ┌───────────────┼────────────────┐
        ▼               ▼                ▼
 ┌────────────┐  ┌──────────────┐  ┌───────────────┐
 │ auth-service│  │ presenza-svc │  │ ballerini-svc │
 │  (7999)     │  │   (8082)     │  │    (8081)     │
 └──────┬──────┘  └──────┬───────┘  └───────┬───────┘
        └────────────────┼──────────────────┘
                         ▼
                  ┌─────────────┐
                  │ PostgreSQL  │  (schema `bard`)
                  └─────────────┘
```

## Servizi

### auth-service — autenticazione
Responsabile dell'autenticazione: riceve email e password, valida le credenziali e
**crea il token JWT**. Espone le operazioni di registrazione, login e profilo utente.

- Porta: `7999`
- Endpoint: `/api/auth/register`, `/api/auth/login`, `/api/auth/me`
- Sicurezza: Spring Security + JWT (jjwt), password codificate con BCrypt
- Ruoli: `STUDENT` (default), gestiti su tabella dedicata

### ballerini-service — utenti dell'applicazione
Responsabile dei **ballerini**, cioè gli utenti generali dell'intera piattaforma.
È il servizio che gestisce le anagrafiche dei destinatari delle attività.

- Porta: `8081`
- Stato: struttura base (config, sicurezza, OpenAPI); controller/service/repository da implementare

### presenza-service — presenze
Contiene **tutte le funzioni relative alle presenze** (registrazione, aggiornamento,
lettura per ballerino, cancellazione).

- Porta: `8082`
- Endpoint: `/api/presenza` (POST, PUT, GET `/{id}`, DELETE `/{id}`)
- Entità: `Presenza` (`presenzaId`, `data`, `ballerino_id`)

### api-gateway — ingresso unico
Punto di ingresso di tutte le richieste esterne: instrada verso i microservizi,
applica la validazione del token JWT e gestisce il CORS.

- Porta: `8080`
- Rotte: `/api/auth/**`, `/api/ballerino/**`, `/api/presenza/**`

### eureka-service — service discovery
Server Eureka: tiene traccia dei microservizi attivi e permette il routing
basato sul nome del servizio (`lb://<nome-servizio>`).

- Porta: `8761`

## Stack tecnologico

| Area | Tecnologia |
|---|---|
| Linguaggio | Java 21 |
| Framework | Spring Boot (4.1.1 per presenza/ballerini, 3.5.0 per auth/gateway) |
| Web | Spring WebMVC (servizi), Spring WebFlux (gateway) |
| Persistenza | Spring Data JPA + Hibernate (`ddl-auto: validate`) |
| Database | PostgreSQL, schema `bard` |
| Service discovery | Netflix Eureka |
| Sicurezza | Spring Security, JWT |
| Validazione | Bean Validation (Hibernate Validator) |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven |
| Frontend | Angular (`BARD-Front`) |

## Metodologia di sviluppo

- **TDD (Test Driven Design)**: ogni feature viene verificata testando se la modifica
  rompe o meno il programma. Il test di riferimento è un **context load** che verifica
  che l'applicazione si avvia correttamente; ulteriori tipi di test si valuteranno successivamente.
- Architettura a layer: `controller → service → repository` (le entità non compaiono nel controller).
- DTO separati per input e output; validazione Bean Validation in entrata.
- `@Transactional` sui metodi di scrittura, mai a livello di classe.
- Schema SQL manuale con vincoli espliciti (`NOT NULL` / `UNIQUE` / `CHECK`).
- Log su stdout con operazione, id e codice.

## Struttura del repository

```
BARD/
├── BARD-Front/          Frontend Angular
├── bard-backend/        Backend a microservizi
│   ├── auth-service/
│   ├── ballerini-service/
│   ├── presenza-service/
│   ├── gateway/
│   ├── eureka-service/
│   └── sql/             Schema database (schema.sql)
├── strumenti/           Strumenti di supporto (Observability, apiCall)
├── graphify-out/        Grafo del codice (graphify)
├── AGENTS.md            Regole di progetto
└── ROADMAP.md           Roadmap del progetto
```

## Sviluppi futuri

- **Ampliamento delle presenze**: l'idea è di introdurre **due tipi di presenza**.
  Questa è un'estensione prevista per il futuro, da progettare e implementare in un secondo momento.
