package eu.bcvsolutions.idm.core.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmWebsocketLogDto;
import eu.bcvsolutions.idm.core.notification.api.dto.filter.IdmNotificationFilter;
import eu.bcvsolutions.idm.core.notification.api.service.IdmWebsocketLogService;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.core.notification.repository.IdmWebsocketLogRepository;

/**
 * Websocket log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmWebsocketLogService 
		extends AbstractNotificationLogService<IdmWebsocketLogDto, IdmWebsocketLog, IdmNotificationFilter> 
		implements IdmWebsocketLogService {

	
	@Autowired
	public DefaultIdmWebsocketLogService(IdmWebsocketLogRepository repository) {
		super(repository);		
	}

}
