package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @since 7.6.0
 *
 */
@Relation(collectionRelation = "automaticRoleAttributeRules")
public class IdmAutomaticRoleAttributeRuleDto extends AbstractDto {

	private static final long serialVersionUID = -9191481914442485135L;

	@Embedded(dtoClass = IdmAutomaticRoleAttributeDto.class)
	private UUID automaticRoleAttribute;
	@Embedded(dtoClass = IdmFormAttributeDto.class)
	private UUID formAttribute;
	private String attributeName;
	private AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.IDENTITY;
	private String value;
	private AutomaticRoleAttributeRuleComparison comparison = AutomaticRoleAttributeRuleComparison.EQUALS;

	public UUID getAutomaticRoleAttribute() {
		return automaticRoleAttribute;
	}

	public void setAutomaticRoleAttribute(UUID automaticRoleAttribute) {
		this.automaticRoleAttribute = automaticRoleAttribute;
	}

	public UUID getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(UUID formAttribute) {
		this.formAttribute = formAttribute;
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
