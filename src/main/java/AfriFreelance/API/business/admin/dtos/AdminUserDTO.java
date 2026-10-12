package AfriFreelance.API.business.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDTO {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String phone;
    private String status;
    private String activeMode;
    private String avatarUrl;
    private String headline;
    private String country;
    private String city;
    private List<String> roles;
    private Boolean hasFreelanceProfile;
    private Boolean hasClientProfile;
    private LocalDateTime memberSince;
}
