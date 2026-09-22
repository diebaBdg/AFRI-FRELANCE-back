package AfriFreelance.API.business.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionItemDTO {
    private String label;
    private String key;
    private boolean completed;
}
