/*package SimTeg.simulateur.BACKEND.Service.SimTegService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.DonneesPret;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;

import java.util.ArrayList;
import java.util.List;


@Service
 public class CalculTEGService {
@Autowired
private NotificationService notificationService;
    public  SimulationDtoResponse calculerTEG(DonneesPret donnees) {
        if (donnees.montantEmprunte <= 0 || donnees.tauxInteretNominal <= 0 || donnees.duree <= 0) {
            throw new IllegalArgumentException("Les données du prêt sont invalides.");
        }

        int frequence ;
        switch (donnees.getFrequence()) {
            case MENSUELLE:
                frequence = 12;
                break;
            case TRIMESTRIALITE:
                frequence = 4;
                break;
            case ANNUELLE:
            default:
                frequence = 1;
                break;
        }

        double tauxPeriodique = (donnees.tauxInteretNominal/100) / frequence;
        //durée est exprimée en annnée d'ou la multiplication en fonctionde la frequence
        double duree=donnees.getDuree()*frequence;
        donnees.setDuree(duree);

        // Calcul de l'annuité ou mensualité fixe (échéance constante)
        double echeance = (donnees.montantEmprunte * tauxPeriodique) /
                (1 - Math.pow(1 + tauxPeriodique, -donnees.duree));
        double echeanceTotale = echeance + donnees.assurance;

        // Capital net perçu
        double capitalNet = donnees.montantEmprunte - donnees.frais ;

        // Calcul du TEG via méthode itérative (formule BEAC avec flux actualisés)
        double tegPeriodique = trouverTaux(capitalNet, echeanceTotale, donnees.duree);
        double tegAnnuel = Math.pow(1 + tegPeriodique, frequence) - 1;

        List<AmortisementLigne> tableau = genererTableau(donnees, tauxPeriodique, echeance);

        SimulationDtoResponse simulationDtoResponse=new SimulationDtoResponse();
        simulationDtoResponse.setTegAnnuel(tegAnnuel * 100);
        simulationDtoResponse.setTableauAmortissement(tableau);
        simulationDtoResponse.setEcheance(echeance);

        return simulationDtoResponse;
    }

    private static double trouverTaux(double capital, double echeanceTotale, double duree) {
        double tauxMin = 0.00000001;
        double tauxMax = 1;
        double precision = 1e-10;
        double taux = 0;

        while ((tauxMax - tauxMin) > precision) {
            taux = (tauxMin + tauxMax) / 2;
            double sommeActualisee = 0;
            for (int periode = 1; periode <= duree; periode++) {
                sommeActualisee += echeanceTotale / Math.pow(1 + taux, periode);
            }
            if (sommeActualisee > capital) {
                tauxMin = taux;
            } else {
                tauxMax = taux;
            }
        }
        return taux;
    }
    private static List<AmortisementLigne> genererTableau(DonneesPret donnees, double tauxPeriodique, double echeance) {
        List<AmortisementLigne> tableau = new ArrayList<>();
        double capitalRestant = donnees.montantEmprunte;

        for (int periode = 1; periode <= donnees.duree; periode++) {
            double interet = capitalRestant * tauxPeriodique;
            double principal = echeance - interet;
            capitalRestant -= principal;
            AmortisementLigne echeance1=new AmortisementLigne();
            echeance1.setNumero(periode);
            echeance1.setCapitalRestant(Math.max(capitalRestant, 0));
            echeance1.setInteret(interet);
            echeance1.setPrincipal(principal);
            echeance1.setAssurance(donnees.assurance);
            echeance1.setEcheanceTotale(echeance + donnees.assurance);

            tableau.add(echeance1);
        }

        return tableau;
    }
}
*/
package SimTeg.simulateur.BACKEND.Service.SimTegService;

import SimTeg.simulateur.BACKEND.Dto.DataType.AmortisementLigne;
import SimTeg.simulateur.BACKEND.Dto.DataType.DeblocageLigne;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.DonneesPret;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Service
public class CalculTEGService {

    @Autowired
    private NotificationService notificationService;

    private static final double TOLERANCE = 1e-10;
    private static final int MAX_ITERATIONS = 100;

