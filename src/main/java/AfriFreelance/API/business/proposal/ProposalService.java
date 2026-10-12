package AfriFreelance.API.business.proposal;

import AfriFreelance.API.business.contract.ContractService;
import AfriFreelance.API.business.notification2.NotificationService;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.project.Project;
import AfriFreelance.API.business.project.ProjectRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.proposal.dtos.*;
import AfriFreelance.API.business.contract.dtos.ContractDTO;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.NotificationType;
import AfriFreelance.API.enums.ProjectStatus;
import AfriFreelance.API.enums.ProposalStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProposalService {

    private final ProposalRepository proposalRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;
    private final ContractService contractService;

    public ProposalDTO createProposal(UUID projectId, ProposalRequest request) {
        UUID freelanceId = profileService.getCurrentUserId();
        User freelance = userRepository.findById(freelanceId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", freelanceId));

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));

        if (project.getStatus() != ProjectStatus.OPEN) {
            throw new BusinessException("PROJECT_NOT_OPEN", "Ce projet n'accepte plus de candidatures", projectId);
        }

        if (project.getClient().getId().equals(freelanceId)) {
            throw new BusinessException("CANNOT_PROPOSE_OWN_PROJECT", "Vous ne pouvez pas candidater à votre propre projet", null);
        }

        if (proposalRepository.existsByProjectIdAndFreelanceId(projectId, freelanceId)) {
            throw new BusinessException("ALREADY_PROPOSED", "Vous avez déjà candidaté à ce projet", projectId);
        }

        Proposal proposal = Proposal.builder()
                .project(project)
                .freelance(freelance)
                .proposedAmount(request.getProposedAmount())
                .currency(request.getCurrency() != null ? Currency.valueOf(request.getCurrency()) : project.getCurrency())
                .estimatedDurationDays(request.getEstimatedDurationDays())
                .coverLetter(request.getCoverLetter())
                .attachmentUrl(request.getAttachmentUrl())
                .status(ProposalStatus.PENDING)
                .build();

        proposal = proposalRepository.save(proposal);

        project.setProposalCount(project.getProposalCount() + 1);
        projectRepository.save(project);

        notificationService.sendNotification(
                project.getClient().getId(),
                NotificationType.PROPOSAL_RECEIVED,
                "Nouvelle candidature sur \"" + project.getTitle() + "\"",
                proposal.getId()
        );

        return toDTO(proposal);
    }

    @Transactional(readOnly = true)
    public List<ProposalDTO> getProposalsForProject(UUID projectId) {
        UUID userId = profileService.getCurrentUserId();
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", projectId));

        if (!project.getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_PROJECT_OWNER", "Seul le client peut voir les candidatures", userId);
        }

        return proposalRepository.findByProjectIdOrderByCreatedAtAsc(projectId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProposalDTO> getMyProposals() {
        UUID freelanceId = profileService.getCurrentUserId();
        return proposalRepository.findByFreelanceIdOrderByCreatedAtDesc(freelanceId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProposalDTO getProposal(UUID proposalId) {
        UUID userId = profileService.getCurrentUserId();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException("PROPOSAL_NOT_FOUND", "Candidature non trouvée", proposalId));

        boolean isFreelance = proposal.getFreelance().getId().equals(userId);
        boolean isClient = proposal.getProject().getClient().getId().equals(userId);

        if (!isFreelance && !isClient) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas autorisé à voir cette candidature", userId);
        }

        return toDTO(proposal);
    }

    public ProposalDTO updateProposalStatus(UUID proposalId, String statusStr) {
        UUID userId = profileService.getCurrentUserId();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException("PROPOSAL_NOT_FOUND", "Candidature non trouvée", proposalId));

        ProposalStatus newStatus = ProposalStatus.valueOf(statusStr.toUpperCase());

        if (newStatus == ProposalStatus.ACCEPTED) {
            if (!proposal.getProject().getClient().getId().equals(userId)) {
                throw new BusinessException("NOT_PROJECT_OWNER", "Seul le client peut accepter une candidature", userId);
            }

            proposal.setStatus(ProposalStatus.ACCEPTED);
            proposal = proposalRepository.save(proposal);

            Project project = proposal.getProject();
            project.setStatus(ProjectStatus.ASSIGNED);
            project.setAssignedFreelanceId(proposal.getFreelance().getId());
            projectRepository.save(project);

            proposalRepository.findByProjectIdOrderByCreatedAtAsc(project.getId()).stream()
                    .filter(p -> !p.getId().equals(proposal.getId()) && p.getStatus() == ProposalStatus.PENDING)
                    .forEach(p -> {
                        p.setStatus(ProposalStatus.REJECTED);
                        proposalRepository.save(p);
                        notificationService.sendNotification(
                                p.getFreelance().getId(),
                                NotificationType.PROPOSAL_REJECTED,
                                "Candidature refusée pour \"" + project.getTitle() + "\"",
                                p.getId()
                        );
                    });

            notificationService.sendNotification(
                    proposal.getFreelance().getId(),
                    NotificationType.PROPOSAL_ACCEPTED,
                    "Candidature acceptée pour \"" + project.getTitle() + "\"",
                    proposal.getId()
            );

            ContractDTO contract = contractService.createContractFromProposal(proposal);
            log.info("Contrat créé: {} pour la proposition {}", contract.getId(), proposal.getId());

        } else if (newStatus == ProposalStatus.REJECTED) {
            if (!proposal.getProject().getClient().getId().equals(userId)) {
                throw new BusinessException("NOT_PROJECT_OWNER", "Seul le client peut refuser une candidature", userId);
            }
            proposal.setStatus(ProposalStatus.REJECTED);
            proposal = proposalRepository.save(proposal);

            notificationService.sendNotification(
                    proposal.getFreelance().getId(),
                    NotificationType.PROPOSAL_REJECTED,
                    "Candidature refusée pour \"" + proposal.getProject().getTitle() + "\"",
                    proposal.getId()
            );

        } else if (newStatus == ProposalStatus.WITHDRAWN) {
            if (!proposal.getFreelance().getId().equals(userId)) {
                throw new BusinessException("NOT_PROPOSAL_OWNER", "Vous ne pouvez retirer que vos propres candidatures", userId);
            }
            proposal.setStatus(ProposalStatus.WITHDRAWN);
            proposal = proposalRepository.save(proposal);

            Project project = proposal.getProject();
            project.setProposalCount(Math.max(0, project.getProposalCount() - 1));
            projectRepository.save(project);
        }

        return toDTO(proposal);
    }

    public void deleteProposal(UUID proposalId) {
        UUID userId = profileService.getCurrentUserId();
        Proposal proposal = proposalRepository.findById(proposalId)
                .orElseThrow(() -> new BusinessException("PROPOSAL_NOT_FOUND", "Candidature non trouvée", proposalId));

        if (!proposal.getFreelance().getId().equals(userId)) {
            throw new BusinessException("NOT_PROPOSAL_OWNER", "Vous ne pouvez supprimer que vos propres candidatures", userId);
        }

        if (proposal.getStatus() == ProposalStatus.ACCEPTED) {
            throw new BusinessException("CANNOT_DELETE_ACCEPTED", "Vous ne pouvez pas supprimer une candidature acceptée", proposalId);
        }

        proposalRepository.delete(proposal);

        Project project = proposal.getProject();
        project.setProposalCount(Math.max(0, project.getProposalCount() - 1));
        projectRepository.save(project);
    }

    private ProposalDTO toDTO(Proposal p) {
        return ProposalDTO.builder()
                .id(p.getId())
                .projectId(p.getProject().getId())
                .projectTitle(p.getProject().getTitle())
                .freelanceId(p.getFreelance().getId())
                .freelanceName(p.getFreelance().getFullName())
                .freelanceAvatarUrl(p.getFreelance().getAvatarUrl())
                .freelanceHeadline(p.getFreelance().getHeadline())
                .proposedAmount(p.getProposedAmount())
                .currency(p.getCurrency() != null ? p.getCurrency().name() : "XOF")
                .estimatedDurationDays(p.getEstimatedDurationDays())
                .coverLetter(p.getCoverLetter())
                .attachmentUrl(p.getAttachmentUrl())
                .status(p.getStatus() != null ? p.getStatus().name() : "PENDING")
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
