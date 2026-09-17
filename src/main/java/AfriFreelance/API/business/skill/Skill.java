package AfriFreelance.API.business.skill;

import AfriFreelance.API.business.freelanceprofile.FreelanceProfile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.enums.SkillLevel;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_skills")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_profile_id", nullable = false)
    private FreelanceProfile freelanceProfile;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private SkillLevel level = SkillLevel.INTERMEDIATE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (level == null) level = SkillLevel.INTERMEDIATE;
    }
}
