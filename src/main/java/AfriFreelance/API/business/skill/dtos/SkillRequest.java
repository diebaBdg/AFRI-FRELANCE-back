package AfriFreelance.API.business.skill.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SkillRequest {

    @NotBlank(message = "Le nom de la compétence est obligatoire")
    @Size(max = 100)
    private String name;

    private String level;
}
