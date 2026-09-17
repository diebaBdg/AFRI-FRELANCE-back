package AfriFreelance.API.business.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreferenceDTO {
    private UUID id;
    private Boolean emailMessages;
    private Boolean emailProjectUpdates;
    private Boolean emailMarketing;
    private Boolean pushMessages;
    private Boolean pushProjectUpdates;
    private Boolean pushContractUpdates;
    private Boolean pushPaymentUpdates;
}
