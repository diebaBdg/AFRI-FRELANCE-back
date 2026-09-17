package AfriFreelance.API.business.review.dto;

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
public class ReviewDTO {
    private UUID id;
    private UUID reviewerId;
    private String reviewerName;
    private String reviewerAvatarUrl;
    private UUID reviewedId;
    private Integer rating;
    private String comment;
    private UUID projectId;
    private LocalDateTime createdAt;
}
