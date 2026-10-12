package AfriFreelance.API.business.moderation;

import AfriFreelance.API.enums.ReportStatus;
import AfriFreelance.API.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {

    List<Report> findByReporterIdOrderByCreatedAtDesc(UUID reporterId);

    List<Report> findByStatusOrderByCreatedAtDesc(ReportStatus status);

    List<Report> findByReportTypeAndTargetId(ReportType reportType, UUID targetId);

    boolean existsByReporterIdAndReportTypeAndTargetId(UUID reporterId, ReportType reportType, UUID targetId);
}
