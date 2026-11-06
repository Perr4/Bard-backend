package io.bard.learning_service.controller;

import io.bard.learning_service.dto.BallerinoCreate;
import io.bard.learning_service.dto.BallerinoDto;
import io.bard.learning_service.model.Ballerino;
import io.bard.learning_service.service.BallerinoService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
public class BallerinoController {

    private final BallerinoService service;

    BallerinoController(BallerinoService serv){
        this.service = serv;
    }

    //AGGIUNGERE VALIDAZIONE

    //CRUD

    //C *
    @PostMapping("/ballerino")
    public ResponseEntity<BallerinoDto> saveNewBallerino(@RequestBody BallerinoCreate create){
          System.out.println(create);
        return  ResponseEntity.ok(service.createBallerino(create));
    }

    //R *
    @GetMapping("/ballerino")
    public ResponseEntity<List<BallerinoDto>> getAllBallerino(){
        return ResponseEntity.ok(service.getAllBallerini());
    }

    //U
    @PutMapping("/ballerino")
    public ResponseEntity<BallerinoDto> updateBallerino(@RequestBody BallerinoDto updateEntity) {
         return ResponseEntity.ok(service.modificaBallerino(updateEntity));
    }

    //D *
    @DeleteMapping("/ballerino/{id}")
    public ResponseEntity<BallerinoDto> deleteBallerino(@PathVariable("id") UUID uuid){
        return ResponseEntity.ok(service.deleteBallerino(uuid));
    }

}
