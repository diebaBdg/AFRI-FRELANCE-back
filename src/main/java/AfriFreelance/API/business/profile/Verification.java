package AfriFreelance.API.business.profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.VerificationStatus;
import AfriFreelance.API.enums.VerificationType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_verifications",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "verification_type"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_type", nullable = false, length = 20)
    private VerificationType verificationType;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (status == null) status = VerificationStatus.PENDING;
    }
}
