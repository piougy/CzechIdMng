package eu.bcvsolutions.idm.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmNotificationLog;
import eu.bcvsolutions.idm.notification.repository.IdmNotificationLogRepository;
import eu.bcvsolutions.idm.notification.service.api.IdmNotificationLogService;

/**
 * Notification log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmNotificationLogService extends AbstractReadWriteEntityService<IdmNotificationLog, NotificationFilter> implements IdmNotificationLogService {
	
	@Autowired
	public DefaultIdmNotificationLogService(IdmNotificationLogRepository repository) {
		super(repository);		
	}

}
