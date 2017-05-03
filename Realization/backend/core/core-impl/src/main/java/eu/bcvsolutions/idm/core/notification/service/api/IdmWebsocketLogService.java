package eu.bcvsolutions.idm.core.notification.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmWebsocketLogDto;
import eu.bcvsolutions.idm.core.notification.dto.filter.NotificationFilter;
import eu.bcvsolutions.idm.core.notification.entity.IdmWebsocketLog;

/**
 * Websocket log service
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmWebsocketLogService extends ReadWriteDtoService<IdmWebsocketLogDto, IdmWebsocketLog, NotificationFilter> {

}
