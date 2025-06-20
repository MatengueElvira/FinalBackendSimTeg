package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import jakarta.persistence.Entity;


import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class HistoriqueTauxUsure {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private Integer tauxUsureId;

    private double ancienTauxUsure;
    private double nouveauTauxUsure;

    private double ancienSeuil;
    private double nouveauSeuil;

    private double ancienneAnnee;
    private double nouvelleAnnee;

    private LocalDateTime dateModification;

    private String typeChangement; //  "MISE À JOUR"
}
