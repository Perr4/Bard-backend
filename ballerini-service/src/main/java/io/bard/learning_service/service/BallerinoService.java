package io.bard.learning_service.service;

import io.bard.learning_service.dto.BallerinoCreate;
import io.bard.learning_service.repository.BalleriniRepo;
import io.bard.learning_service.repository.PresenzaRepo;
import org.springframework.stereotype.Service;

@Service
public class BallerinoService {

    private final BalleriniRepo repoBallerini;
    private final PresenzaRepo repoPresenza;

    BallerinoService(BalleriniRepo repoBallerini,PresenzaRepo repoPresenza){
      this.repoBallerini = repoBallerini;
      this.repoPresenza = repoPresenza;
    }

    public void createBallerino(BallerinoCreate create){

    }
    public void getBallerino(){

    }
    public void getAllBallerini(){

    }
    public void deleteBallerino(){

    }
    public void modificaBallerino(){

    }
}
