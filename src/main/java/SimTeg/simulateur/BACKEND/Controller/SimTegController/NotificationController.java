package SimTeg.simulateur.BACKEND.Controller.SimTegController;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import SimTeg.simulateur.BACKEND.Entity.SimTegEntity.Notification;
import SimTeg.simulateur.BACKEND.Service.SimTegService.NotificationService;

import java.util.List;

@RestController
@RequestMapping("api/notification")
public class NotificationController {
    @Autowired
    private NotificationService notificationService;
    @GetMapping("afficherNot/{idSimulation}")
    public List<Notification> afficherNotifSimulation(@PathVariable Integer idSimulation){
        return notificationService.afficherNotificationSim(idSimulation);

    }

}
