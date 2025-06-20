package SimTeg.simulateur.BACKEND.Entity;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDate;

@Builder
@Getter
@Setter
public class RegistrationRequest {

    @NotBlank(message = "firstName is mandatory")
    @NotBlank(message = "firstname is mandatory")
    private String firstName;

//    @NotBlank(message = "lastname obligatoire")
//    @NotBlank(message = "lastname obligatoire")
//    private String lastName;

    @Email(message = "Email incorrect dans ça structure")
    @NotBlank(message = "Email est obligatoire")
    @NotBlank(message = "Email  obligatoire")
    private String email;

    @NotBlank(message = "password obligatoire")
    @NotBlank(message = "password obligatoire")
    @Size(min = 4, message="password au mois 8 caractére!")
    private String password;

//    private String photos;



//    @NotBlank(message = "dateNaiss obligatoire")
//    @NotBlank(message = "dateNaiss obligatoire")
//    private LocalDate dateNaiss;

}
