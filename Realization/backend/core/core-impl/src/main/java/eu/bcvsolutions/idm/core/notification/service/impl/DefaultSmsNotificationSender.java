package eu.bcvsolutions.idm.core.notification.service.impl;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationDto;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;
import eu.bcvsolutions.idm.core.notification.service.api.IdmSmsLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


public class DefaultSmsNotificationSender extends AbstractNotificationSender<IdmSmsLogDto>{

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSmsNotificationSender.class);

    private final IdmSmsLogService idmSmsLogService;

    @Autowired
    public DefaultSmsNotificationSender(IdmSmsLogService idmSmsLogService) {
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
        LOG.info("SENDING");
        //TODO: inject service and do magic
        return createSms(notification);
    }

    private IdmSmsLogDto createSms(IdmNotificationDto notification) {

        IdmSmsLogDto smsLog = new IdmSmsLogDto(notification);

        return idmSmsLogService.save(smsLog);
    }

}
