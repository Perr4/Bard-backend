package io.bard.learning_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Ballerino {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ballerino_id")
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    private Integer eta;

    @Enumerated(EnumType.STRING)
    private Sezione sezione;
    private String sesso;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "ballerino_id",referencedColumnName = "ballerino_id")
    private List<Presenza> presenze;
}
