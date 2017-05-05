package eu.bcvsolutions.idm.core.notification.service.impl;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;
import eu.bcvsolutions.idm.core.notification.service.api.IdmSmsLogService;
import org.springframework.beans.factory.annotation.Autowired;

public class DefaultIdmSmsLogService extends AbstractNotificationLogService<IdmSmsLogDto, IdmSmsLog, NotificationFilter> implements IdmSmsLogService {

    @Autowired
    public DefaultIdmSmsLogService(AbstractEntityRepository<IdmSmsLog, NotificationFilter> repository) {
        super(repository);
    }

}
