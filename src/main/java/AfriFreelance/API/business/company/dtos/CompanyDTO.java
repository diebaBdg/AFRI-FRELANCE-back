package AfriFreelance.API.business.company.dtos;

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
public class CompanyDTO {
    private UUID id;
    private String legalName;
    private String commercialName;
    private String sector;
    private String size;
    private String country;
    private String city;
    private String website;
    private String description;
    private String logoUrl;
    private Boolean isVerified;
    private LocalDateTime createdAt;
    private List<CompanyMemberDTO> members;
}
