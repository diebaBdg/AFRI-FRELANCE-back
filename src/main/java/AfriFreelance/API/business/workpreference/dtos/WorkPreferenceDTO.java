package AfriFreelance.API.business.workpreference.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkPreferenceDTO {
    private UUID id;
    private String[] workTypes;
    private String[] projectTypes;
    private String[] acceptedCountries;
    private String timezone;
}
