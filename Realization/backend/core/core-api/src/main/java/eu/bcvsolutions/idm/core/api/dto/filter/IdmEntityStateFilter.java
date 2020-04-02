package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for entity state.
 * 
 * @author Radek Tomiška
 *
 */
public class IdmEntityStateFilter extends DataFilter {
	
	public static final String PARAMETER_OWNER_TYPE = "ownerType";
	public static final String PARAMETER_OWNER_ID = "ownerId";
	public static final String PARAMETER_SUPER_OWNER_ID = "superOwnerId";
	public static final String PARAMETER_EVENT_ID = "eventId";
	public static final String PARAMETER_STATES = "states";
	public static final String PARAMETER_RESULT_CODE = "resultCode";

	public IdmEntityStateFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmEntityStateFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public IdmEntityStateFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(IdmEntityStateDto.class, data, parameterConverter);
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
		if (CollectionUtils.isEmpty(states)) {
    		data.remove(PARAMETER_STATES);
    	} else {
    		data.put(PARAMETER_STATES, new ArrayList<Object>(states));
    	}
	}
	
	public UUID getEventId() {
		return getParameterConverter().toUuid(getData(), PARAMETER_EVENT_ID);
	}
	
	public void setEventId(UUID eventId) {
		set(PARAMETER_EVENT_ID, eventId);
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
}
