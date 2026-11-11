package AfriFreelance.API.business.payment;

import AfriFreelance.API.business.contract.Contract;
import AfriFreelance.API.business.contract.ContractRepository;
import AfriFreelance.API.business.notification2.NotificationService;
import AfriFreelance.API.business.payment.dtos.*;
import AfriFreelance.API.business.payment.provider.CheckoutInitRequest;
import AfriFreelance.API.business.payment.provider.PayDunyaSoftPay;
import AfriFreelance.API.business.payment.provider.PaymentProviderFactory;
import AfriFreelance.API.business.payment.provider.PaymentProviderService;
import AfriFreelance.API.business.payment.provider.ProviderInitResponse;
import AfriFreelance.API.business.payment.provider.SoftPayResult;
import AfriFreelance.API.business.payment.provider.WebhookResult;
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
import org.springframework.beans.factory.annotation.Value;
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
    private final PaymentProviderFactory providerFactory;

    @Value("${paydunya.sandbox:true}")
    private boolean sandbox;

    @Value("${paydunya.channels:wave-senegal,orange-money-senegal}")
    private String channels;

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    public PaymentDTO initiateEscrow(PaymentInitRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User payer = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        Contract contract = contractRepository.findById(request.getContractId())
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", request.getContractId()));

        if (!contract.getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_CONTRACT_CLIENT", "Seul le client peut initier le paiement", userId);
        }

        var existing = paymentRepository.findByContractIdAndPaymentType(contract.getId(), PaymentType.ESCROW_DEPOSIT);
        if (existing.isPresent()) {
            Payment current = existing.get();
            if (current.getStatus() == PaymentStatus.ESCROWED || current.getStatus() == PaymentStatus.RELEASED) {
                throw new BusinessException("ESCROW_ALREADY_EXISTS", "Un dépôt escrow existe déjà pour ce contrat", contract.getId());
            }
            return refreshCheckout(current, payer, request);
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
                .customerPhone(firstNonBlank(request.getCustomerPhone(), payer.getPhone()))
                .build();

        payment = paymentRepository.save(payment);
        return createCheckout(payment, payer, request);

    }

    private PaymentDTO refreshCheckout(Payment payment, User payer, PaymentInitRequest request) {
        payment.setStatus(PaymentStatus.PENDING);
        payment.setProviderName(request.getProviderName());
        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            payment.setCustomerPhone(request.getCustomerPhone());
        }
        payment = paymentRepository.save(payment);
        return createCheckout(payment, payer, request);
    }

    private PaymentDTO createCheckout(Payment payment, User payer, PaymentInitRequest request) {
        Contract contract = payment.getContract();
        String projectTitle = contract.getProposal() != null && contract.getProposal().getProject() != null
                ? contract.getProposal().getProject().getTitle()
                : "Contrat AfriFreelance";

        PaymentProviderService provider = providerFactory.getProvider(request.getProviderName());
        ProviderInitResponse providerResponse = provider.initiateCheckout(
                CheckoutInitRequest.builder()
                        .transactionRef(payment.getTransactionRef())
                        .amount(payment.getAmount())
                        .currency(payment.getCurrency())
                        .customerPhone(firstNonBlank(request.getCustomerPhone(), payment.getCustomerPhone(), payer.getPhone()))
                        .customerName(payer.getFullName())
                        .customerEmail(payer.getEmail())
                        .itemName(projectTitle)
                        .description("Escrow AfriFreelance — " + projectTitle)
                        .successUrl(request.getSuccessUrl())
                        .errorUrl(request.getErrorUrl())
                        .channel(request.getChannel())
                        .build()
        );

        payment.setCheckoutUrl(providerResponse.getCheckoutUrl());
        payment.setProviderSessionId(providerResponse.getProviderSessionId());
        payment = paymentRepository.save(payment);

        log.info("Escrow initié: {} pour le contrat {} via {} — checkoutUrl={}",
                payment.getTransactionRef(), contract.getId(), request.getProviderName(), providerResponse.getCheckoutUrl());
        return toDTO(payment);
    }

    public PaymentConfigDTO getConfig() {
        return PaymentConfigDTO.builder()
                .provider("PAYDUNYA")
                .sandbox(sandbox)
                .payoutEnabled(false)
                .ipnPath(contextPath + "/webhooks/payments/paydunya")
                .methods(PayDunyaSoftPay.enabled(channels).stream()
                        .map(m -> PaymentMethodDTO.builder()
                                .id(m.id())
                                .label(m.label())
                                .country(m.country())
                                .softPay(m.softPay())
                                .build())
                        .toList())
                .build();
    }

    public PaymentSoftPayResponse initiateSoftPay(UUID paymentId, PaymentSoftPayRequest request) {
        UUID userId = profileService.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement non trouvé", paymentId));

        if (!payment.getPayer().getId().equals(userId)) {
            throw new BusinessException("NOT_PAYER", "Seul le payeur peut lancer SoftPay", userId);
        }
        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException("PAYMENT_NOT_PENDING", "Ce paiement n'est plus en attente", paymentId);
        }
        if (payment.getProviderSessionId() == null) {
            throw new BusinessException("NO_PROVIDER_SESSION", "Initie d'abord le checkout escrow", paymentId);
        }

        String phone = firstNonBlank(request.getCustomerPhone(), payment.getCustomerPhone(), payment.getPayer().getPhone());
        PaymentProviderService provider = providerFactory.getProvider(payment.getProviderName());
        SoftPayResult result = provider.initiateSoftPay(
                payment.getProviderSessionId(),
                request.getChannel(),
                payment.getPayer().getFullName(),
                payment.getPayer().getEmail(),
                phone
        );

        if (request.getCustomerPhone() != null && !request.getCustomerPhone().isBlank()) {
            payment.setCustomerPhone(request.getCustomerPhone());
            payment = paymentRepository.save(payment);
        }

        return PaymentSoftPayResponse.builder()
                .success(result.isSuccess())
                .message(result.getMessage())
                .url(result.getUrl())
                .omUrl(result.getOmUrl())
                .maxitUrl(result.getMaxitUrl())
                .payment(toDTO(payment))
                .build();
    }

    public PaymentDTO confirmFromInvoiceToken(String invoiceToken) {
        if (invoiceToken == null || invoiceToken.isBlank()) {
            throw new BusinessException("TOKEN_REQUIRED", "Token de facture PayDunya manquant", null);
        }
        Payment payment = paymentRepository.findByProviderSessionId(invoiceToken)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement introuvable pour ce token", invoiceToken));

        PaymentProviderService provider = providerFactory.getProvider(payment.getProviderName());
        boolean verified = provider.verifyPayment(invoiceToken);
        if (!verified) {
            return toDTO(payment);
        }
        if (payment.getStatus() == PaymentStatus.PENDING) {
            WebhookResult result = WebhookResult.builder()
                    .success(true)
                    .invoiceToken(invoiceToken)
                    .transactionRef(payment.getTransactionRef())
                    .providerTransactionId(invoiceToken)
                    .amount(payment.getAmount())
                    .build();
            return confirmPaymentFromWebhook(result);
        }
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

        if (payment.getProviderName() == null || payment.getProviderSessionId() == null) {
            throw new BusinessException("NO_PROVIDER_SESSION", "Aucune session fournisseur à vérifier", paymentId);
        }

        PaymentProviderService provider = providerFactory.getProvider(payment.getProviderName());
        boolean verified = provider.verifyPayment(payment.getProviderSessionId());
        if (!verified) {
            throw new BusinessException(
                    "PAYMENT_NOT_CONFIRMED",
                    "PayDunya n'a pas confirmé ce paiement. Terminez le checkout ou réessayez après le callback IPN.",
                    paymentId
            );
        }

        payment.setStatus(PaymentStatus.ESCROWED);
        payment.setWebhookVerified(true);
        if (request != null && request.getProviderTransactionId() != null) {
            payment.setProviderTransactionId(request.getProviderTransactionId());
        } else if (payment.getProviderTransactionId() == null) {
            payment.setProviderTransactionId(payment.getProviderSessionId());
        }
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

        String providerSessionId = payment.getProviderSessionId();
        if (providerSessionId != null && payment.getProviderName() != null) {
            PaymentProviderService provider = providerFactory.getProvider(payment.getProviderName());
            boolean refunded = provider.refundPayment(providerSessionId, payment.getAmount(), payment.getCurrency());
            if (!refunded) {
                throw new BusinessException(
                        "REFUND_MANUAL_REQUIRED",
                        "PayDunya ne permet pas encore le remboursement automatique. Rembourse le client depuis le dashboard PayDunya, puis mets à jour le statut.",
                        paymentId
                );
            }
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

    public PaymentDTO confirmPaymentFromWebhook(WebhookResult result) {
        Payment payment = findPaymentForWebhook(result);
        String providerTransactionId = result.getProviderTransactionId();

        if (payment.getStatus() != PaymentStatus.PENDING) {
            log.info("Webhook: paiement {} déjà traité (statut {}), ignoré", payment.getTransactionRef(), payment.getStatus());
            return toDTO(payment);
        }

        payment.setStatus(PaymentStatus.ESCROWED);
        payment.setProviderTransactionId(providerTransactionId);
        payment.setWebhookVerified(true);
        payment.setPaidAt(LocalDateTime.now());
        payment = paymentRepository.save(payment);

        notificationService.sendNotification(
                payment.getPayee().getId(),
                NotificationType.PAYMENT_CONFIRMED,
                "Paiement escrow confirmé pour \"" + payment.getContract().getProposal().getProject().getTitle() + "\"",
                payment.getId()
        );

        notificationService.sendNotification(
                payment.getPayer().getId(),
                NotificationType.PAYMENT_CONFIRMED,
                "Paiement escrow confirmé pour \"" + payment.getContract().getProposal().getProject().getTitle() + "\"",
                payment.getId()
        );

        log.info("Paiement {} confirmé via webhook (providerTxId={})", payment.getTransactionRef(), providerTransactionId);
        return toDTO(payment);
    }

    private Payment findPaymentForWebhook(WebhookResult result) {
        if (result.getTransactionRef() != null && !result.getTransactionRef().isBlank()) {
            var byRef = paymentRepository.findByTransactionRef(result.getTransactionRef());
            if (byRef.isPresent()) {
                return byRef.get();
            }
        }
        if (result.getInvoiceToken() != null && !result.getInvoiceToken().isBlank()) {
            var byToken = paymentRepository.findByProviderSessionId(result.getInvoiceToken());
            if (byToken.isPresent()) {
                return byToken.get();
            }
        }
        throw new BusinessException("PAYMENT_NOT_FOUND",
                "Paiement non trouvé pour la référence: " + result.getTransactionRef(),
                result.getTransactionRef());
    }

    public PaymentDTO verifyPayment(UUID paymentId) {
        UUID userId = profileService.getCurrentUserId();
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BusinessException("PAYMENT_NOT_FOUND", "Paiement non trouvé", paymentId));

        if (!payment.getPayer().getId().equals(userId) && !payment.getContract().getClient().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas autorisé", userId);
        }

        if (payment.getProviderName() == null || payment.getProviderSessionId() == null) {
            throw new BusinessException("NO_PROVIDER_SESSION", "Aucune session fournisseur à vérifier", paymentId);
        }

        PaymentProviderService provider = providerFactory.getProvider(payment.getProviderName());
        boolean verified = provider.verifyPayment(payment.getProviderSessionId());

        if (verified && payment.getStatus() == PaymentStatus.PENDING) {
            payment.setStatus(PaymentStatus.ESCROWED);
            payment.setWebhookVerified(true);
            if (payment.getPaidAt() == null) {
                payment.setPaidAt(LocalDateTime.now());
            }
            payment = paymentRepository.save(payment);

            notificationService.sendNotification(
                    payment.getPayee().getId(),
                    NotificationType.PAYMENT_CONFIRMED,
                    "Paiement escrow confirmé pour \"" + payment.getContract().getProposal().getProject().getTitle() + "\"",
                    payment.getId()
            );
        }

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
                .checkoutUrl(p.getCheckoutUrl())
                .providerSessionId(p.getProviderSessionId())
                .customerPhone(p.getCustomerPhone())
                .webhookVerified(p.getWebhookVerified())
                .paidAt(p.getPaidAt())
                .releasedAt(p.getReleasedAt())
                .createdAt(p.getCreatedAt())
                .build();
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
