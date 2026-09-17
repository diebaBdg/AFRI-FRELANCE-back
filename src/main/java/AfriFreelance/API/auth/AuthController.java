package AfriFreelance.API.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import AfriFreelance.API.auth.dtos.*;
import AfriFreelance.API.business.user.Role;
import AfriFreelance.API.business.user.RoleRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.enums.UserStatus;
import AfriFreelance.API.utils.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Inscription, connexion et gestion des tokens JWT")
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    private Optional<User> findUserByLogin(String login) {
        Optional<User> userByEmail = userRepository.findByEmail(login);
        if (userByEmail.isPresent()) {
            return userByEmail;
        }
        return userRepository.findByUsername(login);
    }

    @PostMapping("/login")
    @Operation(summary = "Connexion utilisateur (email ou username + mot de passe)")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest) {
        try {
            Optional<User> userOpt = findUserByLogin(loginRequest.login());

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiant ou mot de passe incorrect");
            }

            User user = userOpt.get();
            String springUsername = user.getUsername();

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(springUsername, loginRequest.password())
            );

            if (authentication.isAuthenticated()) {
                String token = jwtUtils.generateToken(user);

                return ResponseEntity.ok(new LoginResponse(
                        user.getId(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getFullName(),
                        token,
                        user.getRoles().stream()
                                .map(role -> role.getCode())
                                .collect(Collectors.toSet())
                ));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Identifiants invalides");
            }

        } catch (AuthenticationException e) {
            log.error("Erreur d'authentification : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Identifiant ou mot de passe incorrect");
        } catch (Exception e) {
            log.error("Erreur inattendue lors de la connexion : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erreur serveur");
        }
    }


    @GetMapping("/generate-hash")
    @Operation(summary = "Générer un hash BCrypt pour un mot de passe (outil de débogage)")
    public ResponseEntity<?> generateHash(@RequestParam String password) {
        try {
            String encodedPassword = passwordEncoder.encode(password);
            boolean matches = passwordEncoder.matches(password, encodedPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("password", password);
            response.put("hash", encodedPassword);
            response.put("length", encodedPassword.length());
            response.put("matches_test", matches);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Erreur lors de la génération du hash : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Erreur: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription d'un nouvel utilisateur (rôle DEMANDEUR par défaut)")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            if (userRepository.findByEmail(registerRequest.getEmail()).isPresent()) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ApiError("Cet email est déjà utilisé"));
            }

            if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new ApiError("Les mots de passe ne correspondent pas"));
            }

            String username = generateUsernameFromEmail(registerRequest.getEmail());

            if (userRepository.findByUsername(username).isPresent()) {
                int counter = 1;
                String baseUsername = username;
                while (userRepository.findByUsername(username).isPresent()) {
                    username = baseUsername + counter;
                    counter++;
                }
            }

            User newUser = User.builder()
                    .username(username)
                    .email(registerRequest.getEmail())
                    .fullName(registerRequest.getNom() + " " + registerRequest.getPrenom())
                    .phone(registerRequest.getPhone())
                    .password(passwordEncoder.encode(registerRequest.getPassword()))
                    .status(UserStatus.ACTIF)
                    .build();

            Role demandeurRole = roleRepository.findByCode("DEMANDEUR")
                    .orElseThrow(() -> new RuntimeException("Rôle DEMANDEUR introuvable. Veuillez initialiser les rôles dans la base."));

            newUser.setRoles(new HashSet<>());
            newUser.getRoles().add(demandeurRole);

            userRepository.save(newUser);

            log.info("Nouvel utilisateur inscrit : {} (username: {})", newUser.getEmail(), newUser.getUsername());

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterResponse(
                            "Inscription réussie ! Vous pouvez maintenant vous connecter.",
                            newUser.getEmail()
                    ));

        } catch (RuntimeException e) {
            log.error("Erreur lors de l'inscription : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError(e.getMessage()));
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'inscription : {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiError("Une erreur est survenue lors de l'inscription"));
        }
    }

    private String generateUsernameFromEmail(String email) {
        String usernamePart = email.split("@")[0];
        usernamePart = usernamePart.replaceAll("[^a-zA-Z0-9]", "");
        return usernamePart.length() > 50 ? usernamePart.substring(0, 50) : usernamePart;
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}