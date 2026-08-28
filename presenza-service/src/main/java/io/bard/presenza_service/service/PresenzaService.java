package io.bard.presenza_service.service;

import io.bard.presenza_service.dto.PresenzaDto;
import io.bard.presenza_service.model.Presenza;

import io.bard.presenza_service.repo.PresenzaRepo;
import jakarta.persistence.EntityNotFoundException;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PresenzaService {
    private final ModelMapper mapper;
    private final PresenzaRepo repo;

    public PresenzaService(PresenzaRepo repo,ModelMapper mapper){
        this.mapper = mapper;
        this.repo = repo;
    }

    @Transactional
    public PresenzaDto createPresenza(PresenzaDto create){
       Presenza ent =  mapper.map(create, Presenza.class);
       repo.save(ent);
        return mapper.map(ent,PresenzaDto.class);
    }
    public List<PresenzaDto> getAllbyBallerinoId(UUID id){
        return repo.findAllByBallerinoId(id).stream().map(item->
                mapper.map(item, PresenzaDto.class)).toList(); }

    public void getBallerino(){

    }
    @Transactional
    public PresenzaDto deletePresenza(UUID uuid){
        Presenza entity = repo.findById(uuid).orElseThrow(
                EntityNotFoundException::new
        );
        repo.deleteById(entity.getId());

        return mapper.map(entity,PresenzaDto.class);
    }
    public PresenzaDto modificaPresenza(PresenzaDto updateEntity){
        return null;
    }



}
