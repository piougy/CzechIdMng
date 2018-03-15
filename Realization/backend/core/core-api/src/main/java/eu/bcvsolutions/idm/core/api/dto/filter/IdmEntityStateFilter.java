package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;

/**
 * Filter for entity state
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityStateFilter extends DataFilter {
	
	private String ownerType;
	private UUID ownerId;
	private UUID eventId;
	private DateTime createdFrom;
    private DateTime createdTill;
	
	public IdmEntityStateFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmEntityStateFilter(MultiValueMap<String, Object> data) {
		super(IdmEntityStateDto.class, data);
	}

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public UUID getOwnerId() {
		return ownerId;
	}

	public void setOwnerId(UUID ownerId) {
		this.ownerId = ownerId;
	}
	
	public UUID getEventId() {
		return eventId;
	}
	
	public void setEventId(UUID eventId) {
		this.eventId = eventId;
	}

	public DateTime getCreatedFrom() {
		return createdFrom;
	}

	public void setCreatedFrom(DateTime createdFrom) {
		this.createdFrom = createdFrom;
	}

	public DateTime getCreatedTill() {
		return createdTill;
	}

	public void setCreatedTill(DateTime createdTill) {
		this.createdTill = createdTill;
	}
}
