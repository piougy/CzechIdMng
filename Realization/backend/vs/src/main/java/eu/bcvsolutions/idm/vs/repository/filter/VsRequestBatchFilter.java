package eu.bcvsolutions.idm.vs.repository.filter;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.vs.service.api.dto.VsRequestBatchDto;

/**
 * Filter for vs request batch
 * 
 * @author Svanda
 *
 */
public class VsRequestBatchFilter extends DataFilter {
	
	public VsRequestBatchFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public VsRequestBatchFilter(MultiValueMap<String, Object> data) {
		super(VsRequestBatchDto.class, data);
	}

}
