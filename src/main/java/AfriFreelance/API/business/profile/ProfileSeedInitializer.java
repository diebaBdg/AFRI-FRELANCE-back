package AfriFreelance.API.business.profile;

import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.enums.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Component
@Order(20)
@RequiredArgsConstructor
@Transactional
public class ProfileSeedInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FreelanceProfileRepository freelanceProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;
    private final VerificationRepository verificationRepository;
    private final BadgeRepository badgeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedSarrBloom();
        seedAwaDiop();
        seedKofiMensah();
        seedFatouNdiaye();
        log.info("Profile seed data initialized");
    }

    private void seedSarrBloom() {
        Optional<User> existing = userRepository.findByEmail("sarrbloom@gmail.com");
        if (existing.isPresent() && freelanceProfileRepository.existsByUserId(existing.get().getId())) {
            return;
        }

        User user = existing.orElseGet(() -> {
            User u = User.builder()
                    .username("sarrbloom")
                    .email("sarrbloom@gmail.com")
                    .fullName("Sarr Bloom")
                    .phone("771234567")
                    .password(passwordEncoder.encode("Password123!"))
                    .status(UserStatus.ACTIF)
                    .activeMode(ActiveMode.FREELANCE)
                    .avatarUrl(null)
                    .headline("Développeur Full-Stack Java / Spring Boot")
                    .bio("Ingénieur en informatique spécialisé dans le développement d'applications web et mobiles.")
                    .country("Sénégal")
                    .city("Dakar")
                    .defaultCurrency(Currency.USD)
                    .timezone("Africa/Dakar")
                    .primaryLanguage("Français")
                    .memberSinceYear(2026)
                    .build();
            return userRepository.save(u);
        });

        FreelanceProfile fp = FreelanceProfile.builder()
                .user(user)
                .title("Développeur Full-Stack Java / Spring Boot")
                .overview("Ingénieur en informatique spécialisé dans le développement d'applications web et mobiles. Passionné par les architectures robustes et les APIs performantes. 5+ ans d'expérience avec Spring Boot, React et PostgreSQL.")
                .country("Sénégal")
                .city("Dakar")
                .hourlyRate(35.0)
                .dailyRate(280.0)
                .minProjectRate(500.0)
                .currency(Currency.USD)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .availabilityType(AvailabilityType.FULL_TIME)
                .weeklyHours(40)
                .profileActive(true)
                .completionPercentage(85)
                .isVerified(true)
                .build();
        fp = freelanceProfileRepository.save(fp);

        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Java").level(SkillLevel.EXPERT).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Spring Boot").level(SkillLevel.EXPERT).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("React").level(SkillLevel.ADVANCED).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("PostgreSQL").level(SkillLevel.ADVANCED).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Docker").level(SkillLevel.INTERMEDIATE).build());

        fp.getExperiences().add(WorkExperience.builder()
                .freelanceProfile(fp)
                .jobTitle("Full-Stack Developer")
                .company("Gaindé 2000")
                .country("Sénégal")
                .startDate(LocalDate.of(2023, 1, 1))
                .current(true)
                .description("Développement et maintenance d'applications critiques pour l'administration sénégalaise.")
                .build());
        fp.getExperiences().add(WorkExperience.builder()
                .freelanceProfile(fp)
                .jobTitle("Backend Developer")
                .company("Sonatel")
                .country("Sénégal")
                .startDate(LocalDate.of(2020, 6, 1))
                .endDate(LocalDate.of(2022, 12, 31))
                .description("Conception d'APIs REST et microservices pour les services mobiles.")
                .build());

        fp.getEducations().add(Education.builder()
                .freelanceProfile(fp)
                .institution("Ecole Supérieure Polytechnique de Dakar")
                .degree("Ingénieur")
                .field("Informatique")
                .startDate(LocalDate.of(2016, 10, 1))
                .endDate(LocalDate.of(2020, 7, 31))
                .description("Option génie logiciel et bases de données.")
                .build());

        fp.getCertifications().add(Certification.builder()
                .freelanceProfile(fp)
                .name("Oracle Certified Professional, Java SE 17 Developer")
                .issuer("Oracle")
                .issueDate(LocalDate.of(2023, 5, 15))
                .verificationUrl("https://oracle.com/verify/12345")
                .credentialId("OCP-12345")
                .build());

        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Français").level(LanguageLevel.NATIF).build());
        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Anglais").level(LanguageLevel.AVANCE).build());
        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Wolof").level(LanguageLevel.NATIF).build());

        fp.getPortfolioItems().add(PortfolioItem.builder()
                .freelanceProfile(fp)
                .title("AfriFreelance Platform")
                .description("Plateforme de freelancing pour le marché africain.")
                .technologies(new String[]{"Next.js", "Spring Boot", "PostgreSQL"})
                .projectDate(LocalDate.of(2025, 6, 1))
                .role("Lead Developer")
                .build());

        fp.getProfessionalLinks().add(ProfessionalLink.builder().freelanceProfile(fp).platform(PlatformType.GITHUB).url("https://github.com/sarrbloom").build());
        fp.getProfessionalLinks().add(ProfessionalLink.builder().freelanceProfile(fp).platform(PlatformType.LINKEDIN).url("https://linkedin.com/in/sarrbloom").build());

        freelanceProfileRepository.save(fp);

        ClientProfile cp = ClientProfile.builder()
                .user(user)
                .displayName("Sarr Bloom")
                .clientType(ClientType.INDIVIDUAL)
                .overview("Client particulier cherchant des freelances pour des projets tech.")
                .country("Sénégal")
                .city("Dakar")
                .profileActive(true)
                .projectsPublished(4)
                .projectsCompleted(3)
                .hireRate(78.0)
                .avgRating(4.8)
                .totalSpent(12500.0)
                .build();
        clientProfileRepository.save(cp);

        createVerification(user, VerificationType.EMAIL, VerificationStatus.VERIFIED);
        createVerification(user, VerificationType.PHONE, VerificationStatus.VERIFIED);
        createVerification(user, VerificationType.IDENTITY, VerificationStatus.VERIFIED);
        createBadge(user, BadgeType.EMAIL_VERIFIED);
        createBadge(user, BadgeType.PHONE_VERIFIED);
        createBadge(user, BadgeType.IDENTITY_VERIFIED);
        createBadge(user, BadgeType.TOP_FREELANCER);
    }

    private void seedAwaDiop() {
        Optional<User> existing = userRepository.findByEmail("awadiop@gmail.com");
        if (existing.isPresent() && freelanceProfileRepository.existsByUserId(existing.get().getId())) {
            return;
        }

        User user = existing.orElseGet(() -> {
            User u = User.builder()
                    .username("awadiop")
                    .email("awadiop@gmail.com")
                    .fullName("Awa Diop")
                    .phone("779876543")
                    .password(passwordEncoder.encode("Password123!"))
                    .status(UserStatus.ACTIF)
                    .activeMode(ActiveMode.CLIENT)
                    .headline("Product Owner - Fintech")
                    .bio("Product owner dans une fintech, à la recherche de talents créatifs.")
                    .country("Sénégal")
                    .city("Dakar")
                    .defaultCurrency(Currency.XOF)
                    .timezone("Africa/Dakar")
                    .primaryLanguage("Français")
                    .memberSinceYear(2026)
                    .build();
            return userRepository.save(u);
        });

        ClientProfile cp = ClientProfile.builder()
                .user(user)
                .displayName("Awa Diop")
                .clientType(ClientType.COMPANY)
                .overview("Responsable produit chez PayDakar, une solution de paiement mobile.")
                .country("Sénégal")
                .city("Dakar")
                .profileActive(true)
                .projectsPublished(12)
                .projectsCompleted(10)
                .hireRate(85.0)
                .avgRating(4.9)
                .totalSpent(450000.0)
                .build();
        clientProfileRepository.save(cp);

        Company company = Company.builder()
                .ownerId(user.getId())
                .legalName("PayDakar SARL")
                .commercialName("PayDakar")
                .sector("Fintech")
                .size(CompanySize.SME)
                .country("Sénégal")
                .city("Dakar")
                .website("https://paydakar.com")
                .description("Solution de paiement mobile pour l'Afrique de l'Ouest.")
                .isVerified(true)
                .build();
        company = companyRepository.save(company);
        cp.setCompanyId(company.getId());
        clientProfileRepository.save(cp);

        companyMemberRepository.save(CompanyMember.builder()
                .company(company)
                .user(user)
                .role(CompanyRole.OWNER)
                .build());

        createVerification(user, VerificationType.EMAIL, VerificationStatus.VERIFIED);
        createVerification(user, VerificationType.PHONE, VerificationStatus.VERIFIED);
        createBadge(user, BadgeType.EMAIL_VERIFIED);
        createBadge(user, BadgeType.PHONE_VERIFIED);
    }

    private void seedKofiMensah() {
        Optional<User> existing = userRepository.findByEmail("kofimensah@gmail.com");
        if (existing.isPresent() && freelanceProfileRepository.existsByUserId(existing.get().getId())) {
            return;
        }

        User user = existing.orElseGet(() -> {
            User u = User.builder()
                    .username("kofimensah")
                    .email("kofimensah@gmail.com")
                    .fullName("Kofi Mensah")
                    .phone("0555123456")
                    .password(passwordEncoder.encode("Password123!"))
                    .status(UserStatus.ACTIF)
                    .activeMode(ActiveMode.FREELANCE)
                    .headline("UI/UX Designer & Frontend Developer")
                    .bio("Designer passionné par les expériences digitales africaines.")
                    .country("Côte d'Ivoire")
                    .city("Abidjan")
                    .defaultCurrency(Currency.EUR)
                    .timezone("Africa/Abidjan")
                    .primaryLanguage("Français")
                    .memberSinceYear(2025)
                    .build();
            return userRepository.save(u);
        });

        FreelanceProfile fp = FreelanceProfile.builder()
                .user(user)
                .title("UI/UX Designer & Frontend Developer")
                .overview("Designer produit avec 4 ans d'expérience. Je crée des interfaces modernes, accessibles et centrées utilisateur. Spécialisé dans le design system et les applications React.")
                .country("Côte d'Ivoire")
                .city("Abidjan")
                .hourlyRate(25.0)
                .dailyRate(200.0)
                .minProjectRate(300.0)
                .currency(Currency.EUR)
                .availabilityStatus(AvailabilityStatus.SOON)
                .availabilityType(AvailabilityType.PART_TIME)
                .weeklyHours(20)
                .profileActive(true)
                .completionPercentage(65)
                .isVerified(false)
                .build();
        fp = freelanceProfileRepository.save(fp);

        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Figma").level(SkillLevel.EXPERT).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("React").level(SkillLevel.ADVANCED).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Tailwind CSS").level(SkillLevel.ADVANCED).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("TypeScript").level(SkillLevel.INTERMEDIATE).build());

        fp.getExperiences().add(WorkExperience.builder()
                .freelanceProfile(fp)
                .jobTitle("Product Designer")
                .company("Cocobod Digital")
                .country("Côte d'Ivoire")
                .startDate(LocalDate.of(2022, 3, 1))
                .current(true)
                .description("Design d'applications agritech pour les coopératives de cacao.")
                .build());

        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Français").level(LanguageLevel.COURANT).build());
        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Anglais").level(LanguageLevel.INTERMEDIAIRE).build());
        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Baoulé").level(LanguageLevel.NATIF).build());

        fp.getPortfolioItems().add(PortfolioItem.builder()
                .freelanceProfile(fp)
                .title("CocoaTrace App")
                .description("Application de traçabilité du cacao pour les producteurs ivoiriens.")
                .technologies(new String[]{"Figma", "React Native"})
                .projectDate(LocalDate.of(2024, 2, 1))
                .role("Lead Designer")
                .build());

        fp.getProfessionalLinks().add(ProfessionalLink.builder().freelanceProfile(fp).platform(PlatformType.BEHANCE).url("https://behance.net/kofimensah").build());
        fp.getProfessionalLinks().add(ProfessionalLink.builder().freelanceProfile(fp).platform(PlatformType.DRIBBBLE).url("https://dribbble.com/kofimensah").build());

        freelanceProfileRepository.save(fp);

        createVerification(user, VerificationType.EMAIL, VerificationStatus.VERIFIED);
        createBadge(user, BadgeType.EMAIL_VERIFIED);
    }

    private void seedFatouNdiaye() {
        Optional<User> existing = userRepository.findByEmail("fatoundiaye@gmail.com");
        if (existing.isPresent() && freelanceProfileRepository.existsByUserId(existing.get().getId())) {
            return;
        }

        User user = existing.orElseGet(() -> {
            User u = User.builder()
                    .username("fatoundiaye")
                    .email("fatoundiaye@gmail.com")
                    .fullName("Fatou Ndiaye")
                    .phone("776543210")
                    .password(passwordEncoder.encode("Password123!"))
                    .status(UserStatus.ACTIF)
                    .activeMode(ActiveMode.FREELANCE)
                    .headline("Data Scientist & ML Engineer")
                    .bio("Data scientist spécialisée en machine learning et IA appliquée.")
                    .country("Sénégal")
                    .city("Thiès")
                    .defaultCurrency(Currency.USD)
                    .timezone("Africa/Dakar")
                    .primaryLanguage("Français")
                    .memberSinceYear(2026)
                    .build();
            return userRepository.save(u);
        });

        FreelanceProfile fp = FreelanceProfile.builder()
                .user(user)
                .title("Data Scientist & ML Engineer")
                .overview("Data scientist avec une maîtrise de Python, TensorFlow et les pipelines de données. J'aide les entreprises à tirer parti de leurs données pour prendre de meilleures décisions.")
                .country("Sénégal")
                .city("Thiès")
                .hourlyRate(40.0)
                .dailyRate(320.0)
                .minProjectRate(800.0)
                .currency(Currency.USD)
                .availabilityStatus(AvailabilityStatus.AVAILABLE)
                .availabilityType(AvailabilityType.PER_PROJECT)
                .weeklyHours(15)
                .profileActive(true)
                .completionPercentage(72)
                .isVerified(false)
                .build();
        fp = freelanceProfileRepository.save(fp);

        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Python").level(SkillLevel.EXPERT).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("TensorFlow").level(SkillLevel.ADVANCED).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("SQL").level(SkillLevel.ADVANCED).build());
        fp.getSkills().add(Skill.builder().freelanceProfile(fp).name("Tableau").level(SkillLevel.INTERMEDIATE).build());

        fp.getEducations().add(Education.builder()
                .freelanceProfile(fp)
                .institution("Université Cheikh Anta Diop")
                .degree("Master")
                .field("Data Science")
                .startDate(LocalDate.of(2018, 10, 1))
                .endDate(LocalDate.of(2020, 7, 31))
                .build());

        fp.getCertifications().add(Certification.builder()
                .freelanceProfile(fp)
                .name("Google Data Analytics Professional Certificate")
                .issuer("Google")
                .issueDate(LocalDate.of(2022, 8, 1))
                .build());

        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Français").level(LanguageLevel.COURANT).build());
        fp.getLanguages().add(Language.builder().freelanceProfile(fp).languageName("Anglais").level(LanguageLevel.AVANCE).build());

        freelanceProfileRepository.save(fp);

        createVerification(user, VerificationType.EMAIL, VerificationStatus.VERIFIED);
        createVerification(user, VerificationType.PHONE, VerificationStatus.VERIFIED);
        createBadge(user, BadgeType.EMAIL_VERIFIED);
        createBadge(user, BadgeType.PHONE_VERIFIED);
    }

    private void createVerification(User user, VerificationType type, VerificationStatus status) {
        if (verificationRepository.findByUserIdAndVerificationType(user.getId(), type).isEmpty()) {
            verificationRepository.save(Verification.builder()
                    .user(user)
                    .verificationType(type)
                    .status(status)
                    .verifiedAt(status == VerificationStatus.VERIFIED ? java.time.LocalDateTime.now() : null)
                    .build());
        }
    }

    private void createBadge(User user, BadgeType type) {
        if (badgeRepository.findByUserIdAndBadgeType(user.getId(), type).isEmpty()) {
            badgeRepository.save(Badge.builder().user(user).badgeType(type).build());
        }
    }
}
