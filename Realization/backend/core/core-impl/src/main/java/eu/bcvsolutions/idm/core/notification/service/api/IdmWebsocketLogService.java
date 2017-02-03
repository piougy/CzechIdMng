package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;

/**
 * Websocket log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmWebsocketLogService extends ReadWriteEntityService<IdmWebsocketLog, NotificationFilter> {

}
