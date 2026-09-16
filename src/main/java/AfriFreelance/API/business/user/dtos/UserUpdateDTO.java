package AfriFreelance.API.business.user.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import AfriFreelance.API.enums.UserStatus;
import AfriFreelance.API.validation.StrongPassword;

import java.util.Set;

@Data
public class UserUpdateDTO {

    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    private String username;

    @Email(message = "L'email doit être valide")
    private String email;

    @Size(max = 200, message = "Le nom complet ne doit pas dépasser 200 caractères")
    private String fullName;

    private String adresse;

    @Size(max = 20, message = "Le numéro de téléphone ne doit pas dépasser 20 caractères")
    private String phone;

    @StrongPassword
    private String password;

    private UserStatus status;

    /** Codes des roles a assigner (remplace les roles existants si fourni). */
    private Set<String> roleCodes;
}
