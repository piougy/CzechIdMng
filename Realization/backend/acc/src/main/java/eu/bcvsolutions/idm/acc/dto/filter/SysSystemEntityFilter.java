package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;
import eu.bcvsolutions.idm.core.api.utils.ParameterConverter;

/**
 * Filter for entity on target system
 * 
 * @author Radek Tomiška
 *
 */
public class SysSystemEntityFilter extends DataFilter {
	
	private UUID systemId;	
	private String uid;	
	private SystemEntityType entityType;
	
	public SysSystemEntityFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSystemEntityFilter(MultiValueMap<String, Object> data) {
		this(data, null);
	}
	
	public SysSystemEntityFilter(MultiValueMap<String, Object> data, ParameterConverter parameterConverter) {
		super(SysSystemEntityDto.class, data, parameterConverter);
	}

	public UUID getSystemId() {
		return systemId;
	}

	public void setSystemId(UUID systemId) {
		this.systemId = systemId;
	}

	public SystemEntityType getEntityType() {
		return entityType;
	}

	public void setEntityType(SystemEntityType entityType) {
		this.entityType = entityType;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
