package eu.bcvsolutions.idm.core.notification.api.domain;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;

import eu.bcvsolutions.idm.core.notification.api.dto.IdmEmailLogDto;

/**
 * Interface for transfer object to email event.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface SendOperation {

	/**
	 * Return email log
	 * 
	 * @return
	 */
	IdmEmailLogDto getEmailLog();

	/**
	 * Return endpoint
	 * 
	 * @return
	 */
	Endpoint getEndpoint();

	/**
	 * Return exchange
	 * 
	 * @return
	 */
	Exchange getExchange();

}
