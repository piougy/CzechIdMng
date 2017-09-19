package eu.bcvsolutions.idm.core.api.audit.dto;

import java.io.Serializable;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.BaseDto;

/**
 * Dto for entity IdmLoggingEventException
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "loggingEventExceptions")
public class IdmLoggingEventExceptionDto implements BaseDto {

	private static final long serialVersionUID = 7785506028503517861L;

	private Long id;
	private Long event;
	private String traceLine;

	@Override
	public Serializable getId() {
		return id;
	}

	@Override
	public void setId(Serializable id) {
		this.id = (Long) id;
	}

	public Long getEvent() {
		return event;
	}

	public void setEvent(Long event) {
		this.event = event;
	}

	public String getTraceLine() {
		return traceLine;
	}

	public void setTraceLine(String traceLine) {
		this.traceLine = traceLine;
	}

}
