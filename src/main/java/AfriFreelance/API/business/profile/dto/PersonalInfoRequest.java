package AfriFreelance.API.business.profile.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PersonalInfoRequest {

    @NotBlank(message = "Le nom complet est obligatoire")
    @Size(max = 200)
    private String fullName;

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "Le nom d'utilisateur ne peut contenir que des lettres, chiffres et underscore")
    private String username;

    @Size(max = 200, message = "Le titre ne doit pas dépasser 200 caractères")
    private String headline;

    @Size(max = 2000, message = "La bio ne doit pas dépasser 2000 caractères")
    private String bio;

    private String country;
    private String city;
    private String timezone;
    private String primaryLanguage;
    private String avatarUrl;
    private String defaultCurrency;
}
