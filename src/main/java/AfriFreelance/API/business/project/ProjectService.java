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

import java.time.LocalDate;
import java.util.Locale;
import java.util.Map;
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

    private static final Map<String, ProjectType> PROJECT_TYPE_ALIASES = Map.of(
            "fixed", ProjectType.FIXED_PRICE,
            "fixedprice", ProjectType.FIXED_PRICE,
            "fixed_price", ProjectType.FIXED_PRICE,
            "monthly", ProjectType.HOURLY,
            "hourly", ProjectType.HOURLY,
            "horaire", ProjectType.HOURLY
    );

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    public ProjectDTO createProject(ProjectRequest request) {
        UUID clientId = profileService.getCurrentUserId();
        User client = userRepository.findById(clientId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", clientId));

        validateBudget(request);
        ProjectType projectType = resolveProjectType(request.getProjectType());

        Project project = Project.builder()
                .client(client)
                .title(request.getTitle())
                .description(request.getDescription())
                .projectType(projectType)
                .budgetMin(request.getBudgetMin())
                .budgetMax(request.getBudgetMax())
                .currency(resolveCurrency(request.getCurrency(), client))
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

    private ProjectType resolveProjectType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return ProjectType.FIXED_PRICE;
        }
        String normalized = rawType.trim().toLowerCase(Locale.ROOT).replace('-', '_');
        if (PROJECT_TYPE_ALIASES.containsKey(normalized)) {
            return PROJECT_TYPE_ALIASES.get(normalized);
        }
        throw new BusinessException("INVALID_PROJECT_TYPE",
                "Type de projet inconnu: " + rawType
                        + ". Valeurs acceptées: fixed, monthly (ou FIXED_PRICE, HOURLY)",
                rawType);
    }

    private void validateBudget(ProjectRequest request) {
        if (request.getBudgetMin() != null && request.getBudgetMin() < 0) {
            throw new BusinessException("INVALID_BUDGET", "Le budget minimum ne peut pas être négatif", request.getBudgetMin());
        }
        if (request.getBudgetMax() != null && request.getBudgetMax() < 0) {
            throw new BusinessException("INVALID_BUDGET", "Le budget maximum ne peut pas être négatif", request.getBudgetMax());
        }
        if (request.getBudgetMin() != null && request.getBudgetMax() != null
                && request.getBudgetMin() > request.getBudgetMax()) {
            throw new BusinessException("INVALID_BUDGET",
                    "Le budget minimum ne peut pas dépasser le budget maximum",
                    Map.of("budgetMin", request.getBudgetMin(), "budgetMax", request.getBudgetMax()));
        }
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new BusinessException("INVALID_DEADLINE",
                    "La date limite ne peut pas être antérieure à la date du jour", deadline.toString());
        }
    }

    private Currency resolveCurrency(String rawCurrency, User client) {
        if (rawCurrency == null || rawCurrency.isBlank()) {
            return client.getDefaultCurrency();
        }
        try {
            return Currency.valueOf(rawCurrency.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("INVALID_CURRENCY",
                    "Devise inconnue: " + rawCurrency, rawCurrency);
        }
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

        validateBudget(request);
        validateDeadline(request.getExpectedStartDate());

        if (request.getTitle() != null) project.setTitle(request.getTitle());
        if (request.getDescription() != null) project.setDescription(request.getDescription());
        if (request.getProjectType() != null) project.setProjectType(resolveProjectType(request.getProjectType()));
        if (request.getBudgetMin() != null) project.setBudgetMin(request.getBudgetMin());
        if (request.getBudgetMax() != null) project.setBudgetMax(request.getBudgetMax());
        if (request.getCurrency() != null) {
            project.setCurrency(resolveCurrency(request.getCurrency(), project.getClient()));
        }
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
        Page<Project> projectPage = projectRepository.searchProjects(
                ProjectStatus.OPEN,
                query == null ? "" : query,
                category == null ? "" : category,
                country == null ? "" : country,
                pageable);

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
