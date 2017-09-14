package eu.bcvsolutions.idm.core.notification.repository;

import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;

/**
 * Repository for sent messages through websocket
 * 
 * @author Radek Tomiška 
 *
 */
public interface IdmWebsocketLogRepository extends AbstractEntityRepository<IdmWebsocketLog> {
	
}
