package io.bard.learning_service.service;

import io.bard.learning_service.dto.BallerinoCreate;
import io.bard.learning_service.dto.BallerinoDto;
import io.bard.learning_service.model.Ballerino;
import io.bard.learning_service.repository.BalleriniRepo;
import io.bard.learning_service.repository.PresenzaRepo;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class BallerinoService {
    private final ModelMapper mapper;
    private final BalleriniRepo ballRepo;


    //TODO aggiunger findById e Update

    BallerinoService(BalleriniRepo repoBallerini,ModelMapper mapper){
      this.ballRepo = repoBallerini;
      this.mapper = mapper;
    }
    @Transactional
    public BallerinoDto createBallerino(BallerinoCreate create){
        Ballerino ball = mapper.map(create,Ballerino.class);
        return mapper.map( ballRepo.save(ball),BallerinoDto.class);
    }
    public List<BallerinoDto> getAllBallerini(){
        return ballRepo.findAll()
                .stream()
                .map(item->
                        mapper.map(item,BallerinoDto.class))
                .toList();
    }
    public void getBallerino(){

    }
    @Transactional
    public BallerinoDto deleteBallerino(UUID uuid){
            Ballerino entity = ballRepo.findById(uuid).orElseThrow(
                    EntityNotFoundException::new
            );
            ballRepo.deleteById(entity.getId());

        return mapper.map(entity,BallerinoDto.class);
    }
    public BallerinoDto modificaBallerino(BallerinoDto updateEntity){
    return null;
    }
}
