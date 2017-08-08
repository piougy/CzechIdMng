package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmWebsocketLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmWebsocketLogRepository;
import eu.bcvsolutions.idm.core.notification.service.api.IdmWebsocketLogService;

/**
 * Websocket log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmWebsocketLogService 
		extends AbstractNotificationLogService<IdmWebsocketLogDto, IdmWebsocketLog, NotificationFilter> 
		implements IdmWebsocketLogService {

	
	@Autowired
	public DefaultIdmWebsocketLogService(IdmWebsocketLogRepository repository) {
		super(repository);		
	}

}
