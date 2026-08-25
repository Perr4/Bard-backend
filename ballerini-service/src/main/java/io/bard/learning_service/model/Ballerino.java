package io.bard.learning_service.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;
@Entity
@Getter
@Setter
public class Ballerino {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String nome;
    private String cognome;
    private Integer eta;
    private String sesso;

    @OneToMany
    @JoinColumn(name = "id",referencedColumnName = "id")
    private List<Presenza> presenze;
}