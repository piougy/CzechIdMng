package eu.bcvsolutions.idm.vs.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.vs.dto.VsSystemImplementerDto;

/**
 * Filter for VS request implementer
 * 
 * @author Svanda
 *
 */
public class VsSystemImplementerFilter extends DataFilter {

	UUID systemId;
	UUID identityId;

	public VsSystemImplementerFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public VsSystemImplementerFilter(MultiValueMap<String, Object> data) {
		super(VsSystemImplementerDto.class, data);
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public UUID getIdentityId() {
		return identityId;
	}

	public void setIdentityId(UUID identityId) {
		this.identityId = identityId;
	}
}
