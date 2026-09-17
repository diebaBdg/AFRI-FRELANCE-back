package AfriFreelance.API.business.profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.ClientType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_client_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", length = 200)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "client_type", length = 20)
    @Builder.Default
    private ClientType clientType = ClientType.INDIVIDUAL;

    @Column(name = "company_id")
    private UUID companyId;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "profile_active")
    @Builder.Default
    private Boolean profileActive = true;

    @Column(name = "projects_published")
    @Builder.Default
    private Integer projectsPublished = 0;

    @Column(name = "projects_completed")
    @Builder.Default
    private Integer projectsCompleted = 0;

    @Column(name = "hire_rate")
    @Builder.Default
    private Double hireRate = 0.0;

    @Column(name = "avg_rating")
    @Builder.Default
    private Double avgRating = 0.0;

    @Column(name = "total_spent")
    @Builder.Default
    private Double totalSpent = 0.0;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (profileActive == null) profileActive = true;
        if (projectsPublished == null) projectsPublished = 0;
        if (projectsCompleted == null) projectsCompleted = 0;
        if (hireRate == null) hireRate = 0.0;
        if (avgRating == null) avgRating = 0.0;
        if (totalSpent == null) totalSpent = 0.0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
