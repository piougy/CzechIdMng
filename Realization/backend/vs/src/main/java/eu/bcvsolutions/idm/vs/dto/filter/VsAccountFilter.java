package eu.bcvsolutions.idm.vs.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.vs.dto.VsAccountDto;

/**
 * Filter for vs account
 * 
 * @author Svanda
 *
 */
public class VsAccountFilter extends DataFilter {

	String uid;
	UUID systemId;
	
	public VsAccountFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public VsAccountFilter(MultiValueMap<String, Object> data) {
		super(VsAccountDto.class, data);
	}
	
	public void setUid(String uidValue) {
		this.uid = uidValue;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public String getUid() {
		return uid;
	}

	public UUID getSystemId() {
		return systemId;
	}
	
}
