package AfriFreelance.API.business.conversation.dtos;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreateConversationRequest {

    @NotNull(message = "L'utilisateur destinataire est obligatoire")
    private UUID targetUserId;

    private UUID projectId;
}
