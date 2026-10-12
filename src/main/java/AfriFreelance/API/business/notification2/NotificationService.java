package AfriFreelance.API.business.notification2;

import AfriFreelance.API.auth.SecurityUserPrincipal;
import AfriFreelance.API.business.notification2.dtos.NotificationDTO;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Async
    public void sendNotification(UUID userId, NotificationType type, String message, UUID entityId) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                log.warn("Cannot send notification: user {} not found", userId);
                return;
            }
            Notification notification = Notification.builder()
                    .user(user)
                    .type(type)
                    .message(message)
                    .entityId(entityId)
                    .isRead(false)
                    .build();
            notificationRepository.save(notification);
        } catch (Exception e) {
            log.error("Failed to send notification to user {}: {}", userId, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationDTO> getMyNotifications() {
        UUID userId = getCurrentUserId();
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getUnreadCount() {
        UUID userId = getCurrentUserId();
        return notificationRepository.countUnreadByUserId(userId);
    }

    public void markAsRead(UUID notificationId) {
        UUID userId = getCurrentUserId();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new BusinessException("NOTIFICATION_NOT_FOUND", "Notification non trouvée", notificationId));
        if (!notification.getUser().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas autorisé", userId);
        }
        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public void markAllAsRead() {
        UUID userId = getCurrentUserId();
        notificationRepository.markAllAsRead(userId);
    }

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            throw new BusinessException("NOT_AUTHENTICATED", "Utilisateur non authentifié", null);
        }
        return principal.getId();
    }

    private NotificationDTO toDTO(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType() != null ? n.getType().name() : null)
                .message(n.getMessage())
                .entityId(n.getEntityId())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .readAt(n.getReadAt())
                .build();
    }
}
