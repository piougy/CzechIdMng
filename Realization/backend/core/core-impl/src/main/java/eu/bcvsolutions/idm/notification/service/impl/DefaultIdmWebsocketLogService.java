package eu.bcvsolutions.idm.notification.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmWebsocketLog;
import eu.bcvsolutions.idm.notification.repository.IdmWebsocketLogRepository;
import eu.bcvsolutions.idm.notification.service.api.IdmWebsocketLogService;

/**
 * Websocket log service
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmWebsocketLogService extends AbstractReadWriteEntityService<IdmWebsocketLog, NotificationFilter> implements IdmWebsocketLogService {
	
	@Autowired
	public DefaultIdmWebsocketLogService(IdmWebsocketLogRepository repository) {
		super(repository);		
	}

}
