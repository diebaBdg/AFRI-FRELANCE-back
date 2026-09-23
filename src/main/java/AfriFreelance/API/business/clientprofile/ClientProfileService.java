package AfriFreelance.API.business.clientprofile;

import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.business.badge.dtos.*;
import AfriFreelance.API.business.certification.dtos.*;
import AfriFreelance.API.business.clientprofile.dtos.*;
import AfriFreelance.API.business.company.dtos.*;
import AfriFreelance.API.business.education.dtos.*;
import AfriFreelance.API.business.favorite.dtos.*;
import AfriFreelance.API.business.language.dtos.*;
import AfriFreelance.API.business.portfolio.dtos.*;
import AfriFreelance.API.business.professionallink.dtos.*;
import AfriFreelance.API.business.skill.dtos.*;
import AfriFreelance.API.business.verification.dtos.*;
import AfriFreelance.API.business.workexperience.dtos.*;
import AfriFreelance.API.business.workpreference.dtos.*;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.ClientType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ClientProfileService {

    private final ClientProfileRepository clientProfileRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public ClientProfileDTO getMyClientProfile() {
        UUID userId = profileService.getCurrentUserId();
        ClientProfile cp = clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("NO_CLIENT_PROFILE",
                        "Vous n'avez pas encore créé de profil client", userId));
        return toDTO(cp);
    }

    public ClientProfileDTO createOrUpdateClientProfile(ClientProfileRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        ClientProfile cp = clientProfileRepository.findByUserId(userId).orElse(null);
        boolean isNew = cp == null;

        if (isNew) {
            cp = new ClientProfile();
            cp.setUser(user);
        }

        cp.setDisplayName(request.getDisplayName() != null ? request.getDisplayName() : user.getFullName());
        if (request.getClientType() != null) {
            try { cp.setClientType(ClientType.valueOf(request.getClientType())); } catch (IllegalArgumentException ignored) {}
        }
        cp.setOverview(request.getOverview());
        cp.setCountry(request.getCountry() != null ? request.getCountry() : user.getCountry());
        cp.setCity(request.getCity() != null ? request.getCity() : user.getCity());
        if (request.getProfileActive() != null) cp.setProfileActive(request.getProfileActive());

        cp = clientProfileRepository.save(cp);
        return toDTO(cp);
    }

    public void toggleClientProfile(boolean active) {
        UUID userId = profileService.getCurrentUserId();
        ClientProfile cp = clientProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("NO_CLIENT_PROFILE",
                        "Vous n'avez pas encore créé de profil client", userId));
        cp.setProfileActive(active);
        clientProfileRepository.save(cp);
    }

    private ClientProfileDTO toDTO(ClientProfile cp) {
        return ClientProfileDTO.builder()
                .id(cp.getId())
                .userId(cp.getUser().getId())
                .displayName(cp.getDisplayName())
                .clientType(cp.getClientType() != null ? cp.getClientType().name() : "INDIVIDUAL")
                .companyId(cp.getCompanyId())
                .overview(cp.getOverview())
                .country(cp.getCountry())
                .city(cp.getCity())
                .profileActive(cp.getProfileActive())
                .projectsPublished(cp.getProjectsPublished())
                .projectsCompleted(cp.getProjectsCompleted())
                .hireRate(cp.getHireRate())
                .avgRating(cp.getAvgRating())
                .totalSpent(cp.getTotalSpent())
                .build();
    }
}
