package AfriFreelance.API.business.security;

import AfriFreelance.API.auth.SecurityUserPrincipal;
import AfriFreelance.API.auth.dtos.ChangePasswordRequest;
import AfriFreelance.API.auth.dtos.VerifyPasswordRequest;
import AfriFreelance.API.auth.dtos.VerifyPasswordResponse;
import AfriFreelance.API.business.security.dto.SecurityInfoDTO;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.validation.PasswordPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SecurityService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            throw new BusinessException("NOT_AUTHENTICATED", "Utilisateur non authentifié", null);
        }
        return principal.getId();
    }

    public SecurityInfoDTO getSecurityInfo() {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        return SecurityInfoDTO.builder()
                .email(user.getEmail())
                .phone(user.getPhone())
                .hasPassword(true)
                .lastPasswordChange(user.getLastPasswordChange() != null ? user.getLastPasswordChange() : user.getDateCreation())
                .twoFactorEnabled(false)
                .build();
    }

    public SecurityInfoDTO changePassword(ChangePasswordRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        if (request.getOldPassword() == null || request.getOldPassword().isBlank()) {
            throw new BusinessException("OLD_PASSWORD_REQUIRED", "L'ancien mot de passe est obligatoire", null);
        }

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new BusinessException("INVALID_OLD_PASSWORD", "L'ancien mot de passe est incorrect", null);
        }

        if (!PasswordPolicy.isStrong(request.getNewPassword())) {
            throw new BusinessException("WEAK_PASSWORD", PasswordPolicy.MESSAGE, null);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastPasswordChange(LocalDateTime.now());
        userRepository.save(user);

        log.info("Mot de passe changé pour l'utilisateur: {}", userId);

        return getSecurityInfo();
    }

    public VerifyPasswordResponse verifyPassword(VerifyPasswordRequest request) {
        UUID userId = getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        boolean valid = passwordEncoder.matches(request.getPassword(), user.getPassword());
        return new VerifyPasswordResponse(valid);
    }
}
