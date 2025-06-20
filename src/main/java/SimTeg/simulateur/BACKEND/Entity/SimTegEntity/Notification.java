package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;

import java.time.LocalDateTime;

@Entity

@Data
public class Notification extends AbstractEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "simulation_id")
    private Simulation simulation;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "dateNotification")
    private LocalDateTime dateNotification;

    @Column(name = "alerte")
    private boolean alerte; // true si dépassement, false sinon

    @Column(name = "annee")
    private int annee;
}
