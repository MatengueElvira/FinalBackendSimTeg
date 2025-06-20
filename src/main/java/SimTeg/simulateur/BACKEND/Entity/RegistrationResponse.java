package SimTeg.simulateur.BACKEND.Entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import SimTeg.simulateur.BACKEND.User.User;

@Getter
@Setter
@Builder
public class RegistrationResponse {

    private User user;
    private String erroMsg;
    private String success;

}
