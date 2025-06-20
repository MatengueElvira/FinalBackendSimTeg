package SimTeg.simulateur.BACKEND.Dto.SimTegDto;

import lombok.Data;
import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;

import java.util.List;
@Data
public class SimulationDtoResponse {
    public double tegAnnuel;
    public double echeance;
    public List<AmortisementLigne> tableauAmortissement;
}
