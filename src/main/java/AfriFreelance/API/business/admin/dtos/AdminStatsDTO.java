package AfriFreelance.API.business.admin.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminStatsDTO {
    private long totalUsers;
    private long totalFreelancers;
    private long totalClients;
    private long totalProjects;
    private long openProjects;
    private long inProgressProjects;
    private long completedProjects;
    private long totalProposals;
    private long totalContracts;
    private long activeContracts;
    private long totalPayments;
    private double totalVolume;
    private double totalCommission;
    private long totalReports;
    private long pendingReports;
}
