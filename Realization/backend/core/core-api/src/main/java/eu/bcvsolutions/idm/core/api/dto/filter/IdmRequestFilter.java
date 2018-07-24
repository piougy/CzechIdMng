package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.RequestState;
import eu.bcvsolutions.idm.core.api.dto.IdmRequestDto;

/**
 * Filter for universal requests
 *
 * @author svandav
 */
public class IdmRequestFilter extends DataFilter {
	
	private List<RequestState> states;

	public IdmRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmRequestDto.class, data);
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
