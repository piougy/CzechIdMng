package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestItemDto;

/**
 * Filter for request's items
 *
 * @author svandav
 */
public class IdmRequestItemFilter extends DataFilter {
	
	private List<RequestState> states;
	private UUID requestId;
	private UUID originalOwnerId;
	private String originalType;

	public IdmRequestItemFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmRequestItemFilter(MultiValueMap<String, Object> data) {
		super(IdmRequestItemDto.class, data);
	}

	public List<RequestState> getStates() {
		if (states == null) {
			states = new ArrayList<>();
		}
		return states;
	}

	public void setStates(List<RequestState> states) {
		this.states = states;
	}

	public UUID getRequestId() {
		return requestId;
	}

	public void setRequestId(UUID requestId) {
		this.requestId = requestId;
	}

	public UUID getOriginalOwnerId() {
		return originalOwnerId;
	}

	public void setOriginalOwnerId(UUID originalOwnerId) {
		this.originalOwnerId = originalOwnerId;
	}

	public String getOriginalType() {
		return originalType;
	}

	public void setOriginalType(String originalType) {
		this.originalType = originalType;
	}

}
