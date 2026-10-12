package AfriFreelance.API.business.fileupload;

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
public class FileDTO {
    private UUID id;
    private String originalName;
    private String contentType;
    private Long size;
    private String accessType;
    private UUID contractId;
    private UUID projectId;
    private UUID uploadedById;
    private String uploadedByName;
    private LocalDateTime createdAt;
}
