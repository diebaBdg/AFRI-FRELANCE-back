package AfriFreelance.API.business.payment;

import AfriFreelance.API.auth.SecurityUserPrincipal;
import AfriFreelance.API.business.contract.Contract;
import AfriFreelance.API.business.contract.ContractRepository;
import AfriFreelance.API.business.notification2.NotificationService;
import AfriFreelance.API.business.payment.dtos.*;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.NotificationType;
import AfriFreelance.API.enums.PaymentStatus;
import AfriFreelance.API.enums.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ContractRepository contractRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    public PaymentDTO initiateEscrow(PaymentInitRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User payer = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", request.getContractId()));

        if (!contract.getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_CONTRACT_CLIENT", "Seul le client peut initier le paiement", userId);
        }

        if (paymentRepository.findByContractIdAndPaymentType(contract.getId(), PaymentType.ESCROW_DEPOSIT).isPresent()) {
            throw new BusinessException("ESCROW_ALREADY_EXISTS", "Un dépôt escrow existe déjà pour ce contrat", contract.getId());
        }

        Double commission = contract.getCommissionAmount() != null ? contract.getCommissionAmount() : 0.0;
        Double netAmount = contract.getAmount() - commission;

        String txRef = "ESC-" + contract.getId().toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis();

        Payment payment = Payment.builder()
                .transactionRef(txRef)
                .contract(contract)
                .payer(payer)
                .payee(contract.getFreelance())
                .paymentType(PaymentType.ESCROW_DEPOSIT)
                .amount(contract.getAmount())
                .currency(contract.getCurrency() != null ? contract.getCurrency() : Currency.XOF)
                .commissionAmount(commission)
                .netAmount(netAmount)
                .status(PaymentStatus.PENDING)
                .providerName(request.getProviderName())
                .build();

        payment = paymentRepository.save(payment);

        log.info("Escrow initié: {} pour le contrat {}", txRef, contract.getId());
        return toDTO(payment);
    }

    public PaymentDTO confirmPayment(UUID paymentId, PaymentActionRequest request) {
        UUID userId = profileService.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement non trouvé", paymentId));

        if (!payment.getPayer().getId().equals(userId)) {
            throw new BusinessException("NOT_PAYER", "Seul le payeur peut confirmer le paiement", userId);
        }

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("PAYMENT_NOT_PENDING", "Ce paiement n'est plus en attente", paymentId);
        }

        payment.setStatus(PaymentStatus.ESCROWED);
        payment.setProviderTransactionId(request.getProviderTransactionId());
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        notificationService.sendNotification(
                payment.getPayee().getId(),
                NotificationType.PAYMENT_CONFIRMED,
                "Paiement escrow confirmé pour \"" + payment.getContract().getProposal().getProject().getTitle() + "\"",
                payment.getId()
        );

        return toDTO(payment);
    }

    public PaymentDTO releasePayment(UUID paymentId) {
        UUID userId = profileService.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement non trouvé", paymentId));

        if (!payment.getContract().getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Seul le client peut libérer le paiement", userId);
        }

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new BusinessException("NOT_ESCROWED", "Le paiement n'est pas en séquestre", paymentId);
        }

        payment.setStatus(PaymentStatus.RELEASED);
        payment.setReleasedAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        Contract contract = payment.getContract();
        contract.setIsPaid(true);
        contractRepository.save(contract);

        notificationService.sendNotification(
                payment.getPayee().getId(),
                NotificationType.PAYMENT_RELEASED,
                "Paiement libéré pour \"" + contract.getProposal().getProject().getTitle() + "\"",
                payment.getId()
        );

        return toDTO(payment);
    }

    public PaymentDTO refundPayment(UUID paymentId) {
        UUID userId = profileService.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement non trouvé", paymentId));

        if (!payment.getContract().getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Seul le client peut demander un remboursement", userId);
        }

        if (payment.getStatus() != PaymentStatus.ESCROWED) {
            throw new BusinessException("NOT_ESCROWED", "Le paiement n'est pas en séquestre", paymentId);
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        notificationService.sendNotification(
                payment.getPayee().getId(),
                NotificationType.PAYMENT_RELEASED,
                "Paiement remboursé pour \"" + payment.getContract().getProposal().getProject().getTitle() + "\"",
                payment.getId()
        );

        return toDTO(payment);
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getMyPayments() {
        UUID userId = profileService.getCurrentUserId();
        List<Payment> asPayer = paymentRepository.findByPayerIdOrderByCreatedAtDesc(userId);
        List<Payment> asPayee = paymentRepository.findByPayeeIdOrderByCreatedAtDesc(userId);

        List<Payment> all = new java.util.ArrayList<>(asPayer);
        all.addAll(asPayee);
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        return all.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PaymentDTO> getContractPayments(UUID contractId) {
        UUID userId = profileService.getCurrentUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));

        if (!contract.getClient().getId().equals(userId) && !contract.getFreelance().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas partie à ce contrat", userId);
        }

        return paymentRepository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentDTO getPayment(UUID paymentId) {
        UUID userId = profileService.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement non trouvé", paymentId));

        if (!payment.getPayer().getId().equals(userId) && (payment.getPayee() == null || !payment.getPayee().getId().equals(userId))) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas autorisé à voir ce paiement", userId);
        }

        return toDTO(payment);
    }

    private PaymentDTO toDTO(Payment p) {
        return PaymentDTO.builder()
                .id(p.getId())
                .transactionRef(p.getTransactionRef())
                .contractId(p.getContract().getId())
                .payerId(p.getPayer().getId())
                .payerName(p.getPayer().getFullName())
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
                .paidAt(p.getPaidAt())
                .releasedAt(p.getReleasedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
