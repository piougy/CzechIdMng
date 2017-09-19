package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmConsoleLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmConsoleLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmConsoleLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmConsoleLogRepository;

/**
 * Console log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmConsoleLogService 
		extends AbstractNotificationLogService<IdmConsoleLogDto, IdmConsoleLog, IdmNotificationFilter> 
		implements IdmConsoleLogService {
	
	@Autowired
	public DefaultIdmConsoleLogService(IdmConsoleLogRepository repository) {
		super(repository);		
	}

}
