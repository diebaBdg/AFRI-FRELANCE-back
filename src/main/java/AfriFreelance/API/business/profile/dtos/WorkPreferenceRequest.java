package AfriFreelance.API.business.profile.dtos;

import lombok.Data;

@Data
public class WorkPreferenceRequest {
    private String[] workTypes;
    private String[] projectTypes;
    private String[] acceptedCountries;
    private String timezone;
}
