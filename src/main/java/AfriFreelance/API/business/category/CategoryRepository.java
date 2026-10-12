package AfriFreelance.API.business.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByIsActiveTrueOrderByDisplayOrderAscNameAsc();
    List<Category> findAllByOrderByDisplayOrderAscNameAsc();
    Optional<Category> findByName(String name);
    boolean existsByName(String name);
}
