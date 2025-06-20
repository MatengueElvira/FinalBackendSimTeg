package SimTeg.simulateur.BACKEND.Dto.SimTegDto;


import lombok.Data;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.TypeEprunteur;

@Data
public class TauxUsureDto {
    private double seuil;
    private int annee;
    private Integer categorieId;
    private TypeEprunteur typeEprunteur;

}
