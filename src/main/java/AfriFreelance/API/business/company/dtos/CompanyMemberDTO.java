package AfriFreelance.API.business.company.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyMemberDTO {
    private UUID id;
    private UUID userId;
    private String fullName;
    private String avatarUrl;
    private String role;
    private LocalDateTime joinedAt;
}
