package AfriFreelance.API.business.moderation;

import AfriFreelance.API.business.moderation.dtos.*;
import AfriFreelance.API.business.profile.ProfileService;
import AfriFreelance.API.business.user.User;
import AfriFreelance.API.business.user.UserRepository;
import AfriFreelance.API.config.exceptions.BusinessException;
import AfriFreelance.API.enums.ReportStatus;
import AfriFreelance.API.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ModerationService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final ProfileService profileService;

    public ReportDTO createReport(ReportRequest request) {
        UUID reporterId = profileService.getCurrentUserId();
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", reporterId));

        if (reportRepository.existsByReporterIdAndReportTypeAndTargetId(reporterId, request.getReportType(), request.getTargetId())) {
            throw new BusinessException("ALREADY_REPORTED", "Vous avez déjà signalé cet élément", request.getTargetId());
        }

        Report report = Report.builder()
                .reporter(reporter)
                .reportType(request.getReportType())
                .targetId(request.getTargetId())
                .reason(request.getReason())
                .description(request.getDescription())
                .status(ReportStatus.PENDING)
                .build();

        report = reportRepository.save(report);
        log.info("Signalement créé: {} par {} contre {} {}", report.getId(), reporterId, request.getReportType(), request.getTargetId());
        return toDTO(report);
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getMyReports() {
        UUID userId = profileService.getCurrentUserId();
        return reportRepository.findByReporterIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getReportsByStatus(ReportStatus status) {
        requireAdmin();
        return reportRepository.findByStatusOrderByCreatedAtDesc(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReportDTO> getAllReports() {
        requireAdmin();
        return reportRepository.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ReportDTO resolveReport(UUID reportId, ReportResolutionRequest request) {
        UUID adminId = requireAdmin();
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new BusinessException("REPORT_NOT_FOUND", "Signalement non trouvé", reportId));

        report.setStatus(request.getStatus());
        report.setResolutionNote(request.getResolutionNote());
        report.setResolvedAt(LocalDateTime.now());

        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Admin non trouvé", adminId));
        report.setResolvedBy(admin);

        if (request.getStatus() == ReportStatus.RESOLVED && report.getReportType().name().equals("USER")) {
            userRepository.findById(report.getTargetId()).ifPresent(reportedUser -> {
                reportedUser.setStatus(UserStatus.SUSPENDU);
                userRepository.save(reportedUser);
                log.info("Utilisateur {} suspendu suite au signalement {}", reportedUser.getId(), reportId);
            });
        }

        report = reportRepository.save(report);
        return toDTO(report);
    }

    private UUID requireAdmin() {
        UUID userId = profileService.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "Utilisateur non trouvé", userId));
        boolean isAdmin = user.getRoles().stream()
                .anyMatch(r -> "ADMIN".equals(r.getCode()));
        if (!isAdmin) {
            throw new BusinessException("NOT_ADMIN", "Accès réservé aux administrateurs", userId);
        }
        return userId;
    }

    private ReportDTO toDTO(Report r) {
        return ReportDTO.builder()
                .id(r.getId())
                .reporterId(r.getReporter().getId())
                .reporterName(r.getReporter().getFullName())
                .reportType(r.getReportType() != null ? r.getReportType().name() : null)
                .targetId(r.getTargetId())
                .reason(r.getReason() != null ? r.getReason().name() : null)
                .description(r.getDescription())
                .status(r.getStatus() != null ? r.getStatus().name() : "PENDING")
                .resolvedById(r.getResolvedBy() != null ? r.getResolvedBy().getId() : null)
                .resolvedByName(r.getResolvedBy() != null ? r.getResolvedBy().getFullName() : null)
                .resolutionNote(r.getResolutionNote())
                .createdAt(r.getCreatedAt())
                .resolvedAt(r.getResolvedAt())
                .build();
    }
}
