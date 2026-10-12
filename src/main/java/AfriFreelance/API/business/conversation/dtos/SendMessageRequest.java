package AfriFreelance.API.business.conversation.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SendMessageRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(max = 5000, message = "Le message ne doit pas dépasser 5000 caractères")
    private String content;

    private String attachmentUrl;
}
