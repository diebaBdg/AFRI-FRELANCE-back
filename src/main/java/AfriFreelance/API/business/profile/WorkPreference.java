package AfriFreelance.API.business.profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_work_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_profile_id", nullable = false, unique = true)
    private FreelanceProfile freelanceProfile;

    @Column(name = "work_types", columnDefinition = "TEXT[]")
    private String[] workTypes;

    @Column(name = "project_types", columnDefinition = "TEXT[]")
    private String[] projectTypes;

    @Column(name = "accepted_countries", columnDefinition = "TEXT[]")
    private String[] acceptedCountries;

    @Column(length = 50)
    private String timezone;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
