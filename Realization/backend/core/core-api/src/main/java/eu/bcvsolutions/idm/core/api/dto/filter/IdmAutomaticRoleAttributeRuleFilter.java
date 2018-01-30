package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeRuleDto;

/**
 * Filter rules for automatic role by attribute
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.7.0
 *
 */

public class IdmAutomaticRoleAttributeRuleFilter extends DataFilter {

	private UUID automaticRoleAttributeId;
	private UUID formAttributeId;
	private String attributeName;
	private AutomaticRoleAttributeRuleType type;
	private String value;
	private AutomaticRoleAttributeRuleComparison comparison;
	
	public IdmAutomaticRoleAttributeRuleFilter() {
		this(new LinkedMultiValueMap<>());
	}
	
	public IdmAutomaticRoleAttributeRuleFilter(Class<? extends BaseDto> dtoClass, MultiValueMap<String, Object> data) {
		super(dtoClass, data);
	}
	
	public IdmAutomaticRoleAttributeRuleFilter(MultiValueMap<String, Object> data) {
		super(IdmAutomaticRoleAttributeRuleDto.class, data);
	}

	public UUID getAutomaticRoleAttributeId() {
		return automaticRoleAttributeId;
	}

	public void setAutomaticRoleAttributeId(UUID automaticRoleAttributeId) {
		this.automaticRoleAttributeId = automaticRoleAttributeId;
	}

	public UUID getFormAttributeId() {
		return formAttributeId;
	}

	public void setFormAttributeId(UUID formAttributeId) {
		this.formAttributeId = formAttributeId;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}

	public AutomaticRoleAttributeRuleType getType() {
		return type;
	}

	public void setType(AutomaticRoleAttributeRuleType type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public AutomaticRoleAttributeRuleComparison getComparison() {
		return comparison;
	}

	public void setComparison(AutomaticRoleAttributeRuleComparison comparison) {
		this.comparison = comparison;
	}
	
	
}
