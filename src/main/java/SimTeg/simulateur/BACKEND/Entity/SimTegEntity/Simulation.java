package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;
import SimTeg.simulateur.BACKEND.Dto.DataType.Assurance;
import SimTeg.simulateur.BACKEND.Dto.DataType.Frais;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;
import SimTeg.simulateur.BACKEND.User.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

//JPA =Java Persistance Api et @Entity est une annotation
@Entity
@ToString(exclude={"CategorieCredit"})
@Data
public class Simulation  extends AbstractEntity {
  @Column(name = "teg")
  private double teg;//taux effectif globlal resultat principale
  @Column(name="echeance")
  private double echeance;
  @Column(name="coutTotal")
  private double coutTotal;
  @Column(name="tableauAmortisement")
  @ElementCollection
  private List<AmortisementLigne> tableauAmortisement;
  @Column(name="tableauAmortisement")
  @ElementCollection
  private List<DeblocageLigne> TableauDeblocages;
  @Column(name="montant")
  private double montant;
  @Column
  private LocalDate DatePremiereEcheance;
  @Column(name="dateDebut")
  private LocalDate dateDebut;
  @Column(name="tauxNominal")
  private double tauxNominal;
  @Column(name="fraisJson" )
  @ElementCollection// car frais est un dataType
  private List<Frais> fraisJson;
  @Column(name="assuranceJson")
  @ElementCollection
  private List<Assurance> assuranceJson;
  @Column(name="dateFin")
  private LocalDate datefin;
  @Column(name="duree")
  private double duree;
  @Column(nullable = true)
  private TypeEprunteur typeEmprunteur;
  @Column(name="frequence")
  private Frequence frequence;//fixe pour les mensualités fixe ,autre pour ammortisement fixe mes ici nous allons d'abord nous appesentire sur les pret amortisable avec interet composé soit des mensualité fixes
// relation plusieur simulation peuvent appartenir à plusieur user
    @ManyToOne(optional = false)
    @JsonIgnore
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


// relatiion many to one pour dire que plusieur simulation peuvaent appartenir à une seul categorie de credit

  @ManyToOne(optional = false)
  @JoinColumn(name = "categorieCredit_id")
  private CategorieCredit categorieCredit;


  @OneToMany(mappedBy = "simulation", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.EAGER)
  @JsonManagedReference
  @JsonIgnore
  private List<Notification> notifications;
}
