package eu.bcvsolutions.idm.acc.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.dto.filter.DataFilter;

/**
 * Filter for systems
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public class SysSystemFilter extends DataFilter {
	
	private Boolean virtual;	
	private UUID passwordPolicyValidationId;
	private UUID passwordPolicyGenerationId;
	
	public SysSystemFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public SysSystemFilter(MultiValueMap<String, Object> data) {
		super(SysSystemDto.class, data);
	}

	public UUID getPasswordPolicyValidationId() {
		return passwordPolicyValidationId;
	}

	public void setPasswordPolicyValidationId(UUID passwordPolicyValidationId) {
		this.passwordPolicyValidationId = passwordPolicyValidationId;
	}

	public UUID getPasswordPolicyGenerationId() {
		return passwordPolicyGenerationId;
	}

	public void setPasswordPolicyGenerationId(UUID passwordPolicyGenerationId) {
		this.passwordPolicyGenerationId = passwordPolicyGenerationId;
	}

	public Boolean getVirtual() {
		return virtual;
	}

	public void setVirtual(Boolean virtual) {
		this.virtual = virtual;
	}
}
