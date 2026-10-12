package AfriFreelance.API.business.conversation.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDTO {
    private UUID id;
    private UUID user1Id;
    private String user1Name;
    private String user1AvatarUrl;
    private UUID user2Id;
    private String user2Name;
    private String user2AvatarUrl;
    private UUID projectId;
    private String projectTitle;
    private String type;
    private LocalDateTime lastMessageAt;
    private LocalDateTime createdAt;
    private String lastMessageContent;
    private Integer unreadCount;
}
