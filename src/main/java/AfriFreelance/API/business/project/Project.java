package AfriFreelance.API.business.project;

import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.ProjectStatus;
import AfriFreelance.API.enums.ProjectType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private User client;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "project_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ProjectType projectType = ProjectType.FIXED_PRICE;

    @Column(name = "budget_min")
    private Double budgetMin;

    @Column(name = "budget_max")
    private Double budgetMax;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Currency currency = Currency.XOF;

    @Column(name = "estimated_duration_days")
    private Integer estimatedDurationDays;

    @Column(name = "expected_start_date")
    private LocalDate expectedStartDate;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "skills_required", columnDefinition = "TEXT[]")
    private String[] skillsRequired;

    @Column(length = 100)
    private String country;

    @Column(name = "is_remote")
    @Builder.Default
    private Boolean isRemote = true;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    @Builder.Default
    private ProjectStatus status = ProjectStatus.OPEN;

    @Column(name = "proposal_count")
    @Builder.Default
    private Integer proposalCount = 0;

    @Column(name = "assigned_freelance_id")
    private UUID assignedFreelanceId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = ProjectStatus.OPEN;
        if (projectType == null) projectType = ProjectType.FIXED_PRICE;
        if (currency == null) currency = Currency.XOF;
        if (isRemote == null) isRemote = true;
        if (proposalCount == null) proposalCount = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
