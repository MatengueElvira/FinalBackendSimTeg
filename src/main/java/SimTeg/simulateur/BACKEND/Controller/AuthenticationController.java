package SimTeg.simulateur.BACKEND.Controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import SimTeg.simulateur.BACKEND.Entity.RegistrationRequest;
import SimTeg.simulateur.BACKEND.Service.AuthenticationService;
import SimTeg.simulateur.BACKEND.User.AuthenticationRequest;
import SimTeg.simulateur.BACKEND.User.AuthenticationResponse;

import java.util.Map;

@RestController
@CrossOrigin("*")
public class AuthenticationController {

    @Autowired
    private AuthenticationService service;

    @PostMapping("/auth/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> register(
             @RequestBody @Valid RegistrationRequest request
    ){
        System.out.println("dans register");
        service.register(request);
        return ResponseEntity.accepted().build();
    }



    @PostMapping("/auth/login")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request ){
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/admin/activate")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> activateAccount(
            @RequestBody @Valid RegistrationRequest request
    ){
        System.out.println("dans register");
        service.toggleUserStatus(request);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("admin/updateRole")
    public ResponseEntity<?> updateRole( @RequestBody @Valid RegistrationRequest request, @RequestBody String roleName) {
        service.updateRole(request, roleName);
        System.out.println(request+ roleName);

        // Appeler le service pour mettre à jour le rôle
        return ResponseEntity.accepted().build();
    }


    @GetMapping("admin/getAllUser")
    public ResponseEntity<AuthenticationResponse> getAllUser(){
        return ResponseEntity.ok(service.getAllUser());
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        service.logout(request, response);
        return ResponseEntity.noContent().build(); // Réponse 204
    }

    @PostMapping("/api/refresh-token")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");
            String newAccessToken = service.refreshAccessToken(refreshToken);
            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }


}
