package eu.bcvsolutions.idm.core.api.dto.filter;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
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
	private UUID superOwnerId;
	private UUID eventId;
	private ZonedDateTime createdFrom;
    private ZonedDateTime createdTill;
    private String resultCode;
    private List<OperationState> states;

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

	public ZonedDateTime getCreatedFrom() {
		return createdFrom;
	}

	public void setCreatedFrom(ZonedDateTime createdFrom) {
		this.createdFrom = createdFrom;
	}

	public ZonedDateTime getCreatedTill() {
		return createdTill;
	}

	public void setCreatedTill(ZonedDateTime createdTill) {
		this.createdTill = createdTill;
	}
	
	public UUID getSuperOwnerId() {
		return superOwnerId;
	}
	
	public void setSuperOwnerId(UUID superOwnerId) {
		this.superOwnerId = superOwnerId;
	}
	
	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}
	
	public String getResultCode() {
		return resultCode;
	}

	public List<OperationState> getStates() {
		if (states == null) {
			states = new ArrayList<>();
		}
		return states;
	}
	
	public void setStates(List<OperationState> states) {
		this.states = states;
	}
}
