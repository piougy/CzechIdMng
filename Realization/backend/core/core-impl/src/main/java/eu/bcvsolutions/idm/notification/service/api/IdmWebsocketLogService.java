package eu.bcvsolutions.idm.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.notification.entity.IdmWebsocketLog;

/**
 * Websocket log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmWebsocketLogService extends ReadWriteEntityService<IdmWebsocketLog, NotificationFilter> {

}
