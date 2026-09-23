package AfriFreelance.API.business.favorite.dtos;

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
public class FavoriteDTO {
    private UUID id;
    private UUID targetUserId;
    private String targetUsername;
    private String targetFullName;
    private String targetAvatarUrl;
    private String targetTitle;
    private LocalDateTime favoritedAt;
}
