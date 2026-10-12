package AfriFreelance.API.business.proposal;

import AfriFreelance.API.business.project.Project;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.ProposalStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_proposals",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "freelance_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Proposal {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_id", nullable = false)
    private User freelance;

    @Column(nullable = false)
    private Double proposedAmount;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Currency currency = Currency.XOF;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    @Column(name = "attachment_url")
    private String attachmentUrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private ProposalStatus status = ProposalStatus.PENDING;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ProposalStatus.PENDING;
        if (currency == null) currency = Currency.XOF;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
