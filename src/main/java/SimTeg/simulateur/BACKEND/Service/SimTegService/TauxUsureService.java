package SimTeg.simulateur.BACKEND.Service.SimTegService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import SimTeg.simulateur.BACKEND.Dto.SimTegDto.TauxUsureDto;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.CategorieCredit;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.HistoriqueTauxUsure;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TauxUsureService {
    @Autowired
    private TauxUsureRepository tauxUsureRepository;
    @Autowired
    private HistoriqueRepository historiqueRepository;
    @Autowired
    private NotificationService notificationService ;
    @Autowired
    private SimulationRepository simulationRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private CategorieCreditRepository categorieCreditRepository;

    public TauxUsure CreerTauxUsure(TauxUsureDto dto){
        TauxUsure tauxUsure=new TauxUsure();
        int annee = LocalDate.now().getYear();

        Optional<CategorieCredit> categorieCreditOptional=categorieCreditRepository.findById(dto.getCategorieId());
        if(categorieCreditOptional.isPresent()){
            CategorieCredit categorieCredit=categorieCreditOptional.get();
            tauxUsure.setCategorieCredit(categorieCredit);
            tauxUsure.setAnnee(annee);
            tauxUsure.setSeuil(dto.getSeuil());
            tauxUsure.setTypeEmprunteur(dto.getTypeEprunteur());
            Optional<TauxUsure> tauxUsure1=tauxUsureRepository.findByCategorieCreditAndTypeEmprunteurAndAnnee(categorieCredit,dto.getTypeEprunteur(),dto.getAnnee());
           if(tauxUsure1.isPresent()){
               throw new RuntimeException("imposible de definir le meme taux d'usure pour la meme année!!!");
           }

            return tauxUsureRepository.save(tauxUsure);
        }
        return null;




    }
    public boolean deleteTauxUsure(Integer id){

        if (tauxUsureRepository.existsById(id)){
            tauxUsureRepository.deleteTauxUsureById(id);

            return true;
        }

        return false;
    }


    public List<TauxUsure> getTauxUsureByCategorieId( Integer  idCategorie){
        if (categorieCreditRepository.existsById(idCategorie)){
            List<TauxUsure> liste=tauxUsureRepository.findByCategorieCreditId(idCategorie);
            return liste;
        }
        return  null;

    }
    @Transactional
    public TauxUsure updateTauxUsure(Integer id ,TauxUsureDto tauxUsureDto){
        TauxUsure existing = tauxUsureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Taux d’usure introuvable"));
        Optional<TauxUsure> tauxUsureOptional=tauxUsureRepository.findById(id) ;
        if (tauxUsureOptional.isPresent()){
            TauxUsure tauxUsure=tauxUsureOptional.get();
            tauxUsure.setSeuil(tauxUsureDto.getSeuil());
            tauxUsure.setAnnee(tauxUsureDto.getAnnee());
            tauxUsure.setTypeEmprunteur(tauxUsureDto.getTypeEprunteur());
            TauxUsure tauxUsure1=tauxUsureRepository.save(tauxUsure);

            // Sauvegarder l’historique
            HistoriqueTauxUsure historique = new HistoriqueTauxUsure();
            historique.setTauxUsureId(existing.getId());
            historique.setAncienTauxUsure(existing.getTauxUsure());
            historique.setNouveauTauxUsure(tauxUsure1.getTauxUsure());
            historique.setAncienSeuil(existing.getSeuil());
            historique.setNouveauSeuil(tauxUsure1.getSeuil());
            historique.setAncienneAnnee(existing.getAnnee());
            historique.setNouvelleAnnee(tauxUsure1.getAnnee());
            historique.setDateModification(LocalDateTime.now());
            historique.setTypeChangement("MISE À JOUR");

            historiqueRepository.save(historique);

            reVerifierSimulationsLiees(tauxUsure1);

            return tauxUsure1;
        }
        return null;

    }
    private void reVerifierSimulationsLiees(TauxUsure tauxUsure) {
        List<Simulation> simulations = simulationRepository.findByCategorieCreditAndTypeEmprunteurAndDateDebutYear(
                tauxUsure.getCategorieCredit(),
                tauxUsure.getTypeEmprunteur(), // à adapter si le type est stocké ailleurs
                (int) tauxUsure.getAnnee()
        );

        for (Simulation sim : simulations) {
            notificationService.verifierEtNotifier(sim);
        }
    }
    public List<TauxUsure> listTauxUsure(){

        return tauxUsureRepository.findAll();
    }

    public List<HistoriqueTauxUsure>listHistorique(Integer idTauxUsure){
        return historiqueRepository.findByTauxUsureId(idTauxUsure);
    }

}
