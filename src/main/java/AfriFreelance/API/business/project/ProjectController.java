package AfriFreelance.API.business.project;

import AfriFreelance.API.business.project.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projets & Missions", description = "Publication, recherche et gestion des missions")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @Operation(summary = "Publier une nouvelle mission")
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody ProjectRequest request) {
        return new ResponseEntity<>(projectService.createProject(request), HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Rechercher des missions (avec filtres)")
    public ResponseEntity<ProjectListDTO> searchProjects(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(projectService.searchProjects(query, category, country, page, size));
    }

    @GetMapping("/{projectId}")
    @Operation(summary = "Consulter le détail d'une mission")
    public ResponseEntity<ProjectDTO> getProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    @PutMapping("/{projectId}")
    @Operation(summary = "Modifier une mission")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable UUID projectId,
                                                      @Valid @RequestBody ProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    @Operation(summary = "Supprimer une mission")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir mes missions publiées")
    public ResponseEntity<List<ProjectDTO>> getMyProjects() {
        return ResponseEntity.ok(projectService.getMyProjects());
    }
}
