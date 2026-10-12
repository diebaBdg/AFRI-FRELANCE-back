package AfriFreelance.API.business.conversation;

import AfriFreelance.API.business.notification2.NotificationService;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.project.Project;
import AfriFreelance.API.business.project.ProjectRepository;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.business.conversation.dtos.*;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.ConversationType;
import AfriFreelance.API.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;

    public ConversationDTO createConversation(CreateConversationRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User me = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        if (userId.equals(request.getTargetUserId())) {
            throw new BusinessException("CANNOT_MESSAGE_SELF", "Vous ne pouvez pas créer une conversation avec vous-même", null);
        }

        User target = userRepository.findById(request.getTargetUserId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", request.getTargetUserId()));

        Optional<Conversation> existing = conversationRepository.findBetweenUsers(userId, request.getTargetUserId());
        if (existing.isPresent()) {
            return toDTO(existing.get(), userId);
        }

        Project project = null;
        ConversationType type = ConversationType.DIRECT;
        if (request.getProjectId() != null) {
            project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new BusinessException("PROJECT_NOT_FOUND", "Projet non trouvé", request.getProjectId()));
            type = ConversationType.PROJECT;
        }

        Conversation conversation = Conversation.builder()
                .user1(me)
                .user2(target)
                .project(project)
                .type(type)
                .build();

        conversation = conversationRepository.save(conversation);
        return toDTO(conversation, userId);
    }

    @Transactional(readOnly = true)
    public List<ConversationDTO> getMyConversations() {
        UUID userId = profileService.getCurrentUserId();
        List<Conversation> conversations = conversationRepository.findByUserIdOrderByLastMessageAtDesc(userId);
        return conversations.stream()
                .map(c -> toDTO(c, userId))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageDTO> getMessages(UUID conversationId) {
        UUID userId = profileService.getCurrentUserId();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("CONVERSATION_NOT_FOUND", "Conversation non trouvée", conversationId));

        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous ne faites pas partie de cette conversation", userId);
        }

        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId).stream()
                .map(this::toMessageDTO)
                .collect(Collectors.toList());
    }

    public MessageDTO sendMessage(UUID conversationId, SendMessageRequest request) {
        UUID userId = profileService.getCurrentUserId();
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new BusinessException("CONVERSATION_NOT_FOUND", "Conversation non trouvée", conversationId));

        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous ne faites pas partie de cette conversation", userId);
        }

        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(request.getContent())
                .attachmentUrl(request.getAttachmentUrl())
                .isRead(false)
                .build();

        message = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        UUID recipientId = conversation.getUser1().getId().equals(userId)
                ? conversation.getUser2().getId()
                : conversation.getUser1().getId();

        notificationService.sendNotification(
                recipientId,
                NotificationType.NEW_MESSAGE,
                "Nouveau message de " + sender.getFullName(),
                message.getId()
        );

        return toMessageDTO(message);
    }

    public void markMessageAsRead(UUID messageId) {
        UUID userId = profileService.getCurrentUserId();
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("MESSAGE_NOT_FOUND", "Message non trouvé", messageId));

        Conversation conversation = message.getConversation();
        if (!conversation.getUser1().getId().equals(userId) && !conversation.getUser2().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas autorisé", userId);
        }

        if (!message.getSender().getId().equals(userId)) {
            message.setIsRead(true);
            message.setReadAt(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    private ConversationDTO toDTO(Conversation c, UUID currentUserId) {
        boolean isUser1 = c.getUser1().getId().equals(currentUserId);
        User other = isUser1 ? c.getUser2() : c.getUser1();
        User me = isUser1 ? c.getUser1() : c.getUser2();

        List<Message> messages = messageRepository.findByConversationIdOrderBySentAtAsc(c.getId());
        String lastContent = messages.isEmpty() ? null : messages.get(messages.size() - 1).getContent();
        int unread = (int) messages.stream()
                .filter(m -> !m.getSender().getId().equals(currentUserId) && !Boolean.TRUE.equals(m.getIsRead()))
                .count();

        return ConversationDTO.builder()
                .id(c.getId())
                .user1Id(c.getUser1().getId())
                .user1Name(c.getUser1().getFullName())
                .user1AvatarUrl(c.getUser1().getAvatarUrl())
                .user2Id(c.getUser2().getId())
                .user2Name(c.getUser2().getFullName())
                .user2AvatarUrl(c.getUser2().getAvatarUrl())
                .projectId(c.getProject() != null ? c.getProject().getId() : null)
                .projectTitle(c.getProject() != null ? c.getProject().getTitle() : null)
                .type(c.getType() != null ? c.getType().name() : "DIRECT")
                .lastMessageAt(c.getLastMessageAt())
                .createdAt(c.getCreatedAt())
                .lastMessageContent(lastContent)
                .unreadCount(unread)
                .build();
    }

    private MessageDTO toMessageDTO(Message m) {
        return MessageDTO.builder()
                .id(m.getId())
                .conversationId(m.getConversation().getId())
                .senderId(m.getSender().getId())
                .senderName(m.getSender().getFullName())
                .senderAvatarUrl(m.getSender().getAvatarUrl())
                .content(m.getContent())
                .attachmentUrl(m.getAttachmentUrl())
                .isRead(m.getIsRead())
                .sentAt(m.getSentAt())
                .readAt(m.getReadAt())
                .build();
    }
}
