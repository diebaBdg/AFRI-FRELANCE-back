package AfriFreelance.API.business.contract;

import AfriFreelance.API.business.notification2.NotificationService;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.proposal.Proposal;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.contract.dtos.*;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.ContractStatus;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.DeliveryStatus;
import AfriFreelance.API.enums.NotificationType;
import AfriFreelance.API.enums.ProjectStatus;
import AfriFreelance.API.business.project.ProjectRepository;
import AfriFreelance.API.business.project.Project;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ContractService {

    private final ContractRepository contractRepository;
    private final DeliveryRepository deliveryRepository;
    private final ProjectRepository projectRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    public ContractDTO createContractFromProposal(Proposal proposal) {
        Contract contract = Contract.builder()
                .proposal(proposal)
                .client(proposal.getProject().getClient())
                .freelance(proposal.getFreelance())
                .amount(proposal.getProposedAmount())
                .currency(proposal.getCurrency() != null ? proposal.getCurrency() : Currency.XOF)
                .estimatedDays(proposal.getEstimatedDurationDays())
                .startDate(LocalDate.now())
                .status(ContractStatus.ACTIVE)
                .commissionRate(10.0)
                .commissionAmount(proposal.getProposedAmount() * 0.10)
                .build();

        contract = contractRepository.save(contract);

        Project project = proposal.getProject();
        project.setStatus(ProjectStatus.IN_PROGRESS);
        projectRepository.save(project);

        notificationService.sendNotification(
                proposal.getFreelance().getId(),
                NotificationType.CONTRACT_CREATED,
                "Contrat créé pour \"" + project.getTitle() + "\"",
                contract.getId()
        );

        notificationService.sendNotification(
                proposal.getProject().getClient().getId(),
                NotificationType.CONTRACT_CREATED,
                "Contrat créé pour \"" + project.getTitle() + "\"",
                contract.getId()
        );

        return toDTO(contract);
    }

    @Transactional(readOnly = true)
    public List<ContractDTO> getMyContracts() {
        UUID userId = profileService.getCurrentUserId();
        return contractRepository.findByClientIdOrFreelanceIdOrderByCreatedAtDesc(userId, userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ContractDTO getContract(UUID contractId) {
        UUID userId = profileService.getCurrentUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));

        if (!contract.getClient().getId().equals(userId) && !contract.getFreelance().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas partie à ce contrat", userId);
        }

        return toDTO(contract);
    }

    public ContractDTO updateContractStatus(UUID contractId, String statusStr) {
        UUID userId = profileService.getCurrentUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));

        ContractStatus newStatus = ContractStatus.valueOf(statusStr.toUpperCase());

        boolean isClient = contract.getClient().getId().equals(userId);
        boolean isFreelance = contract.getFreelance().getId().equals(userId);

        if (!isClient && !isFreelance) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas partie à ce contrat", userId);
        }

        if (newStatus == ContractStatus.COMPLETED) {
            contract.setStatus(ContractStatus.COMPLETED);
            contract = contractRepository.save(contract);

            Project project = contract.getProposal().getProject();
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);

            notificationService.sendNotification(
                    contract.getClient().getId(),
                    NotificationType.CONTRACT_COMPLETED,
                    "Contrat terminé pour \"" + project.getTitle() + "\"",
                    contract.getId()
            );

        } else if (newStatus == ContractStatus.CANCELLED) {
            contract.setStatus(ContractStatus.CANCELLED);
            contract = contractRepository.save(contract);

            Project project = contract.getProposal().getProject();
            project.setStatus(ProjectStatus.CANCELLED);
            projectRepository.save(project);

        } else if (newStatus == ContractStatus.PAUSED || newStatus == ContractStatus.DISPUTED) {
            contract.setStatus(newStatus);
            contract = contractRepository.save(contract);
        }

        return toDTO(contract);
    }

    public DeliveryDTO submitDelivery(UUID contractId, DeliveryRequest request) {
        UUID userId = profileService.getCurrentUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));

        if (!contract.getFreelance().getId().equals(userId)) {
            throw new BusinessException("NOT_FREELANCE", "Seul le freelance peut soumettre un livrable", userId);
        }

        if (contract.getStatus() != ContractStatus.ACTIVE) {
            throw new BusinessException("CONTRACT_NOT_ACTIVE", "Le contrat doit être actif", contractId);
        }

        Delivery delivery = Delivery.builder()
                .contract(contract)
                .description(request.getDescription())
                .attachmentUrl(request.getAttachmentUrl())
                .status(DeliveryStatus.SUBMITTED)
                .build();

        delivery = deliveryRepository.save(delivery);

        notificationService.sendNotification(
                contract.getClient().getId(),
                NotificationType.DELIVERY_SUBMITTED,
                "Nouveau livrable sur \"" + contract.getProposal().getProject().getTitle() + "\"",
                delivery.getId()
        );

        return toDeliveryDTO(delivery);
    }

    public DeliveryDTO approveDelivery(UUID contractId, DeliveryReviewRequest request) {
        UUID userId = profileService.getCurrentUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));

        if (!contract.getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_CLIENT", "Seul le client peut valider un livrable", userId);
        }

        Delivery delivery = deliveryRepository.findByContractIdOrderBySubmittedAtDesc(contractId).stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException("NO_DELIVERY", "Aucun livrable trouvé", contractId));

        DeliveryStatus newStatus = DeliveryStatus.valueOf(request.getStatus().toUpperCase());
        delivery.setStatus(newStatus);
        delivery.setClientFeedback(request.getFeedback());
        delivery.setReviewedAt(LocalDateTime.now());
        delivery = deliveryRepository.save(delivery);

        if (newStatus == DeliveryStatus.APPROVED) {
            contract.setStatus(ContractStatus.COMPLETED);
            contract.setIsPaid(true);
            contractRepository.save(contract);

            Project project = contract.getProposal().getProject();
            project.setStatus(ProjectStatus.COMPLETED);
            projectRepository.save(project);

            notificationService.sendNotification(
                    contract.getFreelance().getId(),
                    NotificationType.DELIVERY_APPROVED,
                    "Livrable approuvé sur \"" + project.getTitle() + "\"",
                    delivery.getId()
            );
        } else if (newStatus == DeliveryStatus.REJECTED || newStatus == DeliveryStatus.REVISION_REQUESTED) {
            notificationService.sendNotification(
                    contract.getFreelance().getId(),
                    newStatus == DeliveryStatus.REJECTED ? NotificationType.DELIVERY_REJECTED : NotificationType.DELIVERY_REJECTED,
                    newStatus == DeliveryStatus.REJECTED
                            ? "Livrable refusé sur \"" + contract.getProposal().getProject().getTitle() + "\""
                            : "Révision demandée sur \"" + contract.getProposal().getProject().getTitle() + "\"",
                    delivery.getId()
            );
        }

        return toDeliveryDTO(delivery);
    }

    private ContractDTO toDTO(Contract c) {
        return ContractDTO.builder()
                .id(c.getId())
                .proposalId(c.getProposal().getId())
                .projectId(c.getProposal().getProject().getId())
                .projectTitle(c.getProposal().getProject().getTitle())
                .clientId(c.getClient().getId())
                .clientName(c.getClient().getFullName())
                .freelanceId(c.getFreelance().getId())
                .freelanceName(c.getFreelance().getFullName())
                .amount(c.getAmount())
                .currency(c.getCurrency() != null ? c.getCurrency().name() : "XOF")
                .startDate(c.getStartDate())
                .endDate(c.getEndDate())
                .estimatedDays(c.getEstimatedDays())
                .status(c.getStatus() != null ? c.getStatus().name() : "ACTIVE")
                .isPaid(c.getIsPaid())
                .commissionRate(c.getCommissionRate())
                .commissionAmount(c.getCommissionAmount())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .deliveries(c.getDeliveries().stream().map(this::toDeliveryDTO).collect(Collectors.toList()))
                .build();
    }

    private DeliveryDTO toDeliveryDTO(Delivery d) {
        return DeliveryDTO.builder()
                .id(d.getId())
                .contractId(d.getContract().getId())
                .description(d.getDescription())
                .attachmentUrl(d.getAttachmentUrl())
                .status(d.getStatus() != null ? d.getStatus().name() : "SUBMITTED")
                .clientFeedback(d.getClientFeedback())
                .submittedAt(d.getSubmittedAt())
                .reviewedAt(d.getReviewedAt())
                .build();
    }
}
