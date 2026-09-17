package AfriFreelance.API.business.notification.dto;

import lombok.Data;

@Data
public class NotificationPreferenceRequest {
    private Boolean emailMessages;
    private Boolean emailProjectUpdates;
    private Boolean emailMarketing;
    private Boolean pushMessages;
    private Boolean pushProjectUpdates;
    private Boolean pushContractUpdates;
    private Boolean pushPaymentUpdates;
}
