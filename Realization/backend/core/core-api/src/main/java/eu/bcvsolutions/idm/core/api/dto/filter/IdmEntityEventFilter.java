package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;

/**
 * Filter for entity events (changes)
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityEventFilter extends DataFilter {
	
	private String ownerType;
	private UUID ownerId;
	private DateTime createdFrom; // >=
    private DateTime createdTill; // <=
    private List<OperationState> states;
    private UUID parentId;
    private PriorityType priority;
	
	public IdmEntityEventFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmEntityEventFilter(MultiValueMap<String, Object> data) {
		super(IdmEntityEventDto.class, data);
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
	
	public List<OperationState> getStates() {
		if (states == null) {
			states = new ArrayList<>();
		}
		return states;
	}
	
	public void setStates(List<OperationState> states) {
		this.states = states;
	}
	
	public UUID getParentId() {
		return parentId;
	}
	
	public void setParentId(UUID parentId) {
		this.parentId = parentId;
	}
	
	public void setPriority(PriorityType priority) {
		this.priority = priority;
	}
	
	public PriorityType getPriority() {
		return priority;
	}
}
