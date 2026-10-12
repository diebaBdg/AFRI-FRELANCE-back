package AfriFreelance.API.business.category.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100)
    private String name;

    @Size(max = 200)
    private String description;

    @Size(max = 50)
    private String iconName;

    private Boolean isActive;

    private Integer displayOrder;
}
