package AfriFreelance.API.business.contract;

import AfriFreelance.API.business.proposal.Proposal;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.ContractStatus;
import AfriFreelance.API.enums.Currency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "td_contracts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposal_id", nullable = false, unique = true)
    private Proposal proposal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_id", nullable = false)
    private User freelance;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Currency currency = Currency.XOF;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "estimated_days")
    private Integer estimatedDays;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ContractStatus status = ContractStatus.ACTIVE;

    @Column(name = "is_paid")
    @Builder.Default
    private Boolean isPaid = false;

    @Column(name = "commission_rate")
    @Builder.Default
    private Double commissionRate = 10.0;

    @Column(name = "commission_amount")
    private Double commissionAmount;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Delivery> deliveries = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ContractStatus.ACTIVE;
        if (currency == null) currency = Currency.XOF;
        if (isPaid == null) isPaid = false;
        if (commissionRate == null) commissionRate = 10.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
