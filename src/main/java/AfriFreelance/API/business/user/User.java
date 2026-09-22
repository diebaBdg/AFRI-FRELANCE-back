package AfriFreelance.API.business.user;

import AfriFreelance.API.enums.ActiveMode;
import AfriFreelance.API.enums.Currency;
import AfriFreelance.API.enums.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "td_users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(length = 255)
    private String adresse;

    @Column(length = 20)
    private String phone;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIF;

    @Enumerated(EnumType.STRING)
    @Column(name = "active_mode", length = 20)
    @Builder.Default
    private ActiveMode activeMode = ActiveMode.FREELANCE;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(length = 200)
    private String headline;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_currency", length = 10)
    @Builder.Default
    private Currency defaultCurrency = Currency.XOF;

    @Column(length = 50)
    @Builder.Default
    private String timezone = "Africa/Dakar";

    @Column(name = "primary_language", length = 50)
    @Builder.Default
    private String primaryLanguage = "Français";

    @Column(name = "member_since_year")
    private Integer memberSinceYear;

    @Column(name = "last_password_change")
    private LocalDateTime lastPasswordChange;

    @Column(name = "total_assigned")
    @Builder.Default
    private Integer totalAssigned = 0;

    @Column(name = "total_approved")
    @Builder.Default
    private Integer totalApproved = 0;

    @Column(name = "total_rejected")
    @Builder.Default
    private Integer totalRejected = 0;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "td_user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        dateModification = LocalDateTime.now();
        if (status == null) status = UserStatus.ACTIF;
        if (activeMode == null) activeMode = ActiveMode.FREELANCE;
        if (defaultCurrency == null) defaultCurrency = Currency.XOF;
        if (totalAssigned == null) totalAssigned = 0;
        if (totalApproved == null) totalApproved = 0;
        if (totalRejected == null) totalRejected = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }
}
