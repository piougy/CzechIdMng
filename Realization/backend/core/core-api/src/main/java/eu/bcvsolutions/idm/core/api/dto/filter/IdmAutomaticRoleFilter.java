package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;

/**
 * Filter for automatic roles
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.7.0
 *
 */

public class IdmAutomaticRoleFilter extends DataFilter {

	private UUID roleId;
	private String name;
	/*
	 * Automatic role attribute has at least one rule with this type
	 */
	private AutomaticRoleAttributeRuleType ruleType;
	/*
	 * Automatic role attribute has at least one rule
	 */
	private Boolean hasRules;
	
	private Boolean concept;

	public IdmAutomaticRoleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAutomaticRoleFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		super(dtoClass, data);
	}
	
	public IdmAutomaticRoleFilter(MultiValueMap<String, Object> data) {
		super(IdmAutomaticRoleAttributeDto.class, data);
	}

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean getHasRules() {
		return hasRules;
	}

	public void setHasRules(Boolean hasRules) {
		this.hasRules = hasRules;
	}

	public AutomaticRoleAttributeRuleType getRuleType() {
		return ruleType;
	}

	public void setRuleType(AutomaticRoleAttributeRuleType ruleType) {
		this.ruleType = ruleType;
	}

	public Boolean getConcept() {
		return concept;
	}

	public void setConcept(Boolean concept) {
		this.concept = concept;
	}
}
