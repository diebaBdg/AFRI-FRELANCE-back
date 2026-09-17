package AfriFreelance.API.business.notification;

import AfriFreelance.API.auth.SecurityUserPrincipal;
import AfriFreelance.API.business.notification.dto.NotificationPreferenceDTO;
import AfriFreelance.API.business.notification.dto.NotificationPreferenceRequest;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationPreferenceService {

    private final NotificationPreferenceRepository preferenceRepository;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            throw new BusinessException("NOT_AUTHENTICATED", "Utilisateur non authentifié", null);
        }
        return principal.getId();
    }

    public NotificationPreferenceDTO getMyPreferences() {
        UUID userId = getCurrentUserId();
        NotificationPreference pref = preferenceRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
            NotificationPreference newPref = NotificationPreference.builder().user(user).build();
            return preferenceRepository.save(newPref);
        });
        return toDTO(pref);
    }

    public NotificationPreferenceDTO updatePreferences(NotificationPreferenceRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        NotificationPreference pref = preferenceRepository.findByUserId(userId).orElse(null);
        if (pref == null) {
            pref = NotificationPreference.builder().user(user).build();
        }

        if (request.getEmailMessages() != null) pref.setEmailMessages(request.getEmailMessages());
        if (request.getEmailProjectUpdates() != null) pref.setEmailProjectUpdates(request.getEmailProjectUpdates());
        if (request.getEmailMarketing() != null) pref.setEmailMarketing(request.getEmailMarketing());
        if (request.getPushMessages() != null) pref.setPushMessages(request.getPushMessages());
        if (request.getPushProjectUpdates() != null) pref.setPushProjectUpdates(request.getPushProjectUpdates());
        if (request.getPushContractUpdates() != null) pref.setPushContractUpdates(request.getPushContractUpdates());
        if (request.getPushPaymentUpdates() != null) pref.setPushPaymentUpdates(request.getPushPaymentUpdates());

        preferenceRepository.save(pref);
        return toDTO(pref);
    }

    private NotificationPreferenceDTO toDTO(NotificationPreference pref) {
        return NotificationPreferenceDTO.builder()
                .id(pref.getId())
                .emailMessages(pref.getEmailMessages())
                .emailProjectUpdates(pref.getEmailProjectUpdates())
                .emailMarketing(pref.getEmailMarketing())
                .pushMessages(pref.getPushMessages())
                .pushProjectUpdates(pref.getPushProjectUpdates())
                .pushContractUpdates(pref.getPushContractUpdates())
                .pushPaymentUpdates(pref.getPushPaymentUpdates())
                .build();
    }
}
