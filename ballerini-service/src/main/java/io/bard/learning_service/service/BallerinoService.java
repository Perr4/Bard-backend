package io.bard.learning_service.service;

import io.bard.learning_service.dto.BallerinoCreate;
import io.bard.learning_service.dto.BallerinoDto;
import io.bard.learning_service.model.Ballerino;
import io.bard.learning_service.repository.BalleriniRepo;
import io.bard.learning_service.service.AuthCliente.RegisterResponse;
import io.bard.learning_service.service.AuthCliente.UserAnagrafica;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.UUID;

@Service
public class BallerinoService {
    private final ModelMapper mapper;
    private final BalleriniRepo ballRepo;
    private final AuthCliente authCliente;

    BallerinoService(BalleriniRepo repoBallerini, ModelMapper mapper, AuthCliente authCliente) {
      this.ballRepo = repoBallerini;
      this.mapper = mapper;
      this.authCliente = authCliente;
    }

    /**
     * Crea l'utente in auth-service e il profilo ballerino.
     *
     * <p>Un'unica chiamata dal front: il servizio registra prima l'utente via
     * Feign e poi salva il profilo con lo {@code userId} restituito. Se la
     * registrazione fallisce (email gia' usata, dati non validi) l'errore di
     * auth-service diventa 400.</p>
     */
    @Transactional
    public BallerinoDto createBallerino(BallerinoCreate create){
        RegisterResponse user;
        try {
            user = authCliente.register(create.getUser());
        } catch (FeignException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossibile creare l'utente: " + e.getMessage());
        }
        if (user == null || user.userId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Impossibile creare l'utente");
        }

        Ballerino ball = new Ballerino();
        ball.setUserId(user.userId());
        ball.setEta(etaDa(create.getUser().dataNascita()));
        ball.setSezione(create.getSezione());
        ball.setSesso(create.getSesso());
        return toDto(ballRepo.save(ball));
    }

    /** Eta' ricavata dalla data di nascita; null se la data manca o e' futura. */
    private Integer etaDa(Date dataNascita) {
        if (dataNascita == null) return null;
        LocalDate nascita = dataNascita.toLocalDate();
        int eta = Period.between(nascita, LocalDate.now()).getYears();
        return eta >= 0 ? eta : null;
    }

    public List<BallerinoDto> getAllBallerini(){
        return ballRepo.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * Ballerino identificato dal proprio id, con l'anagrafica letta da auth-service.
     *
     * @throws ResponseStatusException 404 se il ballerino non esiste
     */
    public BallerinoDto getBallerinoById(UUID id){
        Ballerino ballerino = ballRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ballerino non trovato: " + id));
        return toDto(ballerino);
    }

    @Transactional
    public BallerinoDto deleteBallerino(UUID uuid){
            Ballerino entity = ballRepo.findById(uuid).orElseThrow(
                    EntityNotFoundException::new
            );
            ballRepo.deleteById(entity.getId());

        return toDto(entity);
    }
    /**
     * Aggiorna il profilo di un ballerino.
     *
     * <p>Si possono modificare solo i campi del profilo (eta', sezione, sesso):
     * l'anagrafica appartiene a auth-service e l'id non e' modificabile.</p>
     *
     * @throws ResponseStatusException 404 se il ballerino non esiste
     */
    @Transactional
    public BallerinoDto modificaBallerino(BallerinoDto updateEntity){
        Ballerino esistente = ballRepo.findById(updateEntity.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "ballerino non trovato: " + updateEntity.getId()));

        esistente.setEta(updateEntity.getEta());
        esistente.setSezione(updateEntity.getSezione());
        esistente.setSesso(updateEntity.getSesso());

        return toDto(ballRepo.save(esistente));
    }

    /**
     * Compone il DTO con i dati del ballerino piu' l'anagrafica letta da auth-service.
     *
     * @throws ResponseStatusException 400 se lo userId non esiste
     */
    private BallerinoDto toDto(Ballerino ballerino){
        BallerinoDto dto = mapper.map(ballerino, BallerinoDto.class);
        UserAnagrafica anagrafica;
        try {
            anagrafica = authCliente.findAnagrafica(ballerino.getUserId());
        } catch (FeignException.NotFound e) {
            anagrafica = null;
        }
        if (anagrafica == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "userId non esiste: " + ballerino.getUserId());
        }
        dto.setFirstName(anagrafica.firstName());
        dto.setLastName(anagrafica.lastName());
        dto.setDataNascita(anagrafica.dataNascita());
        return dto;
    }
}
