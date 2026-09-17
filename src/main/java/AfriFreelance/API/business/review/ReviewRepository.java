package AfriFreelance.API.business.review;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByReviewedId(UUID reviewedId);

    List<Review> findByReviewerId(UUID reviewerId);

    Optional<Review> findByReviewerIdAndReviewedId(UUID reviewerId, UUID reviewedId);

    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.reviewed.id = :userId")
    Double getAverageRating(@Param("userId") UUID userId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.reviewed.id = :userId")
    Long getReviewCount(@Param("userId") UUID userId);
}
