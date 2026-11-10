package io.bard.learning_service.service;

import io.bard.learning_service.config.AuthServiceClient;
import io.bard.learning_service.config.AuthServiceClient.UserAnagrafica;
import io.bard.learning_service.dto.BallerinoCreate;
import io.bard.learning_service.dto.BallerinoDto;
import io.bard.learning_service.model.Ballerino;
import io.bard.learning_service.repository.BalleriniRepo;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class BallerinoService {
    private final ModelMapper mapper;
    private final BalleriniRepo ballRepo;
    private final AuthServiceClient authServiceClient;

    //TODO aggiunger findById e Update

    BallerinoService(BalleriniRepo repoBallerini, ModelMapper mapper, AuthServiceClient authServiceClient) {
      this.ballRepo = repoBallerini;
      this.mapper = mapper;
      this.authServiceClient = authServiceClient;
    }
    @Transactional
    public BallerinoDto createBallerino(BallerinoCreate create){
        Ballerino ball = mapper.map(create,Ballerino.class);
        return toDto(ballRepo.save(ball));
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

    /** @deprecated non usato: sostituito da {@link #getBallerinoById(UUID)} */
    @Deprecated
    public void getBallerino(){

    }
    @Transactional
    public BallerinoDto deleteBallerino(UUID uuid){
            Ballerino entity = ballRepo.findById(uuid).orElseThrow(
                    EntityNotFoundException::new
            );
            ballRepo.deleteById(entity.getId());

        return toDto(entity);
    }
    public BallerinoDto modificaBallerino(BallerinoDto updateEntity){
    return null;
    }

    /**
     * Compone il DTO con i dati del ballerino piu' l'anagrafica letta da auth-service.
     *
     * @throws ResponseStatusException 400 se lo userId non esiste
     */
    private BallerinoDto toDto(Ballerino ballerino){
        BallerinoDto dto = mapper.map(ballerino, BallerinoDto.class);
        UserAnagrafica anagrafica = authServiceClient.findAnagrafica(ballerino.getUserId());
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
