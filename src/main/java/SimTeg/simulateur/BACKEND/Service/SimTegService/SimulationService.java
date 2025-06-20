package SimTeg.simulateur.BACKEND.Service.SimTegService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import SimTeg.simulateur.BACKEND.Dto.DataType.Assurance;
import SimTeg.simulateur.BACKEND.Dto.DataType.Frais;
import SimTeg.simulateur.BACKEND.Dto.EnumerationSimTeg.Frequence;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.DonneesPret;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoRequest;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationDtoResponse;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.SimulationRequestGlobal;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.CategorieCreditRepository;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.SimulationRepository;
import SimTeg.simulateur.BACKEND.Repository.UserRepository;
import SimTeg.simulateur.BACKEND.User.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SimulationService {
    @Autowired
    private CalculTEGService calculerTEG;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private SimulationRepository simulationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategorieCreditRepository categorieCreditRepository;

    public SimulationDtoResponse doSimulation(SimulationDtoRequest simulationDtoRequest){

        CategorieCredit categorie = categorieCreditRepository.findById(simulationDtoRequest.getCategoriId())
                .orElseThrow(() -> new RuntimeException("Catégorie de crédit introuvable"));
        List<String> fraisObligatoires = categorie.getFraisObligatoire();
        List<String> assurancesObligatoires = categorie.getAssurances();

        simulationDtoRequest.getFraisList().forEach(f -> {
            if (!fraisObligatoires.contains(f.getNom())) {
                throw new RuntimeException("Frais non valide pour cette catégorie : " + f.getNom());
            }
        });
        simulationDtoRequest.getAssuranceList().forEach(a -> {
            if (!assurancesObligatoires.contains(a.getNom())) {
                throw new RuntimeException("Assurance non valide pour cette catégorie : " + a.getNom());
            }
        });
        double totalFrais = simulationDtoRequest.getFraisList().stream().mapToDouble(Frais::getMontant).sum();
        double montantAssuranceParEcheance = simulationDtoRequest.getAssuranceList().stream().mapToDouble(Assurance::getMontant).sum();
        DonneesPret donneesPret=new DonneesPret();
        donneesPret.setDuree(simulationDtoRequest.getDuree());
        donneesPret.setMontantEmprunte(simulationDtoRequest.getMontantEmprunte());
        donneesPret.setFrais(totalFrais);
        donneesPret.setFrequence(simulationDtoRequest.getFrequence());
        donneesPret.setTauxInteretNominal(simulationDtoRequest.getTauxInteretNominal());
        donneesPret.setAssurance(montantAssuranceParEcheance);
        donneesPret.setDeblocages(simulationDtoRequest.getTableauDeblocages());
        donneesPret.setDatePremiereEcheance(simulationDtoRequest.getDatePremiereEcheance());
        return calculerTEG.calculerTEG(donneesPret);

    }
// methode pour enregistrer une simulation
    public Simulation EnregistrerSimulation(SimulationRequestGlobal simulationRequestGlobal,String email){
        Simulation simulation=new Simulation();
       SimulationDtoRequest simulationDtoRequest=  simulationRequestGlobal.getRequest();
       SimulationDtoResponse simulationDtoResponse=simulationRequestGlobal.getResponse();
        Optional<User> user =userRepository.findByEmail(email);
        Optional<CategorieCredit> categorieCredit= categorieCreditRepository.findById(simulationDtoRequest.getCategoriId());
        if(user.isPresent()){
            simulation.setUser(user.get());
            if(categorieCredit.isPresent()){
                simulation.setCategorieCredit(categorieCredit.get());
                simulation.setTableauDeblocages(simulationDtoRequest.getTableauDeblocages());
                simulation.setDatePremiereEcheance(simulationDtoRequest.getDatePremiereEcheance());
                simulation.setTauxNominal(simulationDtoRequest.getTauxInteretNominal());
                simulation.setMontant(simulationDtoRequest.getMontantEmprunte());
                simulation.setFrequence(simulationDtoRequest.getFrequence());
                simulation.setFraisJson(simulationDtoRequest.getFraisList());
                simulation.setDuree(simulationDtoRequest.getDuree());
                simulation.setTypeEmprunteur(simulationDtoRequest.getTypeEprunteur());
                simulation.setAssuranceJson(simulationDtoRequest.getAssuranceList());
                simulation.setTeg(simulationDtoResponse.getTegAnnuel());
                simulation.setEcheance(simulationDtoResponse.getEcheance());
                simulation.setTableauAmortisement(simulationDtoResponse.getTableauAmortissement());
                Simulation simulation1=simulationRepository.save(simulation);
                notificationService.verifierEtNotifier(simulation1);
                return simulation1;
            }
            return null;

        }

        return null;
    }
     public boolean deleteSimulation (Integer id){
       Optional<Simulation> simulation= simulationRepository.findById(id);
       if(simulation.isPresent())
         {

             Simulation simul=simulation.get();
             if (simul.getTableauAmortisement() != null) {
                 simul.getTableauAmortisement().clear();
             }
             if (simul.getTableauDeblocages() != null) {
                 simul.getTableauDeblocages().clear();
             }
             if (simul.getFraisJson() != null) {
                 simul.getFraisJson().clear();
             }
             if (simul.getAssuranceJson() != null) {
                 simul.getAssuranceJson().clear();
             }
             simulationRepository.deleteNotificationsBySimulationId(id);

             // Puis supprimer la simulation elle-même
             simulationRepository.deleteSimulationByIdNative(id);
             simulationRepository.delete(simul);
             return  true;
         }
        return false;
     }
     public  Simulation UptdateSimulation( SimulationDtoRequest simulationDtoRequest ,Integer id){
        Optional<Simulation> simulatio= simulationRepository.findById(id);

                if(simulatio.isPresent()) {
                    SimulationDtoResponse simulationDtoResponse=doSimulation(simulationDtoRequest);
                    Simulation simulation= simulatio.get();
                    simulation.setTypeEmprunteur(simulationDtoRequest.getTypeEprunteur());
                    simulation.setMontant(simulationDtoRequest.getMontantEmprunte());
                    simulation.setDuree(simulationDtoRequest.getDuree());
                    simulation.setTypeEmprunteur(simulationDtoRequest.getTypeEprunteur());
                    simulation.setFrequence(simulationDtoRequest.getFrequence());
                    simulation.setTauxNominal(simulationDtoRequest.getTauxInteretNominal());
                    simulation.setFraisJson(simulationDtoRequest.getFraisList());
                    simulation.setAssuranceJson(simulationDtoRequest.getAssuranceList());
                    simulation.setTeg(simulationDtoResponse.getTegAnnuel());
                    simulation.setEcheance(simulationDtoResponse.getEcheance());
                    simulation.setTableauAmortisement(simulationDtoResponse.getTableauAmortissement());


                    Simulation simulation1=simulationRepository.save(simulation);
                    notificationService.verifierEtNotifier(simulation1);
                    return simulation1;

                }
        return null ;

     }


    @Transactional
     public List<Simulation> simulationParUserID(Integer id){
       if(userRepository.existsById(id)){
           return  simulationRepository.findByUserId(id);
       }
        return null;
     }
     public List<Simulation> getAllSimulationUserByCategorieId( Integer categorieid,Integer userId){
       List<Simulation> simulation=this.simulationParUserID(userId);
       List<Simulation> simulationresult = new ArrayList<>();
       for (Simulation simulation1: simulation){
        CategorieCredit categorieCredit=   simulation1.getCategorieCredit();
       if(categorieCredit.getId()==categorieid) {
           simulationresult.add(simulation1);

       }
           return  simulationresult;

       }
       return  null;

     }


}
