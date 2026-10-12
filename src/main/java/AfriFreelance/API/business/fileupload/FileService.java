package AfriFreelance.API.business.fileupload;

import AfriFreelance.API.business.contract.Contract;
import AfriFreelance.API.business.contract.ContractRepository;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.FileAccessType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class FileService {

    private final FileRepository fileRepository;
    private final UserRepository userRepository;
    private final ContractRepository contractRepository;
    private final ProfileService profileService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;

    private static final List<String> ALLOWED_TYPES = List.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "text/plain",
            "application/zip",
            "application/x-zip-compressed"
    );

    public FileDTO uploadFile(MultipartFile file, String accessTypeStr, UUID contractId, UUID projectId) {
        UUID userId = profileService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));

        if (file.isEmpty()) {
            throw new BusinessException("FILE_EMPTY", "Le fichier est vide", null);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException("FILE_TOO_LARGE", "Le fichier dépasse la taille maximale de 10MB", null);
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("FILE_TYPE_NOT_ALLOWED",
                    "Type de fichier non autorisé. Types acceptés: PDF, DOC, DOCX, JPG, PNG, GIF, WEBP, TXT, ZIP", contentType);
        }

        if (contractId != null) {
            Contract contract = contractRepository.findById(contractId)
                    .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));
            if (!contract.getClient().getId().equals(userId) && !contract.getFreelance().getId().equals(userId)) {
                throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas partie à ce contrat", userId);
            }
        }

        FileAccessType accessType = FileAccessType.PRIVATE;
        if (accessTypeStr != null && !accessTypeStr.isBlank()) {
            accessType = FileAccessType.valueOf(accessTypeStr.toUpperCase());
        }

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unnamed";
            String storedName = UUID.randomUUID() + "_" + originalName.replaceAll("[^a-zA-Z0-9._-]", "_");
            Path targetPath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            FileEntity entity = FileEntity.builder()
                    .originalName(originalName)
                    .storedName(storedName)
                    .contentType(contentType)
                    .size(file.getSize())
                    .uploadedBy(user)
                    .accessType(accessType)
                    .contractId(contractId)
                    .projectId(projectId)
                    .build();
            entity = fileRepository.save(entity);

            log.info("Fichier uploadé: {} par {}", storedName, userId);
            return toDTO(entity);

        } catch (IOException e) {
            log.error("Erreur lors de l'upload du fichier: {}", e.getMessage(), e);
            throw new BusinessException("FILE_UPLOAD_ERROR", "Erreur lors de l'upload du fichier", null);
        }
    }

    public Resource downloadFile(UUID fileId) {
        UUID userId = profileService.getCurrentUserId();
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("FILE_NOT_FOUND", "Fichier non trouvé", fileId));

        if (!canAccess(file, userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'avez pas accès à ce fichier", userId);
        }

        Path filePath = Paths.get(uploadDir).resolve(file.getStoredName());
        if (!Files.exists(filePath)) {
            throw new BusinessException("FILE_MISSING", "Le fichier physique est introuvable", fileId);
        }

        return new FileSystemResource(filePath);
    }

    @Transactional(readOnly = true)
    public List<FileDTO> getMyFiles() {
        UUID userId = profileService.getCurrentUserId();
        return fileRepository.findByUploadedByIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FileDTO> getContractFiles(UUID contractId) {
        UUID userId = profileService.getCurrentUserId();
        Contract contract = contractRepository.findById(contractId)
                .orElseThrow(() -> new BusinessException("CONTRACT_NOT_FOUND", "Contrat non trouvé", contractId));

        if (!contract.getClient().getId().equals(userId) && !contract.getFreelance().getId().equals(userId)) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous n'êtes pas partie à ce contrat", userId);
        }

        return fileRepository.findByContractIdOrderByCreatedAtDesc(contractId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public void deleteFile(UUID fileId) {
        UUID userId = profileService.getCurrentUserId();
        FileEntity file = fileRepository.findById(fileId)
                .orElseThrow(() -> new BusinessException("FILE_NOT_FOUND", "Fichier non trouvé", fileId));

        boolean isUploader = file.getUploadedBy().getId().equals(userId);
        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode())))
                .orElse(false);

        if (!isUploader && !isAdmin) {
            throw new BusinessException("NOT_AUTHORIZED", "Vous ne pouvez supprimer que vos propres fichiers", userId);
        }

        try {
            Path filePath = Paths.get(uploadDir).resolve(file.getStoredName());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            log.warn("Could not delete physical file {}: {}", file.getStoredName(), e.getMessage());
        }

        fileRepository.delete(file);
        log.info("Fichier supprimé: {} par {}", fileId, userId);
    }

    private boolean canAccess(FileEntity file, UUID userId) {
        if (file.getUploadedBy().getId().equals(userId)) return true;

        boolean isAdmin = userRepository.findById(userId)
                .map(u -> u.getRoles().stream().anyMatch(r -> "ADMIN".equals(r.getCode())))
                .orElse(false);
        if (isAdmin) return true;

        if (file.getAccessType() == FileAccessType.PUBLIC) return true;

        if (file.getAccessType() == FileAccessType.CONTRACT_PARTIES && file.getContractId() != null) {
            Contract contract = contractRepository.findById(file.getContractId()).orElse(null);
            if (contract != null) {
                return contract.getClient().getId().equals(userId) || contract.getFreelance().getId().equals(userId);
            }
        }

        return false;
    }

    private FileDTO toDTO(FileEntity f) {
        return FileDTO.builder()
                .id(f.getId())
                .originalName(f.getOriginalName())
                .contentType(f.getContentType())
                .size(f.getSize())
                .accessType(f.getAccessType() != null ? f.getAccessType().name() : "PRIVATE")
                .contractId(f.getContractId())
                .projectId(f.getProjectId())
                .uploadedById(f.getUploadedBy().getId())
                .uploadedByName(f.getUploadedBy().getFullName())
                .createdAt(f.getCreatedAt())
                .build();
    }
}
