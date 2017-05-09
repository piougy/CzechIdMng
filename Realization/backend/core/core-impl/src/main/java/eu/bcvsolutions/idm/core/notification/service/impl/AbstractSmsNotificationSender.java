package eu.bcvsolutions.idm.core.notification.service.impl;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;
import eu.bcvsolutions.idm.core.notification.service.api.IdmSmsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * This class exists just to provide basic functionality connected to sending sms notifications
 * such as saving SmsNotificationLog. Each concrete implementation of this class must override method sendSms
 * which does actual sending of the sms.
 *
 * @author Peter Sourek
 */
public abstract class AbstractSmsNotificationSender extends AbstractNotificationSender<IdmSmsLogDto>{

    private final IdmSmsLogService idmSmsLogService;

    public AbstractSmsNotificationSender(IdmSmsLogService idmSmsLogService) {
        Assert.notNull(idmSmsLogService);
        this.idmSmsLogService = idmSmsLogService;
    }

    @Override
    public String getType() {
        return IdmSmsLog.NOTIFICATION_TYPE;
    }

    @Override
    public IdmSmsLogDto send(final IdmNotificationDto notification) {
        Assert.notNull(notification, "Notification is required!");
        sendSms(notification);
        return createLog(notification);
    }

    abstract void sendSms(IdmNotificationDto notification);

    private IdmSmsLogDto createLog(IdmNotificationDto notification) {
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
	            /*TODO: use sms number here?*/recipient.getRealRecipient())));
        smsLogDto.setIdentitySender(notification.getIdentitySender());
        smsLogDto.setSent(notification.getSent());
        return idmSmsLogService.save(smsLogDto);
    }

}
