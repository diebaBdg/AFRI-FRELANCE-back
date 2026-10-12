package AfriFreelance.API.business.project.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectListDTO {
    private List<ProjectDTO> projects;
    private long total;
    private int page;
    private int size;
    private int totalPages;
}
