package eu.bcvsolutions.idm.core.notification.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationRecipientDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.api.service.IdmSmsLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;

/**
 * This class exists just to provide basic functionality connected to sending sms notifications
 * such as saving SmsNotificationLog. Each concrete implementation of this class must override method sendSms
 * which does actual sending of the sms.
 *
 * @author Peter Sourek
 */
public abstract class AbstractSmsNotificationSender extends AbstractNotificationSender<IdmSmsLogDto>{

    private final IdmSmsLogService idmSmsLogService;

    private final IdmIdentityService identityService;

    public AbstractSmsNotificationSender(IdmSmsLogService idmSmsLogService, IdmIdentityService identityService) {
        Assert.notNull(idmSmsLogService);
        Assert.notNull(identityService);
        //
        this.idmSmsLogService = idmSmsLogService;
        this.identityService = identityService;
    }

    @Override
    public String getType() {
        return IdmSmsLog.NOTIFICATION_TYPE;
    }

    @Override
    public IdmSmsLogDto send(final IdmNotificationDto notification) {
        Assert.notNull(notification, "Notification is required!");
        List<IdmSmsLogDto> results = sendSms(notification);
        return createLogAndSave(merge(results));
    }

    private IdmSmsLogDto createLogAndSave(IdmNotificationDto notification) {
        final IdmSmsLogDto smsLogDto = createLog(notification);
        return idmSmsLogService.save(smsLogDto);
    }

    // TODO: Remove once partially sent notifications are implemented.
    /**
     *
     * This method is here only until partially sent notifications are implemented.
     * It is basically a workaround for merging all sent notifications (one for each recipient)
     * to one.
     *
     * If one of the given notifications is not sent ({@link IdmSmsLogDto#getSent()} returns null), then returned
     * notification will also be marked as not sent. If all notifications have attribute sent set, then result notification
     * log will have the smallest value of them all. The method also concatenates sent logs.
     *
     * All notifications should have same attributes, except for recipient and sent. This method does no check for this att all,
     * so it is solely on programmer to make sure that all notifications are the same.
     *
     * All recipients will be merged into one.
     *
     * @param smsLogs {@link IdmSmsLogDto} instances to be merged into one
     * @return Merged {@link IdmSmsLogDto} or null if no notifications were passed into method
     */
    protected IdmSmsLogDto merge(List<IdmSmsLogDto> smsLogs) {
        if (smsLogs == null || smsLogs.isEmpty()) {
            return null;
        }

        final IdmSmsLogDto result = createLog(smsLogs.get(0));
        // clear recipients so the first one is not present twice
        result.setRecipients(new ArrayList<>());

        for (IdmSmsLogDto log: smsLogs) {
            result.getRecipients().addAll(log.getRecipients());
            result.setSent(result.getSent() == null ? null : log.getSent());
            // Set message
            if (!StringUtils.isEmpty(log.getSentLog())) {
                result.setSentLog(result.getSentLog() == null ? (result.getSentLog() + " | ") : "" + log.getSentLog());
            }
        }

        return result;
    }

    protected IdmSmsLogDto createLog(IdmNotificationDto notification) {
        Assert.notNull(notification);
        Assert.notNull(notification.getMessage());
        //
        IdmSmsLogDto smsLogDto = new IdmSmsLogDto();
        // parent message
        if (notification.getId() != null) {
            smsLogDto.setParent(notification.getId());
        }
        // clone message
        smsLogDto.setMessage(cloneMessage(notification));
        // clone recipients - resolve real email
        notification.getRecipients().forEach(recipient ->
            smsLogDto.getRecipients().add(cloneRecipient(smsLogDto, recipient,
	            getSmsNumber(recipient))));
        smsLogDto.setIdentitySender(notification.getIdentitySender());
        smsLogDto.setSent(notification.getSent());
        smsLogDto.setSentLog(notification.getSentLog());
        return smsLogDto;
    }

    private String getSmsNumber(IdmNotificationRecipientDto recipient) {
        final IdmIdentityDto identityDto = identityService.get(recipient.getIdentityRecipient());
        if (identityDto == null) {
            return null;
        }
        return identityDto.getPhone();
    }

    public abstract List<IdmSmsLogDto> sendSms(IdmNotificationDto notification);

}
