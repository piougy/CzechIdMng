package eu.bcvsolutions.idm.core.api.dto.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;

/**
 * Filter for {@link IdmContractSliceDto} dtos.
 *
 * @author svandav
 */
public class IdmContractSliceFilter extends IdmIdentityContractFilter implements CorrelationFilter{

	public IdmContractSliceFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmContractSliceFilter(MultiValueMap<String, Object> data) {
		super(IdmContractSliceDto.class, data);
	}
}
