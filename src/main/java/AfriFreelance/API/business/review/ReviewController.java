package AfriFreelance.API.business.review;

import AfriFreelance.API.business.review.dto.ReviewDTO;
import AfriFreelance.API.business.review.dto.ReviewRequest;
import AfriFreelance.API.business.review.dto.ReviewSummaryDTO;
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
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Avis & Évaluations", description = "Gestion des avis entre utilisateurs")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Créer un avis sur un utilisateur")
    public ResponseEntity<ReviewDTO> createReview(@Valid @RequestBody ReviewRequest request) {
        return new ResponseEntity<>(reviewService.createReview(request), HttpStatus.CREATED);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Obtenir les avis reçus par un utilisateur")
    public ResponseEntity<List<ReviewDTO>> getReviewsForUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "Obtenir le résumé des avis d'un utilisateur (note moyenne, total)")
    public ResponseEntity<ReviewSummaryDTO> getReviewSummary(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewSummary(userId));
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir les avis que j'ai écrits")
    public ResponseEntity<List<ReviewDTO>> getMyReviews() {
        return ResponseEntity.ok(reviewService.getMyReviews());
    }

    @DeleteMapping("/{reviewId}")
    @Operation(summary = "Supprimer un avis que j'ai écrit")
    public ResponseEntity<Void> deleteReview(@PathVariable UUID reviewId) {
        reviewService.deleteReview(reviewId);
        return ResponseEntity.noContent().build();
    }
}
