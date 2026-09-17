package AfriFreelance.API.business.dashboard;

import AfriFreelance.API.business.badge.BadgeRepository;
import AfriFreelance.API.business.clientprofile.ClientProfile;
import AfriFreelance.API.business.clientprofile.ClientProfileRepository;
import AfriFreelance.API.business.dashboard.dto.DashboardDTO;
import AfriFreelance.API.business.dashboard.dto.DashboardStatDTO;
import AfriFreelance.API.business.favorite.FavoriteRepository;
import AfriFreelance.API.business.freelanceprofile.FreelanceProfile;
import AfriFreelance.API.business.freelanceprofile.FreelanceProfileRepository;
import AfriFreelance.API.business.profile.*;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.ActiveMode;
import AfriFreelance.API.auth.SecurityUserPrincipal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final FreelanceProfileRepository freelanceProfileRepository;
    private final ClientProfileRepository clientProfileRepository;
    private final BadgeRepository badgeRepository;
    private final FavoriteRepository favoriteRepository;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            throw new BusinessException("NOT_AUTHENTICATED", "Utilisateur non authentifié", null);
        }
        return principal.getId();
    }

    public DashboardDTO getDashboard() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        if (user.getActiveMode() == ActiveMode.CLIENT) {
            return getClientDashboard();
        }
        return getFreelanceDashboard();
    }

    public DashboardDTO getFreelanceDashboard() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        FreelanceProfile fp = freelanceProfileRepository.findByUserId(userId).orElse(null);

        DashboardDTO dto = DashboardDTO.builder()
                .mode("FREELANCE")
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .headline(user.getHeadline())
                .completionPercentage(fp != null ? fp.getCompletionPercentage() : 0)
                .isVerified(fp != null && Boolean.TRUE.equals(fp.getIsVerified()))
                .badgeCount(badgeRepository.findByUserId(userId).size())
                .favoriteCount(favoriteRepository.findByUserId(userId).size())
                .build();

        if (fp != null) {
            dto.setStats(List.of(
                    DashboardStatDTO.builder().key("hourlyRate").label("Tarif horaire")
                            .value(fp.getHourlyRate() != null ? String.valueOf(fp.getHourlyRate()) : "—")
                            .unit(fp.getCurrency() != null ? fp.getCurrency().name() : "XOF").build(),
                    DashboardStatDTO.builder().key("availability").label("Disponibilité")
                            .value(fp.getAvailabilityStatus() != null ? fp.getAvailabilityStatus().name() : "—").build(),
                    DashboardStatDTO.builder().key("weeklyHours").label("Heures / semaine")
                            .value(fp.getWeeklyHours() != null ? String.valueOf(fp.getWeeklyHours()) : "—").build(),
                    DashboardStatDTO.builder().key("skills").label("Compétences")
                            .value(String.valueOf(fp.getSkills().size())).build()
            ));
        }

        return dto;
    }

    public DashboardDTO getClientDashboard() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        ClientProfile cp = clientProfileRepository.findByUserId(userId).orElse(null);

        DashboardDTO dto = DashboardDTO.builder()
                .mode("CLIENT")
                .fullName(user.getFullName())
                .avatarUrl(user.getAvatarUrl())
                .headline(user.getHeadline())
                .completionPercentage(cp != null ? 100 : 0)
                .isVerified(false)
                .badgeCount(badgeRepository.findByUserId(userId).size())
                .favoriteCount(favoriteRepository.findByUserId(userId).size())
                .build();

        if (cp != null) {
            dto.setStats(List.of(
                    DashboardStatDTO.builder().key("projectsPublished").label("Projets publiés")
                            .value(String.valueOf(cp.getProjectsPublished() != null ? cp.getProjectsPublished() : 0)).build(),
                    DashboardStatDTO.builder().key("projectsCompleted").label("Projets terminés")
                            .value(String.valueOf(cp.getProjectsCompleted() != null ? cp.getProjectsCompleted() : 0)).build(),
                    DashboardStatDTO.builder().key("hireRate").label("Taux de recrutement")
                            .value(cp.getHireRate() != null ? String.valueOf(cp.getHireRate()) : "0").unit("%").build(),
                    DashboardStatDTO.builder().key("totalSpent").label("Total dépensé")
                            .value(cp.getTotalSpent() != null ? String.valueOf(cp.getTotalSpent()) : "0")
                            .unit(user.getDefaultCurrency() != null ? user.getDefaultCurrency().name() : "XOF").build()
            ));
        }

        return dto;
    }
}
