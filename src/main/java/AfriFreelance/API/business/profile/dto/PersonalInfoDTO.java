package AfriFreelance.API.business.profile.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class PersonalInfoDTO {
    private UUID id;
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private String headline;
    private String bio;
    private String country;
    private String city;
    private String timezone;
    private String primaryLanguage;
    private String avatarUrl;
    private String defaultCurrency;
    private String activeMode;
    private Integer memberSinceYear;
}
