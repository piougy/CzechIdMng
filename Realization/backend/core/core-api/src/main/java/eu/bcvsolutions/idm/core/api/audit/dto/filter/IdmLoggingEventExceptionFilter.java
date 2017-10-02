package eu.bcvsolutions.idm.core.api.audit.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.BaseFilter;

/**
 * Filter for IdmLoggingEventException
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class IdmLoggingEventExceptionFilter implements BaseFilter {

	private Long event;
	private Long id;

	public Long getEvent() {
		return event;
	}

	public void setEvent(Long event) {
		this.event = event;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

}
