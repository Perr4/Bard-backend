# Piano — entità di lettura "presenza con persona"

## Obiettivo

Il backend rilascia un'entita' di lettura che unisce la presenza ai dati anagrafici
della persona, cosi' che il frontend la visualizzi con una sola chiamata.

**Nessuna nuova tabella**: le entita' restano separate (`presenza`, `auth_users`,
`ballerini`). Quello che si aggiunge e' un DTO di composizione.

---

## Stato attuale

| Cosa | Stato |
|---|---|
| `GET /api/presenza/{ballerinoId}` — presenze di un ballerino | ✅ |
| `POST/PUT/DELETE /api/presenza` | ✅ |
| Entita' `Presenza` condivisa (`bard-common`) | ✅ |
| `GET /api/presenza` — tutte le presenze | ❌ (405) |
| DTO presenza + anagrafica | ❌ |
| Composizione batch (niente N+1) | ❌ |

---

## Passo 1 — `GET /api/presenza` (tutte le presenze)

**File**: `presenza-service/src/main/java/io/bard/presenza_service/repo/PresenzaRepo.java`

Aggiungere:
```java
List<Presenza> findAll();
```

**File**: `presenza-service/src/main/java/io/bard/presenza_service/service/PresenzaService.java`

```java
public List<PresenzaDto> getAll() {
    return repo.findAll().stream()
            .map(item -> mapper.map(item, PresenzaDto.class))
            .toList();
}
```

**File**: `presenza-service/src/main/java/io/bard/presenza_service/controller/PresenzaController.java`

```java
@Operation(summary = "Get all presenze")
@GetMapping("/presenza")
public ResponseEntity<List<PresenzaDto>> getAll() {
    return ResponseEntity.ok(service.getAll());
}
```

**Verifica**:
```bash
curl -H "Authorization: Bearer $TOKEN" http://localhost:8080/api/presenza
# atteso: 200 + array (vuoto se non ci sono presenze)
```

---

## Passo 2 — DTO di composizione

**File nuovo**: `presenza-service/src/main/java/io/bard/presenza_service/dto/PresenzaConPersonaDto.java`

```java
package io.bard.presenza_service.dto;

import io.bard.common.model.Sezione;

import java.sql.Date;
import java.util.UUID;

/**
 * Entita' di lettura: una presenza con i dati anagrafici della persona.
 *
 * <p>L'anagrafica arriva da auth-service; il profilo ballerino da ballerini-service.
 * Il DTO esiste solo in lettura: non e' una tabella.</p>
 */
public record PresenzaConPersonaDto(
        /** ID della presenza. */
        UUID presenzaId,
        /** Data della presenza. */
        Date data,
        /** ID del ballerino. */
        UUID ballerinoId,
        /** Nome della persona (da auth_users). */
        String firstName,
        /** Cognome della persona (da auth_users). */
        String lastName,
        /** Eta' del ballerino (da ballerini). */
        Integer eta,
        /** Sezione del ballerino (da ballerini). */
        Sezione sezione) {
}
```

---

## Passo 3 — endpoint batch in auth-service (evita il N+1)

**File**: `auth-service/src/main/java/it/chronos/auth/controller/AuthController.java`

```java
/**
 * Anagrafiche di piu' utenti in una sola chiamata (uso interno).
 */
@PostMapping("/internal/users/batch")
public List<UserAnagrafica> getAnagrafiche(@RequestBody List<UUID> ids) {
    return ids.stream()
            .map(id -> userRepository.findById(id)
                    .map(u -> new UserAnagrafica(u.getId(), u.getFirstName(),
                            u.getLastName(), u.getDataNascita()))
                    .orElse(null))
            .filter(java.util.Objects::nonNull)
            .toList();
}
```

**Verifica**: `POST /api/auth/internal/users/batch` con `["uuid1","uuid2"]` → lista di anagrafiche.

---

## Passo 4 — client HTTP e composizione in presenza-service

**File nuovo**: `presenza-service/src/main/java/io/bard/presenza_service/config/AuthServiceClient.java`

```java
@Component
public class AuthServiceClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AuthServiceClient(RestTemplate restTemplate,
            @Value("${auth.service.url:http://localhost:7999}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public Map<UUID, UserAnagrafica> findAnagrafiche(List<UUID> ids) {
        UserAnagrafica[] response = restTemplate.postForObject(
                baseUrl + "/api/auth/internal/users/batch", ids, UserAnagrafica[].class);
        return response == null ? Map.of() :
                Arrays.stream(response)
                        .collect(Collectors.toMap(UserAnagrafica::userId, a -> a));
    }

    public record UserAnagrafica(UUID userId, String firstName, String lastName, Date dataNascita) {}
}
```

**File**: `presenza-service/src/main/java/io/bard/presenza_service/service/PresenzaService.java`

```java
public List<PresenzaConPersonaDto> getAllConPersona() {
    List<Presenza> presenze = repo.findAll();
    List<UUID> userIds = presenze.stream()
            .map(Presenza::getBallerinoId).distinct().toList();
    Map<UUID, UserAnagrafica> anagrafiche = authServiceClient.findAnagrafiche(userIds);

    return presenze.stream().map(p -> {
        UserAnagrafica a = anagrafiche.get(p.getBallerinoId());
        return new PresenzaConPersonaDto(
                p.getId(), p.getDate(), p.getBallerinoId(),
                a == null ? null : a.firstName(),
                a == null ? null : a.lastName(),
                null, null);   // eta/sezione: richiedono ballerini-service (passo 5)
    }).toList();
}
```

**File**: `presenza-service/src/main/java/io/bard/presenza_service/controller/PresenzaController.java`

```java
@Operation(summary = "Get all presenze with person details")
@GetMapping("/presenza/con-persona")
public ResponseEntity<List<PresenzaConPersonaDto>> getAllConPersona() {
    return ResponseEntity.ok(service.getAllConPersona());
}
```

**Verifica**:
```bash
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/presenza/con-persona
# atteso: 200 + presenze con firstName/lastName
```

---

## Passo 5 (opzionale) — aggiungere eta' e sezione

Per popolare `eta` e `sezione` serve un altro endpoint batch in `ballerini-service`:

```
POST /api/ballerino/internal/batch  →  lista di BallerinoDto
```

Stessa logica del passo 3. Da fare solo se il frontend deve mostrare eta'/sezione
accanto alla presenza.

---

## Passo 6 — test

Aggiungere in `presenza-service/src/test/java/.../PresenzaServiceTest.java`:

- `getAll_conPresenzeRestituisceLista`
- `getAllConPersona_conAnagraficaPresenteComponeIlDto`
- `getAllConPersona_utenteMancanteLasciaNomeNullo`

Mockare `AuthServiceClient` con Mockito.

---

## Ordine di esecuzione

1. Passo 1 (`GET /api/presenza`) — autonomo, verificabile subito
2. Passo 3 (batch auth-service) — prerequisito del 4
3. Passo 2 (DTO)
4. Passo 4 (client + composizione)
5. Test
6. Passo 5 solo se serve

---

## Cose da decidere prima

1. **`DELETE /api/presenza/{id}`**: oggi l'id e' quello della presenza, ma il frontend
   passa un `ballerino_id`. Da allineare.
2. **Autenticazione**: `/api/presenza/**` e' gia' protetto dal gateway. L'endpoint
   `/internal/**` di auth-service resta aperto (non esposto dal gateway).
3. **`eta`/`sezione`**: servono subito o basta nome e cognome?
