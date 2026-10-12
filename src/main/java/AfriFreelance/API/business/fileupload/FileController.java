package AfriFreelance.API.business.fileupload;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Fichiers", description = "Upload, téléchargement et gestion des pièces jointes")
public class FileController {

    private final FileService fileService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Uploader un fichier (pièce jointe, livrable, document)")
    public ResponseEntity<FileDTO> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "accessType", required = false) String accessType,
            @RequestParam(value = "contractId", required = false) UUID contractId,
            @RequestParam(value = "projectId", required = false) UUID projectId) {
        return ResponseEntity.ok(fileService.uploadFile(file, accessType, contractId, projectId));
    }

    @GetMapping("/{fileId}/download")
    @Operation(summary = "Télécharger un fichier (vérifie les droits d'accès)")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
        FileDTO fileMeta = fileService.getMyFiles().stream()
                .filter(f -> f.getId().equals(fileId))
                .findFirst()
                .orElse(null);

        Resource resource = fileService.downloadFile(fileId);
        String contentType = fileMeta != null ? fileMeta.getContentType() : "application/octet-stream";
        String filename = fileMeta != null ? fileMeta.getOriginalName() : "download";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @GetMapping("/me")
    @Operation(summary = "Obtenir mes fichiers uploadés")
    public ResponseEntity<List<FileDTO>> getMyFiles() {
        return ResponseEntity.ok(fileService.getMyFiles());
    }

    @GetMapping("/contract/{contractId}")
    @Operation(summary = "Obtenir les fichiers d'un contrat (client ou freelance du contrat)")
    public ResponseEntity<List<FileDTO>> getContractFiles(@PathVariable UUID contractId) {
        return ResponseEntity.ok(fileService.getContractFiles(contractId));
    }

    @DeleteMapping("/{fileId}")
    @Operation(summary = "Supprimer un fichier (uploader ou admin)")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
        fileService.deleteFile(fileId);
        return ResponseEntity.noContent().build();
    }
}
