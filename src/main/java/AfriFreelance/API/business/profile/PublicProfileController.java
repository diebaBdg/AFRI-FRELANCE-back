package AfriFreelance.API.business.profile;

import AfriFreelance.API.business.profile.dtos.*;
import AfriFreelance.API.business.badge.dtos.*;
import AfriFreelance.API.business.certification.dtos.*;
import AfriFreelance.API.business.clientprofile.dtos.*;
import AfriFreelance.API.business.company.dtos.*;
import AfriFreelance.API.business.education.dtos.*;
import AfriFreelance.API.business.favorite.dtos.*;
import AfriFreelance.API.business.language.dtos.*;
import AfriFreelance.API.business.portfolio.dtos.*;
import AfriFreelance.API.business.professionallink.dtos.*;
import AfriFreelance.API.business.skill.dtos.*;
import AfriFreelance.API.business.verification.dtos.*;
import AfriFreelance.API.business.workexperience.dtos.*;
import AfriFreelance.API.business.workpreference.dtos.*;
import AfriFreelance.API.business.review.ReviewService;
import AfriFreelance.API.business.review.dto.ReviewDTO;
import AfriFreelance.API.business.review.dto.ReviewSummaryDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
@Tag(name = "Profils Publics", description = "Consultation des profils publics, recherche de freelancers et avis")
public class PublicProfileController {

    private final PublicProfileService publicProfileService;
    private final ReviewService reviewService;

    @GetMapping("/freelancers/{username}")
    @Operation(summary = "Consulter le profil public d'un freelancer")
    public ResponseEntity<PublicFreelanceProfileDTO> getPublicFreelanceProfile(@PathVariable String username) {
        return ResponseEntity.ok(publicProfileService.getPublicFreelanceProfile(username));
    }

    @GetMapping("/clients/{username}")
    @Operation(summary = "Consulter le profil public d'un client")
    public ResponseEntity<PublicClientProfileDTO> getPublicClientProfile(@PathVariable String username) {
        return ResponseEntity.ok(publicProfileService.getPublicClientProfile(username));
    }

    @GetMapping("/freelancers")
    @Operation(summary = "Rechercher des freelancers par texte et/ou pays")
    public ResponseEntity<List<FreelancerCardDTO>> searchFreelancers(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(publicProfileService.searchFreelancers(query, country, limit));
    }

    @GetMapping("/freelancers/{username}/reviews")
    @Operation(summary = "Obtenir les avis publics d'un freelancer")
    public ResponseEntity<List<ReviewDTO>> getFreelancerReviews(@PathVariable String username) {
        UUID userId = publicProfileService.getUserIdByUsername(username);
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    @GetMapping("/clients/{username}/reviews")
    @Operation(summary = "Obtenir les avis publics d'un client")
    public ResponseEntity<List<ReviewDTO>> getClientReviews(@PathVariable String username) {
        UUID userId = publicProfileService.getUserIdByUsername(username);
        return ResponseEntity.ok(reviewService.getReviewsForUser(userId));
    }

    @GetMapping("/users/{userId}/reviews/summary")
    @Operation(summary = "Obtenir le résumé des avis d'un utilisateur (note moyenne, total)")
    public ResponseEntity<ReviewSummaryDTO> getReviewSummary(@PathVariable UUID userId) {
        return ResponseEntity.ok(reviewService.getReviewSummary(userId));
    }
}
