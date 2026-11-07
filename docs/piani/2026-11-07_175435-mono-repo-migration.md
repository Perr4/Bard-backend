# Piano — Migrazione a mono-repo multi-module

**Data**: 2026-11-07 17:54 UTC · **Stato**: piano da approvare — nessuna azione eseguita
**Richiesta**: una repo per il backend, una per il frontend, archiviare le 6 esistenti

---

## Goal

Raccogliere i 5 microservizi backend in **una sola repository Git** multi-module Maven
(con modulo condiviso `bard-common`), creare una repo separata per il frontend, e
**archiviare le 6 repository esistenti**.

---

## Decisioni

| # | Decisione | Scelta |
|---|---|---|
| **R1** | Nome repo backend | **`Bard-backend`** (Gitea non accetta spazi: "Bard backend" → `Bard-backend`) |
| **R2** | Nome repo frontend | **`bard-front`** (la repo attuale è `Bard-front`: va **rinominata**) |
| **R3** | Storia git | **preservata** per i 5 servizi (via `git filter-repo`) |
| **R4** | Token Gitea | file `/media/myfiles/SSD/Code/BARD/.gitea-token` (da compilare) |
| **R5** | Prerequisito | `git-filter-repo` installato (fatto) |

**Nota**: i nomi repo su Gitea non possono contenere spazi. Se per te "Bard backend" va
scritto diversamente, dimmelo prima di creare la repo.

---

## Current context (verificato)

**6 repo su Gitea** (`http://192.168.1.22:4000/perra.giacomo/`):
- `BARD-auth-service`
- `BARD-Ballerini-service`
- `BARD-presenza-service`
- `BARD-gateway`
- `BARD-eureka-service`
- `Bard-front` (frontend Angular)

**Struttura locale**: `/media/myfiles/SSD/Code/BARD/bard-backend/<servizio>/` con `.git` in
ogni servizio. `BARD/` **non** è un repo git. `sql/` è fuori da ogni repo.

**Versioni disallineate** (opportunità della mono-repo):
- Spring Boot **3.5.0**: auth-service, gateway
- Spring Boot **4.1.1**: ballerini-service, presenza-service
- Java: 21 (auth, presenza) · **25** (ballerini) · da verificare (gateway, eureka)

**Strumenti**: `git filter-repo` disponibile come `python3 -m git_filter_repo`.

---

## Struttura finale

```
BARD-backend/                  ← repo git unica (nuova su Gitea)
├── pom.xml                    ← parent Maven condiviso
├── bard-common/               ← NUOVO modulo: UserAccount condivisa + DTO
├── auth-service/
├── ballerini-service/
├── presenza-service/
├── gateway/
├── eureka-service/
├── sql/                       ← schema.sql (oggi fuori repo)
└── docs/                      ← README, ROADMAP, idee, rapporti (oggi fuori repo)
```

Frontend: repo separata `bard-front` (rinominata da `Bard-front`).

---

## Step-by-step tasks

> Regole: una sola esecuzione se verde; se fallisce, diagnosi e report.
> **NON fare push fino a verifica locale completa.**

### Fase 0 — Prerequisiti
- [ ] **T0.1** L'utente compila `/media/myfiles/SSD/Code/BARD/.gitea-token` con il token.
- [ ] **T0.2** Verificare il token: `curl -s -H "Authorization: token <TOKEN>" http://192.168.1.22:4000/api/v1/user` → 200 con `login: perra.giacomo`.
- [ ] **T0.3** Verificare che `python3 -m git_filter_repo --version` risponda (fatto: `a40bce548d2c`).

### Fase 1 — Creare la repo backend
- [ ] **T1.1** Creare la repo vuota `Bard-backend` su Gitea via API:
  `POST /api/v1/user/repos` con `{"name":"Bard-backend","private":false,"default_branch":"main"}`.
- [ ] **T1.2** Verificare: `GET /api/v1/repos/perra.giacomo/Bard-backend` → 200.

### Fase 2 — Preparare i 5 alberi con storia preservata
Per ogni servizio (`auth-service`, `ballerini-service`, `presenza-service`, `gateway`, `eureka-service`):
- [ ] **T2.1** Clone in `/home/perra/.hermes/cache/scratch/mono/<servizio>`
- [ ] **T2.2** `python3 -m git_filter_repo --to-subdirectory-filter <servizio>`
      → riscrive la storia: tutti i path finiscono sotto `<servizio>/`
- [ ] **T2.3** Verificare: `git -C <clone> log --oneline | wc -l` > 0 (storia conservata)

### Fase 3 — Costruire la mono-repo
- [ ] **T3.1** `mkdir /home/perra/.hermes/cache/scratch/mono/BARD-backend && git init`
- [ ] **T3.2** Per ogni servizio: `git subtree add --prefix=<servizio> <clone> develop -m "merge <servizio>"`
- [ ] **T3.3** Verificare: `git log --oneline` mostra i commit di tutti i servizi; `ls` mostra le 5 cartelle.
- [ ] **T3.4** Spostare `sql/` nella repo (copia da `/media/myfiles/SSD/Code/BARD/bard-backend/sql`).
- [ ] **T3.5** Spostare i docs (`README.md`, `ROADMAP.md`, `idee*.md`, `rapporto*.md`, `progetto*.md`) in `docs/`.
- [ ] **T3.6** Aggiungere `.gitignore` alla root con `target/`, `.idea/`, e il file `.gitea-token`.

