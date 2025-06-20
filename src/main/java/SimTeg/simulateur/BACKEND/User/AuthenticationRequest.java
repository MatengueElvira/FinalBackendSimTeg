package SimTeg.simulateur.BACKEND.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Builder
@Data
public class AuthenticationRequest {
    @Email(message = "Email is not formated")
    @NotBlank(message = "Email is mandatory")
    @NotBlank(message = "Email is mandatory")
    private String email;

    @NotBlank(message = "password is mandatory")
    @NotBlank(message = "password is mandatory")
    @Size(min = 4, message="password should be 8 characters long")
    private String password;
}
