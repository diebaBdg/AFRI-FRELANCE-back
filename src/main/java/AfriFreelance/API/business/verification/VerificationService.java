package AfriFreelance.API.business.verification;

import AfriFreelance.API.business.badge.Badge;
import AfriFreelance.API.business.badge.BadgeRepository;
import AfriFreelance.API.business.freelanceprofile.FreelanceProfileRepository;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.BadgeType;
import AfriFreelance.API.enums.VerificationStatus;
import AfriFreelance.API.enums.VerificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class VerificationService {

    private final VerificationRepository verificationRepository;
    private final BadgeRepository badgeRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final FreelanceProfileRepository freelanceProfileRepository;

    public VerificationSummaryDTO getMyVerifications() {
        UUID userId = profileService.getCurrentUserId();
        return getVerificationsForUser(userId);
    }

    public VerificationSummaryDTO getVerificationsForUser(UUID userId) {
        List<Verification> verifications = verificationRepository.findByUserId(userId);

        List<VerificationDTO> dtos = Arrays.stream(VerificationType.values())
                .map(type -> {
                    Verification v = verifications.stream()
                            .filter(ver -> ver.getVerificationType() == type)
                            .findFirst()
                            .orElse(null);
                    if (v == null) {
                        return VerificationDTO.builder()
                                .verificationType(type.name())
                                .status("PENDING")
                                .build();
                    }
                    return VerificationDTO.builder()
                            .id(v.getId())
                            .verificationType(v.getVerificationType().name())
                            .status(v.getStatus().name())
                            .verifiedAt(v.getVerifiedAt())
                            .createdAt(v.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        long completed = dtos.stream().filter(d -> "VERIFIED".equals(d.getStatus())).count();
        int total = VerificationType.values().length;
        double progress = (completed * 100.0) / total;

        return VerificationSummaryDTO.builder()
                .verifications(dtos)
                .completed((int) completed)
                .total(total)
                .progressPercentage(progress)
                .build();
    }

    public VerificationDTO requestVerification(String verificationTypeStr) {
        UUID userId = profileService.getCurrentUserId();
        VerificationType type = VerificationType.valueOf(verificationTypeStr);

        Verification v = verificationRepository.findByUserIdAndVerificationType(userId, type)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
                    Verification newV = Verification.builder()
                            .user(user)
                            .verificationType(type)
                            .status(VerificationStatus.PENDING)
                            .build();
                    return verificationRepository.save(newV);
                });

        if (v.getStatus() == VerificationStatus.VERIFIED) {
            throw new BusinessException("ALREADY_VERIFIED", "Cette vérification est déjà complétée", type.name());
        }

        return VerificationDTO.builder()
                .id(v.getId())
                .verificationType(v.getVerificationType().name())
                .status(v.getStatus().name())
                .verifiedAt(v.getVerifiedAt())
                .createdAt(v.getCreatedAt())
                .build();
    }

    public VerificationDTO simulateVerification(String verificationTypeStr) {
        UUID userId = profileService.getCurrentUserId();
        VerificationType type = VerificationType.valueOf(verificationTypeStr);

        Verification v = verificationRepository.findByUserIdAndVerificationType(userId, type)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
                    Verification newV = Verification.builder()
                            .user(user)
                            .verificationType(type)
                            .status(VerificationStatus.PENDING)
                            .build();
                    return verificationRepository.save(newV);
                });

        v.setStatus(VerificationStatus.VERIFIED);
        v.setVerifiedAt(LocalDateTime.now());
        verificationRepository.save(v);

        BadgeType badgeType = mapToBadge(type);
        if (badgeRepository.findByUserIdAndBadgeType(userId, badgeType).isEmpty()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
            badgeRepository.save(Badge.builder().user(user).badgeType(badgeType).build());
        }

        if (type == VerificationType.IDENTITY || type == VerificationType.PROFESSIONAL) {
            freelanceProfileRepository.findByUserId(userId).ifPresent(fp -> {
                long verifiedCount = verificationRepository.findByUserId(userId).stream()
                        .filter(ver -> ver.getStatus() == VerificationStatus.VERIFIED)
                        .count();
                if (verifiedCount >= 3) {
                    fp.setIsVerified(true);
                    freelanceProfileRepository.save(fp);
                }
            });
        }

        return VerificationDTO.builder()
                .id(v.getId())
                .verificationType(v.getVerificationType().name())
                .status(v.getStatus().name())
                .verifiedAt(v.getVerifiedAt())
                .createdAt(v.getCreatedAt())
                .build();
    }

    public List<BadgeDTO> getMyBadges() {
        UUID userId = profileService.getCurrentUserId();
        return badgeRepository.findByUserId(userId).stream()
                .map(b -> BadgeDTO.builder()
                        .id(b.getId())
                        .badgeType(b.getBadgeType().name())
                        .awardedAt(b.getAwardedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public List<BadgeDTO> getBadgesForUser(UUID userId) {
        return badgeRepository.findByUserId(userId).stream()
                .map(b -> BadgeDTO.builder()
                        .id(b.getId())
                        .badgeType(b.getBadgeType().name())
                        .awardedAt(b.getAwardedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private BadgeType mapToBadge(VerificationType type) {
        return switch (type) {
            case EMAIL -> BadgeType.EMAIL_VERIFIED;
            case PHONE -> BadgeType.PHONE_VERIFIED;
            case IDENTITY -> BadgeType.IDENTITY_VERIFIED;
            case PROFESSIONAL -> BadgeType.PROFESSIONAL_VERIFIED;
        };
    }
}
