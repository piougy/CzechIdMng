package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;

/**
 * Filter for automatic rule role request
 *
 * @author svandav
 */
public class IdmAutomaticRoleAttributeRuleRequestFilter extends DataFilter {
	
    private UUID roleRequestId;
    private UUID roleId;
    private UUID automaticRoleId;
    private UUID formAttributeId;
    private UUID ruleId;
    
    public IdmAutomaticRoleAttributeRuleRequestFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAutomaticRoleAttributeRuleRequestFilter(MultiValueMap<String, Object> data) {
		super(IdmConceptRoleRequestDto.class, data);
	}

    public UUID getRoleRequestId() {
        return roleRequestId;
    }

    public void setRoleRequestId(UUID roleRequestId) {
        this.roleRequestId = roleRequestId;
    }

    public UUID getRoleId() {
        return roleId;
    }

    public void setRoleId(UUID roleId) {
        this.roleId = roleId;
    }

	public UUID getFormAttributeId() {
		return formAttributeId;
	}

	public void setFormAttributeId(UUID formAttributeId) {
		this.formAttributeId = formAttributeId;
	}

	public UUID getAutomaticRoleId() {
		return automaticRoleId;
	}

	public void setAutomaticRoleId(UUID automaticRoleId) {
		this.automaticRoleId = automaticRoleId;
	}

	public UUID getRuleId() {
		return ruleId;
	}

	public void setRuleId(UUID ruleId) {
		this.ruleId = ruleId;
	}
    
}
