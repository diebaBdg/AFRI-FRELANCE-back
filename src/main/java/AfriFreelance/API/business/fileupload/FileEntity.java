package AfriFreelance.API.business.fileupload;

import AfriFreelance.API.business.user.User;
import AfriFreelance.API.enums.FileAccessType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "td_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "original_name", nullable = false, length = 255)
    private String originalName;

    @Column(name = "stored_name", nullable = false, length = 255)
    private String storedName;

    @Column(nullable = false, length = 100)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id", nullable = false)
    private User uploadedBy;

    @Column(name = "access_type", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private FileAccessType accessType = FileAccessType.PRIVATE;

    @Column(name = "contract_id")
    private UUID contractId;

    @Column(name = "project_id")
    private UUID projectId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (accessType == null) accessType = FileAccessType.PRIVATE;
    }
}
