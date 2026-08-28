package io.bard.presenza_service.controller;

import io.bard.presenza_service.dto.PresenzaDto;
import io.bard.presenza_service.service.PresenzaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api")
public class PresenzaController {
    private final PresenzaService service;

    public PresenzaController(PresenzaService service){
        this.service = service;
    }

    //AGGIUNGERE VALIDAZIONE

    //CRUD

    //C *
    @PostMapping("/presenza")

    public ResponseEntity<PresenzaDto> saveNewPresenza(@RequestBody PresenzaDto create){
        System.out.println(create);
        return  ResponseEntity.ok(service.createPresenza(create));
    }

    //R *
    @GetMapping("/presenza/{id}")
    public ResponseEntity<List<PresenzaDto>> getAllbyBallerinoId(@PathVariable UUID id){
        return ResponseEntity.ok(service.getAllbyBallerinoId(id));
    }

    //U
    @PutMapping("/presenza")
    public ResponseEntity<PresenzaDto> updatePresenza(@RequestBody PresenzaDto updateEntity) {
        return ResponseEntity.ok(service.modificaPresenza(updateEntity));
    }

    //D *
    @DeleteMapping("/presenza/{id}")
    public ResponseEntity<PresenzaDto> deletePresenza(@PathVariable("id") UUID uuid){
        return ResponseEntity.ok(service.deletePresenza(uuid));
    }

}
