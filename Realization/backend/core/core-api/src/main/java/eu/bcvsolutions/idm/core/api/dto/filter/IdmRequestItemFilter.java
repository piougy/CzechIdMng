package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;

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

}
