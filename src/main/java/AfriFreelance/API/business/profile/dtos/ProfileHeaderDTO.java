package AfriFreelance.API.business.profile.dtos;

import lombok.Data;

import java.util.UUID;

@Data
public class ProfileHeaderDTO {
    private UUID id;
    private String username;
    private String fullName;
    private String avatarUrl;
    private String headline;
    private String country;
    private String city;
    private Integer memberSinceYear;
    private Boolean isVerified;
    private String activeMode;
}
