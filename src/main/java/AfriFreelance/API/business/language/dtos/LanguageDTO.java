package AfriFreelance.API.business.language.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {
    private UUID id;
    private String languageName;
    private String level;
}
