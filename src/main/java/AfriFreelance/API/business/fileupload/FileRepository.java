package AfriFreelance.API.business.fileupload;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, UUID> {
    List<FileEntity> findByUploadedByIdOrderByCreatedAtDesc(UUID uploadedById);
    List<FileEntity> findByContractIdOrderByCreatedAtDesc(UUID contractId);
    List<FileEntity> findByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
