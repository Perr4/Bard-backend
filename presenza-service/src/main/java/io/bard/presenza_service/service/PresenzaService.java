package io.bard.presenza_service.service;

import io.bard.presenza_service.dto.PresenzaDto;
import io.bard.common.model.Presenza;

import io.bard.presenza_service.repo.PresenzaRepo;
import jakarta.persistence.EntityNotFoundException;
import org.aspectj.asm.IModelFilter;
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

    /** Tutte le presenze, per i report e la classifica della dashboard. */
    public List<PresenzaDto> getAllPresenze() {
        return repo.findAll().stream()
                .map(item -> mapper.map(item, PresenzaDto.class))
                .toList();
    }


    @Transactional
    public PresenzaDto deletePresenza(UUID uuid){
        Presenza entity = repo.findById(uuid).orElseThrow(
                EntityNotFoundException::new);
        repo.deleteById(uuid);
        return mapper.map(entity,PresenzaDto.class);
    }
    @Transactional
    public PresenzaDto modificaPresenza(PresenzaDto updateEntity){
        Presenza entity = mapper.map(updateEntity, Presenza.class);
        return mapper.map(repo.save(entity), PresenzaDto.class);
    }



}
