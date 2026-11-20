package io.bard.uscite_service.service;

import io.bard.uscite_service.dto.PartecipazioneDto;
import io.bard.uscite_service.dto.UscitaDto;
import io.bard.uscite_service.model.Numero;
import io.bard.uscite_service.model.Partecipazione;
import io.bard.uscite_service.model.Uscita;
import io.bard.uscite_service.repo.UscitaRepo;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class UscitaService {

    private final UscitaRepo repo;

    public UscitaService(UscitaRepo repo) {
        this.repo = repo;
    }

    /** Tutte le uscite. */
    public List<UscitaDto> getAll() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    /** Le 5 uscite con piu' partecipanti, per la dashboard. */
    public List<UscitaDto> getTop5() {
        return repo.findAll().stream()
                .sorted(Comparator.comparingInt((Uscita u) -> u.getPartecipazioni().size()).reversed())
                .limit(5)
                .map(this::toDto)
                .toList();
    }

    /**
     * Uscita singola.
     *
     * @throws ResponseStatusException 404 se non esiste
     */
    public UscitaDto getById(UUID id) {
        return toDto(repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "uscita non trovata: " + id)));
    }

    /** Crea un'uscita con i partecipanti indicati. */
    @Transactional
    public UscitaDto create(UscitaDto dto) {
        return toDto(repo.save(toEntity(dto)));
    }

    /**
     * «Tutti i ballerini escono assieme»: crea l'uscita e iscrive tutti i ballerini,
     * ciascuno con tutti i numeri.
     */
    @Transactional
    public UscitaDto createConTuttiIPartner(UscitaDto dto) {
        Uscita uscita = toEntity(dto);
        for (UUID ballerinoId : repo.findAllBallerinoIds()) {
            for (Numero numero : Numero.values()) {
                uscita.getPartecipazioni().add(new Partecipazione(ballerinoId, numero));
            }
        }
        return toDto(repo.save(uscita));
    }

    /**
     * Elimina un'uscita.
     *
     * @throws ResponseStatusException 404 se non esiste
     */
    @Transactional
    public void delete(UUID id) {
        Uscita uscita = repo.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "uscita non trovata: " + id));
        repo.delete(uscita);
    }

    private Uscita toEntity(UscitaDto dto) {
        Uscita uscita = new Uscita();
        uscita.setData(dto.getData());
        uscita.setLuogo(dto.getLuogo());
        uscita.setTipologia(dto.getTipologia());
        if (dto.getPartecipazioni() != null) {
            uscita.setPartecipazioni(dto.getPartecipazioni().stream()
                    .map(p -> new Partecipazione(p.getBallerinoId(), p.getNumero()))
                    .toList());
        }
        return uscita;
    }

    private UscitaDto toDto(Uscita uscita) {
        UscitaDto dto = new UscitaDto();
        dto.setId(uscita.getId());
        dto.setData(uscita.getData());
        dto.setLuogo(uscita.getLuogo());
        dto.setTipologia(uscita.getTipologia());
        dto.setPartecipazioni(uscita.getPartecipazioni().stream()
                .map(p -> {
                    PartecipazioneDto pd = new PartecipazioneDto();
                    pd.setBallerinoId(p.getBallerinoId());
                    pd.setNumero(p.getNumero());
                    return pd;
                })
                .toList());
        return dto;
    }
}
