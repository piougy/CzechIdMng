package eu.bcvsolutions.idm.core.notification.api.service;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmWebsocketLogDto;

/**
 * Sends notifications through websocket
 * 
 * @author Radek Tomiška
 *
 */
public interface WebsocketNotificationSender extends NotificationSender<IdmWebsocketLogDto> {

}