### Fase 4 — Modulo condiviso `bard-common`
- [ ] **T4.1** Creare `bard-common/pom.xml` (jar, dipendenze: jakarta.persistence, lombok, spring-boot-starter-validation).
- [ ] **T4.2** Creare `bard-common/src/main/java/.../model/UserAccount.java` (entità condivisa, **sola lettura** per gli altri servizi).
- [ ] **T4.3** Creare `bard-common/src/main/java/.../dto/UserAnagrafica.java` (record condiviso).
- [ ] **T4.4** Aggiungere `bard-common` ai pom dei servizi che lo usano.

### Fase 5 — Parent pom condiviso
- [ ] **T5.1** Creare `pom.xml` root con `<modules>` e `<dependencyManagement>`.
- [ ] **T5.2** Decidere la versione Spring Boot unica (proposta: **4.1.1** per tutti; richiede adattamento di auth/gateway).
- [ ] **T5.3** Decidere la versione Java unica (proposta: **25** per tutti, o 21 se 4.1.1 lo supporta — verificare).
- [ ] **T5.4** Rimuovere le versioni duplicate dai pom dei servizi.

### Fase 6 — Verifica locale
- [ ] **T6.1** `mvn -B test` dalla root con `-pl auth-service` → BUILD SUCCESS
- [ ] **T6.2** Ripetere per tutti i 5 moduli.
- [ ] **T6.3** Verificare che i test totali siano: auth 11 · ballerini 4 · presenza 6 · gateway 1 · eureka 1.

### Fase 7 — Push e archiviazione
- [ ] **T7.1** Aggiungere il remote `origin` → `http://192.168.1.22:4000/perra.giacomo/Bard-backend.git`
- [ ] **T7.2** Push di `main` (o `develop`, decidere) con il token.
- [ ] **T7.3** Verificare su Gitea che la repo contenga tutto.
- [ ] **T7.4** **Archiviare** le 6 repo vecchie via API: `POST /api/v1/repos/perra.giacomo/<repo>/archive` (o dal web).
- [ ] **T7.5** **Rinominare** `Bard-front` → `bard-front` (`POST /api/v1/repos/perra.giacomo/Bard-front` con `name`).

### Fase 8 — Migrazione del codice condiviso (se deciso)
- [ ] **T8.1** Spostare l'entità `UserAccount` in `bard-common`.
- [ ] **T8.2** Aggiornare gli import in auth-service e ballerini-service.
- [ ] **T8.3** Test: tutti verdi.
- [ ] **T8.4** Commit.

---

## Tests / validation

```bash
# Storia preservata
git -C /home/perra/.hermes/cache/scratch/mono/<servizio> log --oneline | head

# Build dalla root
cd /home/perra/.hermes/cache/scratch/mono/BARD-backend
mvn -B test -pl auth-service
mvn -B test -pl ballerini-service
mvn -B test -pl presenza-service
mvn -B test -pl gateway
mvn -B test -pl eureka-service
```
Atteso: `BUILD SUCCESS` per ogni modulo.

**Verifica Gitea**
```bash
curl -s -H "Authorization: token <TOKEN>" http://192.168.1.22:4000/api/v1/repos/perra.giacomo/Bard-backend
curl -s -H "Authorization: token <TOKEN>" http://192.168.1.22:4000/api/v1/repos/perra.giacomo/bard-front
```

---

## Risks, tradeoffs, and open questions

**Rischi**
- **Versioni unificate**: portare auth/gateway a Spring Boot 4.1.1 può rompere cose (hanno già un codice su 3.5.0). Verificare bene prima.
- **Java 25**: se si unifica a 25, auth/presenza/gateway/eureka vanno ricompilati. Verificare la compatibilità.
- **Push prima della verifica**: rischio di pubblicare una rotta rotta. Per questo la verifica locale precede il push.
- **Archiviazione irreversibile**: le 6 repo restano accessibili in sola lettura, ma meglio essere sicuri prima.
- **Token nel file**: il file `.gitea-token` **non** deve finire in un commit (è fuori repo, ma verificare).

**Tradeoff**
- Mono-repo: semplicità di build e modulo condiviso, ma un solo posto per 6 deploy → serve
  disciplina sui branch.

**Domande aperte**
- **R6**: versione Spring Boot unica → **4.1.1** per tutti (richiede adattamento) o **3.5.0** per tutti (retrocessione)?
- **R7**: versione Java unica → **25** o **21**?
- **R8**: branch di default della nuova repo → `main` o `develop`?
- **R9**: i docs (`README`, `ROADMAP`, `idee`, rapporti) → dentro la repo (in `docs/`) o fuori?
- **R10**: la repo frontend `bard-front` → rinominare (mantiene la storia, Gitea fa redirect) o crearne una nuova e copiare?
