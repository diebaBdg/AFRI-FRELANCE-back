package AfriFreelance.API.business.project;

import AfriFreelance.API.business.notification2.NotificationService;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.project.dtos.*;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.NotificationType;
import AfriFreelance.API.enums.ProjectStatus;
import AfriFreelance.API.enums.ProjectType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    public ProjectDTO createProject(ProjectRequest request) {
        UUID clientId = profileService.getCurrentUserId();
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", clientId));

        Project project = Project.builder()
                .client(client)
                .title(request.getTitle())
                .description(request.getDescription())
                .projectType(request.getProjectType() != null ? ProjectType.valueOf(request.getProjectType()) : ProjectType.FIXED_PRICE)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .currency(request.getCurrency() != null ? Currency.valueOf(request.getCurrency()) : client.getDefaultCurrency())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .expectedStartDate(request.getExpectedStartDate())
                .category(request.getCategory())
                .skillsRequired(request.getSkillsRequired())
                .country(request.getCountry() != null ? request.getCountry() : client.getCountry())
                .isRemote(request.getIsRemote() != null ? request.getIsRemote() : true)
                .status(ProjectStatus.OPEN)
                .build();

        project = projectRepository.save(project);
        return toDTO(project);
    }

    public ProjectDTO updateProject(UUID projectId, ProjectRequest request) {
        UUID clientId = profileService.getCurrentUserId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));

        if (!project.getClient().getId().equals(clientId)) {
            throw new BusinessException("NOT_PROJECT_OWNER", "Vous n'êtes pas le propriétaire de ce projet", clientId);
        }

        if (project.getStatus() != ProjectStatus.OPEN && project.getStatus() != ProjectStatus.DRAFT) {
            throw new BusinessException("PROJECT_NOT_EDITABLE", "Ce projet ne peut plus être modifié", projectId);
        }

        if (request.getTitle() != null) project.setTitle(request.getTitle());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getProjectType() != null) project.setProjectType(ProjectType.valueOf(request.getProjectType()));
        if (request.getBudgetMin() != null) project.setBudgetMin(request.getBudgetMin());
        if (request.getBudgetMax() != null) project.setBudgetMax(request.getBudgetMax());
        if (request.getCurrency() != null) project.setCurrency(Currency.valueOf(request.getCurrency()));
        if (request.getEstimatedDurationDays() != null) project.setEstimatedDurationDays(request.getEstimatedDurationDays());
        if (request.getExpectedStartDate() != null) project.setExpectedStartDate(request.getExpectedStartDate());
        if (request.getCategory() != null) project.setCategory(request.getCategory());
        if (request.getSkillsRequired() != null) project.setSkillsRequired(request.getSkillsRequired());
        if (request.getCountry() != null) project.setCountry(request.getCountry());
        if (request.getIsRemote() != null) project.setIsRemote(request.getIsRemote());

        project = projectRepository.save(project);
        return toDTO(project);
    }

    public void deleteProject(UUID projectId) {
        UUID clientId = profileService.getCurrentUserId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));

        if (!project.getClient().getId().equals(clientId)) {
            throw new BusinessException("NOT_PROJECT_OWNER", "Vous n'êtes pas le propriétaire de ce projet", clientId);
        }

        if (project.getStatus() == ProjectStatus.IN_PROGRESS || project.getStatus() == ProjectStatus.COMPLETED) {
            throw new BusinessException("PROJECT_NOT_DELETABLE", "Ce projet ne peut pas être supprimé", projectId);
        }

        projectRepository.delete(project);
    }

    @Transactional(readOnly = true)
    public ProjectDTO getProject(UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));
        return toDTO(project);
    }

    @Transactional(readOnly = true)
    public ProjectListDTO searchProjects(String query, String category, String country, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Project> projectPage = projectRepository.searchProjects(ProjectStatus.OPEN, query, category, country, pageable);

        List<ProjectDTO> dtos = projectPage.getContent().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());

        return ProjectListDTO.builder()
                .projects(dtos)
                .total(projectPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(projectPage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public List<ProjectDTO> getMyProjects() {
        UUID clientId = profileService.getCurrentUserId();
        return projectRepository.findByClientIdOrderByCreatedAtDesc(clientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private ProjectDTO toDTO(Project p) {
        return ProjectDTO.builder()
                .id(p.getId())
                .clientId(p.getClient().getId())
                .clientName(p.getClient().getFullName())
                .clientAvatarUrl(p.getClient().getAvatarUrl())
                .title(p.getTitle())
                .description(p.getDescription())
                .projectType(p.getProjectType() != null ? p.getProjectType().name() : "FIXED_PRICE")
                .budgetMin(p.getBudgetMin())
                .budgetMax(p.getBudgetMax())
                .currency(p.getCurrency() != null ? p.getCurrency().name() : "XOF")
                .estimatedDurationDays(p.getEstimatedDurationDays())
                .expectedStartDate(p.getExpectedStartDate())
                .category(p.getCategory())
                .skillsRequired(p.getSkillsRequired())
                .country(p.getCountry())
                .isRemote(p.getIsRemote())
                .status(p.getStatus() != null ? p.getStatus().name() : "OPEN")
                .proposalCount(p.getProposalCount())
                .assignedFreelanceId(p.getAssignedFreelanceId())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
