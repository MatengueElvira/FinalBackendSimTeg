package SimTeg.simulateur.BACKEND.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import SimTeg.simulateur.BACKEND.Config.JwtService;
import SimTeg.simulateur.BACKEND.Entity.RegistrationRequest;
import SimTeg.simulateur.BACKEND.Entity.RegistrationResponse;
import SimTeg.simulateur.BACKEND.Repository.RoleRepository;
import SimTeg.simulateur.BACKEND.Repository.UserRepository;
import SimTeg.simulateur.BACKEND.User.AuthenticationRequest;
import SimTeg.simulateur.BACKEND.User.AuthenticationResponse;
import SimTeg.simulateur.BACKEND.User.Role;
import SimTeg.simulateur.BACKEND.User.User;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private final AuthenticationManager authenticationManager;

    public void register(RegistrationRequest request) {
        //initialisation du role change to USER
        var userRole= roleRepository.findByName("ADMIN")
                .orElseThrow(()->new IllegalStateException("Role USER was not initialized"));

        var user = User.builder()
                .firstName(request.getFirstName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .accountLocked(false)
                .enable(false)
                .roles(List.of(userRole))
                .build();
        userRepository.save(user);

    }

    public AuthenticationResponse authenticate( AuthenticationRequest request) {

            var auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )

            );
            var users = userRepository.findByEmail(request.getEmail()).orElseThrow();
            System.out.println(auth);
            var claims = new HashMap<String, Object>();
            var user = ((User) auth.getPrincipal());
            claims.put("fullName", user.fullName());
            String refreshToken = jwtService.generateRefreshToken(user);
            var jwtToken = jwtService.generateToken(claims, user);

            System.out.println(jwtToken);
            return AuthenticationResponse.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .user(users)
                    .build();
    }

    public RegistrationResponse updatePhotos(String email, String photos){


        System.out.println(email);
        try {
            System.out.println("dans le try");
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {
                System.out.println("dans le if");
                User existingUser = userOptional.get();
                existingUser.setPhotos(photos);

                User savedUser = userRepository.save(existingUser);
                return RegistrationResponse.builder()
                        .user(savedUser)
                        .build();
//                reqRes.setOurUsers(savedUser);
//                reqRes.setStatusCode(200);
//                reqRes.setMessage("User updated successfully");
            } else {
                return RegistrationResponse.builder()
                        .erroMsg("User not found for update")
                        .build();
//                reqRes.setStatusCode(404);
//                reqRes.setMessage("User not found for update");
            }
        } catch (Exception e) {
            System.out.println("dans le catch");
            return RegistrationResponse.builder()
                    .erroMsg("Error occurred while updating user: " + e.getMessage())
                    .build();
//            reqRes.setStatusCode(500);
//            reqRes.setMessage("Error occurred while updating user: " + e.getMessage());
        }



    }

    public AuthenticationResponse getAllUser() {
        List<User> userList = userRepository.findAll();
        return AuthenticationResponse.builder()
                .userList(userList)
                .build();
    }

    public void toggleUserStatus(RegistrationRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User existingUser = userOptional.get();

            // Inverse l'état de l'utilisateur : s'il est activé, on le désactive et vice versa
            boolean newStatus = existingUser.isEnabled();
            existingUser.setEnable(!newStatus);

            // Sauvegarde de l'utilisateur avec le statut mis à jour
            userRepository.save(existingUser);

            // Affichage d'un message de confirmation
            if (newStatus) {
                System.out.println("Le compte de l'utilisateur a été activé.");
            } else {
                System.out.println("Le compte de l'utilisateur a été désactivé.");
            }
        } else {
            System.out.println("Aucun utilisateur trouvé avec l'email : " + request.getEmail());
        }
    }

    @Transactional
    public void updateRole(RegistrationRequest user, String roleName) {
        // Rechercher l'utilisateur par son ID
        Optional<User> userOptional = userRepository.findByEmail(user.getEmail());
        if (!userOptional.isPresent()) {
            throw new RuntimeException("Utilisateur non trouvé");
        }

        User existingUser = userOptional.get();

        // Rechercher le rôle par son nom
        Optional<Role> roleOptional = roleRepository.findByName(roleName);
        if (!roleOptional.isPresent()) {
            throw new RuntimeException("Rôle non trouvé");

        }

        var userRole= roleRepository.findByName(roleName)
                .orElseThrow(()->new IllegalStateException("Role USER was not initialized"));
        Role role = roleOptional.get();

        // Ajouter ou supprimer le rôle de l'utilisateur
        if (!existingUser.getRoles().contains(role)) {
            existingUser.setRoles(List.of(userRole));
//            existingUser.getRoles().add(role);  // Ajouter le rôle
        } else {
            existingUser.getRoles().remove(role);  // Supprimer le rôle si déjà présent
        }
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalide la session HTTP si elle existe
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Supprime le contexte de sécurité
        SecurityContextHolder.clearContext();
    }

    /**
     * Rafraîchir le token d'accès à l'aide du refresh token.
     */
    public String refreshAccessToken(String refreshToken) {
        // Vérifier si le refresh token est expiré
        if (jwtService.isTokenExpired(refreshToken)) {
            throw new IllegalStateException("Refresh token has expired");
        }

        // Extraire l'utilisateur à partir du refresh token
        String username = jwtService.extractUsername(refreshToken);
        var user = userRepository.findByEmail(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Générer un nouveau token d'accès
        return jwtService.generateToken(user);
    }

}
