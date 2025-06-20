package SimTeg.simulateur.BACKEND.Service.SimTegService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Notification;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Simulation;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.TauxUsure;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.NotificationRepository;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.SimulationRepository;
import SimTeg.simulateur.BACKEND.Repository.SimTegRepository.TauxUsureRepository;
import SimTeg.simulateur.BACKEND.handler.NotificationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class NotificationService {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private  TauxUsureRepository tauxUsureRepository;
    @Autowired
    private SimulationRepository simulationRepository;

    public void verifierEtNotifier(Simulation simulation) {
        int annee = LocalDate.now().getYear();

        try {
            Optional<TauxUsure> taux = tauxUsureRepository.findByCategorieCreditAndTypeEmprunteurAndAnnee(
                    simulation.getCategorieCredit(),
                    simulation.getTypeEmprunteur(),
                    annee
            );

            String message;
            boolean alerte;

            if (taux.isPresent()) {
                double seuil = taux.get().getTauxUsure();

                if (simulation.getTeg() > seuil) {
                    message = String.format("TEG %.2f%% dépasse le taux d'usure %.2f%% pour l'année %d.",
                            simulation.getTeg(), seuil, annee);
                    alerte = true;
                } else {
                    message = String.format("TEG %.2f%% est conforme au seuil %.2f%% pour l'année %d.",
                            simulation.getTeg(), seuil, annee);
                    alerte = false;
                }

                // Créer ou mettre à jour la notification
                Notification notif = notificationRepository
                        .findBySimulation(simulation)
                        .orElse(new Notification());

                notif.setSimulation(simulation);
                notif.setMessage(message);
                notif.setDateNotification(LocalDateTime.now());
                notif.setAlerte(alerte);
                notif.setAnnee(annee);

                notificationRepository.save(notif);

            } else {
                message = String.format("Aucun taux d'usure défini pour %s / %s en %d. Vérification impossible.",
                        simulation.getCategorieCredit().getNomCategorie(),
                        simulation.getTypeEmprunteur(),
                        annee);
                alerte = false; // ou true si tu veux avertir même sans seuil

                Notification notiff=new Notification();
                notiff.setSimulation(simulation);
                notiff.setMessage(message);
                notiff.setAlerte(alerte);

                notificationRepository.save(notiff);
            }

        } catch (Exception e) {
            throw new NotificationException("Erreur lors de la vérification du TEG : " + e.getMessage());
        }
    }
    public List<Notification> afficherNotificationSim(Integer idSimulation){
        Optional<Simulation> simulate= simulationRepository.findById(idSimulation);
        if(simulate.isPresent()){
            List<Notification> notifications=notificationRepository.findBySimulationId(idSimulation);
            if (notifications.isEmpty()){
                List<Notification> vide=new ArrayList<>();
                return vide;

            }
            return  notifications;
        }

        throw new RuntimeException("la simulation n'existe probablement pas ");
    }
}
