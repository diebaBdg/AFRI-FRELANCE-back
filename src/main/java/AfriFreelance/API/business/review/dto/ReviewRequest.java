package AfriFreelance.API.business.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequest {

    @NotNull(message = "L'utilisateur à évaluer est obligatoire")
    private UUID reviewedId;

    @NotNull(message = "La note est obligatoire")
    @Min(value = 1, message = "La note minimale est 1")
    @Max(value = 5, message = "La note maximale est 5")
    private Integer rating;

    @Size(max = 2000, message = "Le commentaire ne doit pas dépasser 2000 caractères")
    private String comment;

    private UUID projectId;
}
