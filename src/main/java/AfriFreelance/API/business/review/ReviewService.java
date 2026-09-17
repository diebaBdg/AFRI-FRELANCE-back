package AfriFreelance.API.business.review;

import AfriFreelance.API.auth.SecurityUserPrincipal;
import AfriFreelance.API.business.review.dto.ReviewDTO;
import AfriFreelance.API.business.review.dto.ReviewRequest;
import AfriFreelance.API.business.review.dto.ReviewSummaryDTO;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

    private UUID getCurrentUserId() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof SecurityUserPrincipal principal)) {
            throw new BusinessException("NOT_AUTHENTICATED", "Utilisateur non authentifié", null);
        }
        return principal.getId();
    }

    public ReviewDTO createReview(ReviewRequest request) {
        UUID reviewerId = getCurrentUserId();

        if (reviewerId.equals(request.getReviewedId())) {
            throw new BusinessException("CANNOT_REVIEW_SELF", "Vous ne pouvez pas vous évaluer vous-même", null);
        }

        if (reviewRepository.findByReviewerIdAndReviewedId(reviewerId, request.getReviewedId()).isPresent()) {
            throw new BusinessException("ALREADY_REVIEWED", "Vous avez déjà évalué cet utilisateur", request.getReviewedId());
        }

        User reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", reviewerId));
        User reviewed = userRepository.findById(request.getReviewedId())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", request.getReviewedId()));

        Review review = Review.builder()
                .reviewer(reviewer)
                .reviewed(reviewed)
                .rating(request.getRating())
                .comment(request.getComment())
                .projectId(request.getProjectId())
                .build();
        review = reviewRepository.save(review);

        return toDTO(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getReviewsForUser(UUID userId) {
        return reviewRepository.findByReviewedId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReviewSummaryDTO getReviewSummary(UUID userId) {
        Double avgRating = reviewRepository.getAverageRating(userId);
        Long totalReviews = reviewRepository.getReviewCount(userId);
        return ReviewSummaryDTO.builder()
                .userId(userId)
                .avgRating(avgRating != null ? avgRating : 0.0)
                .totalReviews(totalReviews != null ? totalReviews : 0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ReviewDTO> getMyReviews() {
        UUID userId = getCurrentUserId();
        return reviewRepository.findByReviewerId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteReview(UUID reviewId) {
        UUID userId = getCurrentUserId();
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new BusinessException("REVIEW_NOT_FOUND", "Avis non trouvé", reviewId));
        if (!review.getReviewer().getId().equals(userId)) {
            throw new BusinessException("NOT_REVIEW_OWNER", "Vous n'êtes pas l'auteur de cet avis", reviewId);
        }
        reviewRepository.delete(review);
    }

    private ReviewDTO toDTO(Review r) {
        return ReviewDTO.builder()
                .id(r.getId())
                .reviewerId(r.getReviewer().getId())
                .reviewerName(r.getReviewer().getFullName())
                .reviewerAvatarUrl(r.getReviewer().getAvatarUrl())
                .reviewedId(r.getReviewed().getId())
                .rating(r.getRating())
                .comment(r.getComment())
                .projectId(r.getProjectId())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
