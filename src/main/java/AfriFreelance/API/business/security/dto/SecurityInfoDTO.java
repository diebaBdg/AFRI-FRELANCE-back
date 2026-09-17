package AfriFreelance.API.business.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SecurityInfoDTO {
    private String email;
    private String phone;
    private Boolean hasPassword;
    private LocalDateTime lastPasswordChange;
    private Boolean twoFactorEnabled;
}
