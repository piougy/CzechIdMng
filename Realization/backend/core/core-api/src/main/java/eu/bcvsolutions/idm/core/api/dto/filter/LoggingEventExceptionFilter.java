package eu.bcvsolutions.idm.core.api.dto.filter;

/**
 * Filter for IdmLoggingEventException
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class LoggingEventExceptionFilter implements BaseFilter {

	private Long event;

	public Long getEvent() {
		return event;
	}

	public void setEvent(Long event) {
		this.event = event;
	}

}
