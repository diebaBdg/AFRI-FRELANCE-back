package AfriFreelance.API.business.conversation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    @Query("SELECT c FROM Conversation c WHERE c.user1.id = :userId OR c.user2.id = :userId ORDER BY c.lastMessageAt DESC")
    List<Conversation> findByUserIdOrderByLastMessageAtDesc(@Param("userId") UUID userId);

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.user1.id = :user1Id AND c.user2.id = :user2Id) OR " +
           "(c.user1.id = :user2Id AND c.user2.id = :user1Id)")
    Optional<Conversation> findBetweenUsers(@Param("user1Id") UUID user1Id, @Param("user2Id") UUID user2Id);

    Optional<Conversation> findByProjectId(UUID projectId);
}
