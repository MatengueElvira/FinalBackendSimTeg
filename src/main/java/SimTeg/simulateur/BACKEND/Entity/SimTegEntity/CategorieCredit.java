
package SimTeg.simulateur.BACKEND.Entity.SimTegEntity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import SimTeg.simulateur.BACKEND.Entity.AbstractEntity;


import java.util.List;

@Entity
@Data
@ToString(exclude = {"tauxUsures,simulations"})
public class CategorieCredit extends AbstractEntity {
    @Column
    private String nomCategorie;
    @Column
    private String description;
    @Column
    private List<String> fraisObligatoire;
    @Column
    private List<String> Assurances;
    @OneToMany(mappedBy = "categorieCredit", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnore
    private List<TauxUsure> tauxUsures;
    @OneToMany(mappedBy = "categorieCredit", cascade = CascadeType.ALL, orphanRemoval = true,  fetch = FetchType.EAGER)
    @JsonManagedReference
    @JsonIgnore
    private List<Simulation> simulations;
}





