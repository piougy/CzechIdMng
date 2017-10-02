package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmSmsLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmSmsLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmSmsLog;

/**
 * 
 * @author Peter Šourek
 *
 */
@Service
public class DefaultIdmSmsLogService extends AbstractNotificationLogService<IdmSmsLogDto, IdmSmsLog, IdmNotificationFilter> implements IdmSmsLogService {

    @Autowired
    public DefaultIdmSmsLogService(AbstractEntityRepository<IdmSmsLog> repository) {
        super(repository);
    }

}
