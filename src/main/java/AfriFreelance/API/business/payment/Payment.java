package AfriFreelance.API.business.payment;

import AfriFreelance.API.business.contract.Contract;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.PaymentStatus;
import AfriFreelance.API.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 100)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id", nullable = false)
    private Contract contract;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payer_id", nullable = false)
    private User payer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payee_id")
    private User payee;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 20)
    private PaymentType paymentType;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Currency currency = Currency.XOF;

    @Column(name = "commission_amount")
    private Double commissionAmount;

    @Column(name = "net_amount")
    private Double netAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "provider_name", length = 50)
    private String providerName;

    @Column(name = "provider_transaction_id", length = 200)
    private String providerTransactionId;

    @Column(name = "checkout_url")
    private String checkoutUrl;

    @Column(name = "provider_session_id", length = 255)
    private String providerSessionId;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "webhook_verified")
    @Builder.Default
    private Boolean webhookVerified = false;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "released_at")
    private LocalDateTime releasedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = PaymentStatus.PENDING;
        if (currency == null) currency = Currency.XOF;
        if (webhookVerified == null) webhookVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
