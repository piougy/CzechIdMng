package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;

import org.springframework.hateoas.core.Relation;


/**
 * Logging event property dto. 
 * This DTO has not filled ID attribute (getter and setter for this attribute dot work),
 * Entity IdmLoggingEventProperty has composite primary key.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Relation(collectionRelation = "loggingEventProperties")
public class IdmLoggingEventPropertyDto implements BaseDto {

	private static final long serialVersionUID = -4858710204396564195L;

	private Long eventId;

	private String mappedKey;

	private String mappedValue;

	@Override
	public Serializable getId() {
		return null;
	}

	@Override
	public void setId(Serializable id) {
	}

	public Long getEventId() {
		return eventId;
	}

	public void setEventId(Long eventId) {
		this.eventId = eventId;
	}

	public String getMappedKey() {
		return mappedKey;
	}

	public void setMappedKey(String mappedKey) {
		this.mappedKey = mappedKey;
	}

	public String getMappedValue() {
		return mappedValue;
	}

	public void setMappedValue(String mappedValue) {
		this.mappedValue = mappedValue;
	}

}
