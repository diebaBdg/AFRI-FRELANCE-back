package AfriFreelance.API.business.profile.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionDTO {
    private int percentage;
    private List<CompletionItemDTO> items;
    private List<String> suggestions;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompletionItemDTO {
    private String label;
    private String key;
    private boolean completed;
}
