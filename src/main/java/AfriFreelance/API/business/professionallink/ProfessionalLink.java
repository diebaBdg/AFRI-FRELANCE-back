package AfriFreelance.API.business.professionallink;

import AfriFreelance.API.business.freelanceprofile.FreelanceProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.enums.PlatformType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_professional_links")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionalLink {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_profile_id", nullable = false)
    private FreelanceProfile freelanceProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlatformType platform;

    @Column(nullable = false, length = 500)
    private String url;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
