package eu.bcvsolutions.idm.core.notification.domain;

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
	public IdmEmailLogDto getEmailLog();

	/**
	 * Return endpoint
	 * 
	 * @return
	 */
	public Endpoint getEndpoint();

	/**
	 * Return exchange
	 * 
	 * @return
	 */
	public Exchange getExchange();

}
