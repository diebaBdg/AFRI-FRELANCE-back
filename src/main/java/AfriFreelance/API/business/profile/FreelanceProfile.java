package AfriFreelance.API.business.profile;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.AvailabilityStatus;
import AfriFreelance.API.enums.AvailabilityType;
import AfriFreelance.API.enums.Currency;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "td_freelance_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreelanceProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String city;

    @Column(name = "hourly_rate")
    private Double hourlyRate;

    @Column(name = "daily_rate")
    private Double dailyRate;

    @Column(name = "min_project_rate")
    private Double minProjectRate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private Currency currency = Currency.XOF;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_status", length = 20)
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.AVAILABLE;

    @Enumerated(EnumType.STRING)
    @Column(name = "availability_type", length = 20)
    @Builder.Default
    private AvailabilityType availabilityType = AvailabilityType.FULL_TIME;

    @Column(name = "weekly_hours")
    private Integer weeklyHours;

    @Column(name = "profile_active")
    @Builder.Default
    private Boolean profileActive = true;

    @Column(name = "completion_percentage")
    @Builder.Default
    private Integer completionPercentage = 0;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<WorkExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Education> educations = new ArrayList<>();

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Certification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Language> languages = new HashSet<>();

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<PortfolioItem> portfolioItems = new ArrayList<>();

    @OneToMany(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private Set<ProfessionalLink> professionalLinks = new HashSet<>();

    @OneToOne(mappedBy = "freelanceProfile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private WorkPreference workPreference;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (completionPercentage == null) completionPercentage = 0;
        if (profileActive == null) profileActive = true;
        if (isVerified == null) isVerified = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