    public SimulationDtoResponse calculerTEG(DonneesPret donnees) {
        if (donnees.getMontantEmprunte() <= 0 || donnees.getTauxInteretNominal() <= 0 || donnees.getDuree() <= 0) {
            throw new IllegalArgumentException("Les données du prêt sont invalides.");
        }

        int frequence;
        switch (donnees.getFrequence()) {
            case MENSUELLE:
                frequence = 12;
                break;
            case TRIMESTRIALITE:
                frequence = 4;
                break;
            case ANNUELLE:
            default:
                frequence = 1;
                break;
        }

        double tauxPeriodique = (donnees.getTauxInteretNominal() / 100) / frequence;
        double duree = donnees.getDuree() * frequence;
        donnees.setDuree(duree);

        double montantTotalEmprunte = donnees.getMontantEmprunte();

        double echeance = (montantTotalEmprunte * tauxPeriodique) /
                (1 - Math.pow(1 + tauxPeriodique, -duree));
        System.out.println(tauxPeriodique);
        System.out.println(montantTotalEmprunte);
        System.out.println(duree);
        double echeanceTotale = echeance + donnees.getAssurance();

        // Capital net perçu (somme des déblocages - frais)
        double capitalNet = donnees.getDeblocages().stream().mapToDouble(DeblocageLigne::getMontant).sum() - donnees.getFrais();

        Function<Double, Double> equation = (taux) -> {
            double somme = 0;



            for (int i = 0; i < donnees.getDeblocages().size(); i++) {
                DeblocageLigne deblocage = donnees.getDeblocages().get(i);
                long jours = ChronoUnit.DAYS.between(deblocage.getDateDeblocage(), donnees.getDatePremiereEcheance());
                somme -= deblocage.getMontant() * Math.pow(1 + taux, -jours / 365.0);
            }

            for (int k = 1; k <= duree; k++) {
                somme += echeanceTotale * Math.pow(1 + taux, -k/frequence );
            }

            return somme;
        };

        double teg = newtonRaphson(equation);
        double tegAnnuel = Math.pow(1 + teg, frequence) - 1;

        List<AmortisementLigne> tableau = genererTableau(donnees, tauxPeriodique, echeance);

        SimulationDtoResponse simulationDtoResponse = new SimulationDtoResponse();
        simulationDtoResponse.setTegAnnuel(tegAnnuel * 100);
        simulationDtoResponse.setTableauAmortissement(tableau);
        simulationDtoResponse.setEcheance(echeance);
        System.out.println("Taux périodique: " + tauxPeriodique);
        System.out.println("Durée (nb périod): " + duree);
        System.out.println("Capital net: " + capitalNet);
        System.out.println("Echéance hors assurance: " + echeance);
        System.out.println("Echéance totale (avec assurance): " + echeanceTotale);

        return simulationDtoResponse;

    }

    private double newtonRaphson(Function<Double, Double> f) {
        double x = 0.05; // estimation initiale 5%
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            double fx = f.apply(x);
            double fdx = (f.apply(x + TOLERANCE) - fx) / TOLERANCE;
            double x1 = x - fx / fdx;
            if (Math.abs(x1 - x) < TOLERANCE) return x1;
            x = x1;
        }
        throw new IllegalArgumentException("Échec du calcul du TEG");
    }

    private List<AmortisementLigne> genererTableau(DonneesPret donnees, double tauxPeriodique, double echeance) {
        List<AmortisementLigne> tableau = new ArrayList<>();
        double capitalRestant = donnees.getMontantEmprunte();

        for (int periode = 1; periode <= donnees.getDuree(); periode++) {
            double interet = capitalRestant * tauxPeriodique;
            double principal = echeance - interet;
            capitalRestant -= principal;

            AmortisementLigne ligne = new AmortisementLigne();
            ligne.setNumero(periode);
            ligne.setCapitalRestant(Math.max(capitalRestant, 0));
            ligne.setInteret(interet);
            ligne.setPrincipal(principal);
            ligne.setAssurance(donnees.getAssurance());
            ligne.setEcheanceTotale(echeance + donnees.getAssurance());

            tableau.add(ligne);
        }

        return tableau;


    }


}
