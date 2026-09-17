package AfriFreelance.API.business.profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.enums.LanguageLevel;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_languages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Language {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "freelance_profile_id", nullable = false)
    private FreelanceProfile freelanceProfile;

    @Column(name = "language_name", nullable = false, length = 100)
    private String languageName;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Builder.Default
    private LanguageLevel level = LanguageLevel.INTERMEDIAIRE;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (level == null) level = LanguageLevel.INTERMEDIAIRE;
    }
}
