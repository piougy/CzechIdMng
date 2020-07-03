package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for entity events (changes).
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityEventFilter extends DataFilter {
	
	public static final String PARAMETER_ROOT_ID = "rootId";
	public static final String PARAMETER_PARENT_ID = "parentId";
	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_SUPER_OWNER_ID = "superOwnerId";
	public static final String PARAMETER_STATES = "states";
	public static final String PARAMETER_PRIORITY = "priority";
	public static final String PARAMETER_RESULT_CODE = "resultCode";
	public static final String PARAMETER_EVENT_TYPE = "eventType";
	
	public IdmEntityEventFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmEntityEventFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmEntityEventFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmEntityEventDto.class, data, parameterConverter);
	}

	public String getOwnerType() {
		return getParameterConverter().toString(getData(), PARAMETER_OWNER_TYPE);
	}

	public void setOwnerType(String ownerType) {
		set(PARAMETER_OWNER_TYPE, ownerType);
	}

	public UUID getOwnerId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_OWNER_ID);
	}

	public void setOwnerId(UUID ownerId) {
		set(PARAMETER_OWNER_ID, ownerId);
	}
	
	public List<OperationState> getStates() {
		return getParameterConverter().toEnums(getData(), PARAMETER_STATES, OperationState.class);
	}
	
	public void setStates(List<OperationState> states) {
		put(PARAMETER_STATES, states);
	}
	
	public UUID getParentId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_PARENT_ID);
	}
	
	public void setParentId(UUID parentId) {
		set(PARAMETER_PARENT_ID, parentId);
	}
	
	public void setPriority(PriorityType priority) {
		set(PARAMETER_PRIORITY, priority);
	}
	
	public PriorityType getPriority() {
		return getParameterConverter().toEnum(getData(), PARAMETER_PRIORITY, PriorityType.class);
	}
	
	public void setRootId(UUID rootId) {
		set(PARAMETER_ROOT_ID, rootId);
	}
	
	public UUID getRootId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_ROOT_ID);
	}
	
	public UUID getSuperOwnerId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_SUPER_OWNER_ID);
	}
	
	public void setSuperOwnerId(UUID superOwnerId) {
		set(PARAMETER_SUPER_OWNER_ID, superOwnerId);
	}
	
	public void setResultCode(String resultCode) {
		set(PARAMETER_RESULT_CODE, resultCode);
	}
	
	public String getResultCode() {
		return getParameterConverter().toString(getData(), PARAMETER_RESULT_CODE);
	}
	
	public String getEventType() {
		return getParameterConverter().toString(getData(), PARAMETER_EVENT_TYPE);
	}
	
	public void setEventType(String eventType) {
		set(PARAMETER_EVENT_TYPE, eventType);
	}
}
