package eu.bcvsolutions.idm.example.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmNotificationLogDto;
import eu.bcvsolutions.idm.example.dto.Pong;

/**
 * Example business logic - interface
 * 
 * @author Radek Tomiška
 *
 */
public interface ExampleService {

	/**
	 * Returns given message.
	 * 
	 * @param message
	 * @return
	 */
	Pong ping(String message);
	
	/**
	 * Returns configuration property - private value.
	 * 
	 * @return private value
	 */
	String getPrivateValue();
	
	/**
	 * Sending given message to currently logged identity (example topic is used).
	 * 
	 * @param message
	 */
	List<IdmNotificationLogDto> sendNotification(String message);
}
