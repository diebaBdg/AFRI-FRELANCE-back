package AfriFreelance.API.business.profile;

import AfriFreelance.API.auth.SecurityUserPrincipal;
import AfriFreelance.API.business.badge.BadgeRepository;
import AfriFreelance.API.business.certification.Certification;
import AfriFreelance.API.business.clientprofile.ClientProfile;
import AfriFreelance.API.business.clientprofile.ClientProfileRepository;
import AfriFreelance.API.business.favorite.Favorite;
import AfriFreelance.API.business.favorite.FavoriteRepository;
import AfriFreelance.API.business.freelanceprofile.FreelanceProfile;
import AfriFreelance.API.business.freelanceprofile.FreelanceProfileRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.business.verification.VerificationRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.ActiveMode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final FreelanceProfileRepository freelanceProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final VerificationRepository verificationRepository;
    private final BadgeRepository badgeRepository;
    private final FavoriteRepository favoriteRepository;

    public UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            throw new BusinessException("NOT_AUTHENTICATED", "Utilisateur non authentifié", null);
        }
        return principal.getId();
    }

    public ProfileSummaryDTO getMyProfileSummary() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        boolean hasFreelance = freelanceProfileRepository.existsByUserId(userId);
        boolean hasClient = clientProfileRepository.existsByUserId(userId);

        FreelanceProfile fp = freelanceProfileRepository.findByUserId(userId).orElse(null);
        ClientProfile cp = clientProfileRepository.findByUserId(userId).orElse(null);

        List<BadgeDTO> badges = badgeRepository.findByUserId(userId).stream()
                .map(b -> BadgeDTO.builder()
                        .id(b.getId())
                        .badgeType(b.getBadgeType().name())
                        .awardedAt(b.getAwardedAt())
                        .build())
                .collect(Collectors.toList());

        List<String> availableModes = Arrays.asList("FREELANCE", "CLIENT");

        return ProfileSummaryDTO.builder()
                .userId(userId)
                .username(user.getUsername())
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .headline(user.getHeadline())
                .country(user.getCountry())
                .memberSinceYear(user.getMemberSinceYear())
                .activeMode(user.getActiveMode() != null ? user.getActiveMode().name() : "FREELANCE")
                .hasFreelanceProfile(hasFreelance)
                .hasClientProfile(hasClient)
                .freelanceProfileActive(fp != null && fp.getProfileActive())
                .clientProfileActive(cp != null && cp.getProfileActive())
                .freelanceCompletion(fp != null ? fp.getCompletionPercentage() : 0)
                .clientCompletion(cp != null ? 100 : 0)
                .badges(badges)
                .availableModes(availableModes)
                .build();
    }

    public PersonalInfoDTO getMyPersonalInfo() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        PersonalInfoDTO dto = new PersonalInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setHeadline(user.getHeadline());
        dto.setBio(user.getBio());
        dto.setCountry(user.getCountry());
        dto.setCity(user.getCity());
        dto.setTimezone(user.getTimezone());
        dto.setPrimaryLanguage(user.getPrimaryLanguage());
        dto.setAvatarUrl(user.getAvatarUrl());
        dto.setDefaultCurrency(user.getDefaultCurrency() != null ? user.getDefaultCurrency().name() : "XOF");
        dto.setActiveMode(user.getActiveMode() != null ? user.getActiveMode().name() : "FREELANCE");
        dto.setMemberSinceYear(user.getMemberSinceYear());
        return dto;
    }

    public PersonalInfoDTO updateMyPersonalInfo(PersonalInfoRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        if (request.getFullName() != null) user.setFullName(request.getFullName());

        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new BusinessException("DUPLICATE_USERNAME", "Ce nom d'utilisateur est déjà pris", request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        if (request.getHeadline() != null) user.setHeadline(request.getHeadline());
        if (request.getBio() != null) user.setBio(request.getBio());
        if (request.getCountry() != null) user.setCountry(request.getCountry());
        if (request.getCity() != null) user.setCity(request.getCity());
        if (request.getTimezone() != null) user.setTimezone(request.getTimezone());
        if (request.getPrimaryLanguage() != null) user.setPrimaryLanguage(request.getPrimaryLanguage());
        if (request.getAvatarUrl() != null) user.setAvatarUrl(request.getAvatarUrl());
        if (request.getDefaultCurrency() != null) {
            try {
                user.setDefaultCurrency(AfriFreelance.API.enums.Currency.valueOf(request.getDefaultCurrency()));
            } catch (IllegalArgumentException ignored) {}
        }

        userRepository.save(user);
        return getMyPersonalInfo();
    }

    public void updateAvatar(String avatarUrl) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        user.setAvatarUrl(avatarUrl);
        userRepository.save(user);
    }

    public void removeAvatar() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        user.setAvatarUrl(null);
        userRepository.save(user);
    }

    public ProfileSummaryDTO switchActiveMode(ActiveMode mode) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        if (mode == ActiveMode.FREELANCE && !freelanceProfileRepository.existsByUserId(userId)) {
            throw new BusinessException("NO_FREELANCE_PROFILE",
                    "Vous n'avez pas de profil freelance. Créez-en un d'abord.", null);
        }
        if (mode == ActiveMode.CLIENT && !clientProfileRepository.existsByUserId(userId)) {
            throw new BusinessException("NO_CLIENT_PROFILE",
                    "Vous n'avez pas de profil client. Créez-en un d'abord.", null);
        }

        user.setActiveMode(mode);
        userRepository.save(user);
        return getMyProfileSummary();
    }

    public CompletionDTO getMyCompletion() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        FreelanceProfile fp = freelanceProfileRepository.findByUserId(userId).orElse(null);
        if (fp == null) {
            return CompletionDTO.builder()
                    .percentage(0)
                    .items(List.of())
                    .suggestions(List.of("Créez votre profil freelance pour commencer"))
                    .build();
        }

        int count = 0;
        int total = 8;

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) count++;
        if (fp.getTitle() != null && !fp.getTitle().isBlank()) count++;
        if (fp.getOverview() != null && fp.getOverview().length() >= 50) count++;
        if (fp.getSkills() != null && !fp.getSkills().isEmpty()) count++;
        if (fp.getExperiences() != null && !fp.getExperiences().isEmpty()) count++;
        if (fp.getPortfolioItems() != null && !fp.getPortfolioItems().isEmpty()) count++;
        if (fp.getCertifications() != null && !fp.getCertifications().isEmpty()) count++;
        if (Boolean.TRUE.equals(fp.getIsVerified())) count++;

        int percentage = (count * 100) / total;

        List<CompletionItemDTO> items = Arrays.asList(
                new CompletionItemDTO("Photo", "avatar", user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()),
                new CompletionItemDTO("Titre", "title", fp.getTitle() != null && !fp.getTitle().isBlank()),
                new CompletionItemDTO("Bio", "bio", fp.getOverview() != null && fp.getOverview().length() >= 50),
                new CompletionItemDTO("Compétences", "skills", fp.getSkills() != null && !fp.getSkills().isEmpty()),
                new CompletionItemDTO("Expérience", "experience", fp.getExperiences() != null && !fp.getExperiences().isEmpty()),
                new CompletionItemDTO("Portfolio", "portfolio", fp.getPortfolioItems() != null && !fp.getPortfolioItems().isEmpty()),
                new CompletionItemDTO("Certification", "certification", fp.getCertifications() != null && !fp.getCertifications().isEmpty()),
                new CompletionItemDTO("Vérification", "verification", Boolean.TRUE.equals(fp.getIsVerified()))
        );

        List<String> suggestions = items.stream()
                .filter(i -> !i.isCompleted())
                .map(i -> "Ajoutez votre " + i.getLabel().toLowerCase())
                .collect(Collectors.toList());

        fp.setCompletionPercentage(percentage);
        freelanceProfileRepository.save(fp);

        return CompletionDTO.builder()
                .percentage(percentage)
                .items(items)
                .suggestions(suggestions)
                .build();
    }

    public int recalculateCompletion(UUID freelanceProfileId) {
        FreelanceProfile fp = freelanceProfileRepository.findById(freelanceProfileId).orElse(null);
        if (fp == null) return 0;

        User user = fp.getUser();
        int count = 0;
        int total = 8;

        if (user.getAvatarUrl() != null && !user.getAvatarUrl().isBlank()) count++;
        if (fp.getTitle() != null && !fp.getTitle().isBlank()) count++;
        if (fp.getOverview() != null && fp.getOverview().length() >= 50) count++;
        if (fp.getSkills() != null && !fp.getSkills().isEmpty()) count++;
        if (fp.getExperiences() != null && !fp.getExperiences().isEmpty()) count++;
        if (fp.getPortfolioItems() != null && !fp.getPortfolioItems().isEmpty()) count++;
        if (fp.getCertifications() != null && !fp.getCertifications().isEmpty()) count++;
        if (Boolean.TRUE.equals(fp.getIsVerified())) count++;

        int percentage = (count * 100) / total;
        fp.setCompletionPercentage(percentage);
        freelanceProfileRepository.save(fp);
        return percentage;
    }

    public List<FavoriteDTO> getMyFavorites() {
        UUID userId = getCurrentUserId();
        return favoriteRepository.findByUserId(userId).stream()
                .map(fav -> FavoriteDTO.builder()
                        .id(fav.getId())
                        .targetUserId(fav.getTargetUser().getId())
                        .targetUsername(fav.getTargetUser().getUsername())
                        .targetFullName(fav.getTargetUser().getFullName())
                        .targetAvatarUrl(fav.getTargetUser().getAvatarUrl())
                        .favoritedAt(fav.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public void addFavorite(UUID targetUserId) {
        UUID userId = getCurrentUserId();
        if (userId.equals(targetUserId)) {
            throw new BusinessException("CANNOT_FAVORITE_SELF", "Vous ne pouvez pas vous ajouter vous-même", null);
        }
        if (favoriteRepository.existsByUserIdAndTargetUserId(userId, targetUserId)) {
            return;
        }
        User target = userRepository.findById(targetUserId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", targetUserId));
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        favoriteRepository.save(Favorite.builder().user(me).targetUser(target).build());
    }

    public void removeFavorite(UUID targetUserId) {
        UUID userId = getCurrentUserId();
        favoriteRepository.findByUserIdAndTargetUserId(userId, targetUserId)
                .ifPresent(favoriteRepository::delete);
    }
}
