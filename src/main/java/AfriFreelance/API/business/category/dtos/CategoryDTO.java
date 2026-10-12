package AfriFreelance.API.business.category.dtos;

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
public class CategoryDTO {
    private UUID id;
    private String name;
    private String description;
    private String iconName;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
