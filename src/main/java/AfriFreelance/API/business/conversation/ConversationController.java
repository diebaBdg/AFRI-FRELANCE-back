package AfriFreelance.API.business.conversation;

import AfriFreelance.API.business.conversation.dtos.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Messagerie", description = "Conversations et messages entre utilisateurs")
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping("/conversations")
    @Operation(summary = "Obtenir mes conversations")
    public ResponseEntity<List<ConversationDTO>> getMyConversations() {
        return ResponseEntity.ok(conversationService.getMyConversations());
    }

    @PostMapping("/conversations")
    @Operation(summary = "Créer une conversation avec un autre utilisateur")
    public ResponseEntity<ConversationDTO> createConversation(@Valid @RequestBody CreateConversationRequest request) {
        return new ResponseEntity<>(conversationService.createConversation(request), HttpStatus.CREATED);
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Obtenir les messages d'une conversation")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable UUID conversationId) {
        return ResponseEntity.ok(conversationService.getMessages(conversationId));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Envoyer un message dans une conversation")
    public ResponseEntity<MessageDTO> sendMessage(@PathVariable UUID conversationId,
                                                    @Valid @RequestBody SendMessageRequest request) {
        return new ResponseEntity<>(conversationService.sendMessage(conversationId, request), HttpStatus.CREATED);
    }

    @PatchMapping("/messages/{messageId}/read")
    @Operation(summary = "Marquer un message comme lu")
    public ResponseEntity<Void> markAsRead(@PathVariable UUID messageId) {
        conversationService.markMessageAsRead(messageId);
        return ResponseEntity.noContent().build();
    }
}
