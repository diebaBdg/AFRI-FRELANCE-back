package AfriFreelance.API.business.notification;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.business.user.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_notification_preferences",
        uniqueConstraints = @UniqueConstraint(columnNames = "user_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email_messages")
    @Builder.Default
    private Boolean emailMessages = true;

    @Column(name = "email_project_updates")
    @Builder.Default
    private Boolean emailProjectUpdates = true;

    @Column(name = "email_marketing")
    @Builder.Default
    private Boolean emailMarketing = false;

    @Column(name = "push_messages")
    @Builder.Default
    private Boolean pushMessages = true;

    @Column(name = "push_project_updates")
    @Builder.Default
    private Boolean pushProjectUpdates = true;

    @Column(name = "push_contract_updates")
    @Builder.Default
    private Boolean pushContractUpdates = true;

    @Column(name = "push_payment_updates")
    @Builder.Default
    private Boolean pushPaymentUpdates = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
