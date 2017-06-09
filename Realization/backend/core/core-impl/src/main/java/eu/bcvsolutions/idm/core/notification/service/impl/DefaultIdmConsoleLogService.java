package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmConsoleLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmConsoleLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmConsoleLogService;

/**
 * Console log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmConsoleLogService extends AbstractNotificationLogService<IdmConsoleLogDto, IdmConsoleLog, NotificationFilter> implements IdmConsoleLogService {
	
	@Autowired
	public DefaultIdmConsoleLogService(IdmConsoleLogRepository repository) {
		super(repository);		
	}

}
