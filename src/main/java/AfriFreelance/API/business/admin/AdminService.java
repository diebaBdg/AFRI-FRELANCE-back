package AfriFreelance.API.business.admin;

import AfriFreelance.API.business.admin.dtos.*;
import AfriFreelance.API.business.contract.Contract;
import AfriFreelance.API.business.contract.ContractRepository;
import AfriFreelance.API.business.freelanceprofile.FreelanceProfileRepository;
import AfriFreelance.API.business.clientprofile.ClientProfileRepository;
import AfriFreelance.API.business.moderation.ReportRepository;
import AfriFreelance.API.business.payment.Payment;
import AfriFreelance.API.business.payment.PaymentRepository;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.project.Project;
import AfriFreelance.API.business.project.ProjectRepository;
import AfriFreelance.API.business.proposal.ProposalRepository;
import AfriFreelance.API.business.user.Role;
import AfriFreelance.API.business.user.RoleRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.PaymentStatus;
import AfriFreelance.API.enums.ProjectStatus;
import AfriFreelance.API.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProjectRepository projectRepository;
    private final ProposalRepository proposalRepository;
    private final ContractRepository contractRepository;
    private final PaymentRepository paymentRepository;
    private final ReportRepository reportRepository;
    private final FreelanceProfileRepository freelanceProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final ProfileService profileService;

    private UUID requireAdmin() {
        UUID userId = profileService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getCode()));
        if (!isAdmin) {
            throw new BusinessException("NOT_ADMIN", "Accès réservé aux administrateurs", userId);
        }
        return userId;
    }

    // ===== STATISTICS =====

    @Transactional(readOnly = true)
    public AdminStatsDTO getPlatformStats() {
        requireAdmin();

        long totalUsers = userRepository.count();
        long totalFreelancers = freelanceProfileRepository.count();
        long totalClients = clientProfileRepository.count();

        long totalProjects = projectRepository.count();
        long openProjects = projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.OPEN, Pageable.unpaged()).getTotalElements();
        long inProgressProjects = projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.IN_PROGRESS, Pageable.unpaged()).getTotalElements();
        long completedProjects = projectRepository.findByStatusOrderByCreatedAtDesc(ProjectStatus.COMPLETED, Pageable.unpaged()).getTotalElements();

        long totalProposals = proposalRepository.count();
        long totalContracts = contractRepository.count();
        long activeContracts = contractRepository.findAll().stream()
                .filter(c -> c.getStatus() != null && c.getStatus().name().equals("ACTIVE"))
                .count();

        List<Payment> allPayments = paymentRepository.findAll();
        long totalPayments = allPayments.size();
        double totalVolume = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.RELEASED)
                .mapToDouble(Payment::getAmount)
                .sum();
        double totalCommission = allPayments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.RELEASED)
                .mapToDouble(p -> p.getCommissionAmount() != null ? p.getCommissionAmount() : 0.0)
                .sum();

        long totalReports = reportRepository.count();
        long pendingReports = reportRepository.findByStatusOrderByCreatedAtDesc(
                AfriFreelance.API.enums.ReportStatus.PENDING).size();

        return AdminStatsDTO.builder()
                .totalUsers(totalUsers)
                .totalFreelancers(totalFreelancers)
                .totalClients(totalClients)
                .totalProjects(totalProjects)
                .openProjects(openProjects)
                .inProgressProjects(inProgressProjects)
                .completedProjects(completedProjects)
                .totalProposals(totalProposals)
                .totalContracts(totalContracts)
                .activeContracts(activeContracts)
                .totalPayments(totalPayments)
                .totalVolume(totalVolume)
                .totalCommission(totalCommission)
                .totalReports(totalReports)
                .pendingReports(pendingReports)
                .build();
    }

    // ===== USER MANAGEMENT =====

    @Transactional(readOnly = true)
    public List<AdminUserDTO> getAllUsers(String search, String role, String status) {
        requireAdmin();

        return userRepository.findAll().stream()
                .filter(u -> {
                    if (status != null && !status.isBlank()) {
                        if (!u.getStatus().name().equalsIgnoreCase(status)) return false;
                    }
                    if (role != null && !role.isBlank()) {
                        boolean hasRole = u.getRoles().stream()
                                .anyMatch(r -> r.getCode().equalsIgnoreCase(role));
                        if (!hasRole) return false;
                    }
                    if (search != null && !search.isBlank()) {
                        String q = search.toLowerCase();
                        boolean matchName = u.getFullName() != null && u.getFullName().toLowerCase().contains(q);
                        boolean matchEmail = u.getEmail() != null && u.getEmail().toLowerCase().contains(q);
                        boolean matchUsername = u.getUsername() != null && u.getUsername().toLowerCase().contains(q);
                        if (!matchName && !matchEmail && !matchUsername) return false;
                    }
                    return true;
                })
                .map(this::toUserDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AdminUserDTO getUserDetail(UUID userId) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        return toUserDTO(user);
    }

    public AdminUserDTO updateUserStatus(UUID userId, String statusStr) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        UserStatus newStatus = UserStatus.valueOf(statusStr.toUpperCase());
        user.setStatus(newStatus);
        userRepository.save(user);

        log.info("Admin a changé le statut de l'utilisateur {} vers {}", userId, newStatus);
        return toUserDTO(user);
    }

    public AdminUserDTO updateUserRoles(UUID userId, List<String> roleCodes) {
        requireAdmin();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        Set<Role> newRoles = new HashSet<>();
        for (String code : roleCodes) {
            Role role = roleRepository.findByCode(code.toUpperCase())
                    .orElseThrow(() -> new BusinessException("ROLE_NOT_FOUND", "Rôle non trouvé: " + code, code));
            newRoles.add(role);
        }
        user.setRoles(newRoles);
        userRepository.save(user);

        log.info("Admin a modifié les rôles de l'utilisateur {} vers {}", userId, roleCodes);
        return toUserDTO(user);
    }

    // ===== PROJECT MANAGEMENT =====

    @Transactional(readOnly = true)
    public List<AdminProjectDTO> getAllProjects(String status, String category) {
        requireAdmin();

        return projectRepository.findAll().stream()
                .filter(p -> {
                    if (status != null && !status.isBlank()) {
                        if (!p.getStatus().name().equalsIgnoreCase(status)) return false;
                    }
                    if (category != null && !category.isBlank()) {
                        if (p.getCategory() == null || !p.getCategory().equalsIgnoreCase(category)) return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toProjectDTO)
                .collect(Collectors.toList());
    }

    public AdminProjectDTO updateProjectStatus(UUID projectId, String statusStr) {
        requireAdmin();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));

        ProjectStatus newStatus = ProjectStatus.valueOf(statusStr.toUpperCase());
        project.setStatus(newStatus);
        projectRepository.save(project);

        log.info("Admin a forcé le statut du projet {} vers {}", projectId, newStatus);
        return toProjectDTO(project);
    }

    public void deleteProject(UUID projectId) {
        requireAdmin();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));
        projectRepository.delete(project);
        log.info("Admin a supprimé le projet {}", projectId);
    }

    // ===== TRANSACTION MANAGEMENT =====

    @Transactional(readOnly = true)
    public List<AdminTransactionDTO> getAllTransactions(String status, String type) {
        requireAdmin();

        return paymentRepository.findAll().stream()
                .filter(p -> {
                    if (status != null && !status.isBlank()) {
                        if (p.getStatus() == null || !p.getStatus().name().equalsIgnoreCase(status)) return false;
                    }
                    if (type != null && !type.isBlank()) {
                        if (p.getPaymentType() == null || !p.getPaymentType().name().equalsIgnoreCase(type)) return false;
                    }
                    return true;
                })
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toTransactionDTO)
                .collect(Collectors.toList());
    }

    // ===== MAPPERS =====

    private AdminUserDTO toUserDTO(User u) {
        return AdminUserDTO.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .fullName(u.getFullName())
                .phone(u.getPhone())
                .status(u.getStatus() != null ? u.getStatus().name() : "ACTIF")
                .activeMode(u.getActiveMode() != null ? u.getActiveMode().name() : "FREELANCE")
                .avatarUrl(u.getAvatarUrl())
                .headline(u.getHeadline())
                .country(u.getCountry())
                .city(u.getCity())
                .roles(u.getRoles().stream().map(Role::getCode).collect(Collectors.toList()))
                .hasFreelanceProfile(freelanceProfileRepository.existsByUserId(u.getId()))
                .hasClientProfile(clientProfileRepository.existsByUserId(u.getId()))
                .memberSince(u.getDateCreation())
                .build();
    }

    private AdminProjectDTO toProjectDTO(Project p) {
        return AdminProjectDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .status(p.getStatus() != null ? p.getStatus().name() : "OPEN")
                .category(p.getCategory())
                .clientId(p.getClient().getId())
                .clientName(p.getClient().getFullName())
                .budgetMin(p.getBudgetMin())
                .budgetMax(p.getBudgetMax())
                .currency(p.getCurrency() != null ? p.getCurrency().name() : "XOF")
                .proposalCount(p.getProposalCount())
                .assignedFreelanceId(p.getAssignedFreelanceId())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private AdminTransactionDTO toTransactionDTO(Payment p) {
        String projectTitle = null;
        String assignedFreelanceName = null;
        if (p.getContract() != null && p.getContract().getProposal() != null
                && p.getContract().getProposal().getProject() != null) {
            projectTitle = p.getContract().getProposal().getProject().getTitle();
        }

        return AdminTransactionDTO.builder()
                .id(p.getId())
                .transactionRef(p.getTransactionRef())
                .contractId(p.getContract() != null ? p.getContract().getId() : null)
                .projectTitle(projectTitle)
                .payerId(p.getPayer() != null ? p.getPayer().getId() : null)
                .payerName(p.getPayer() != null ? p.getPayer().getFullName() : null)
                .payeeId(p.getPayee() != null ? p.getPayee().getId() : null)
                .payeeName(p.getPayee() != null ? p.getPayee().getFullName() : null)
                .paymentType(p.getPaymentType() != null ? p.getPaymentType().name() : null)
                .amount(p.getAmount())
                .currency(p.getCurrency() != null ? p.getCurrency().name() : "XOF")
                .commissionAmount(p.getCommissionAmount())
                .netAmount(p.getNetAmount())
                .status(p.getStatus() != null ? p.getStatus().name() : "PENDING")
                .providerName(p.getProviderName())
                .providerTransactionId(p.getProviderTransactionId())
                .createdAt(p.getCreatedAt())
                .paidAt(p.getPaidAt())
                .releasedAt(p.getReleasedAt())
                .build();
    }
}
