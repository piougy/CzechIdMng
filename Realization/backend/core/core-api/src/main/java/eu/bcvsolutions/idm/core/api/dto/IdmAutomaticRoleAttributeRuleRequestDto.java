package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleComparison;
import eu.bcvsolutions.idm.core.api.domain.AutomaticRoleAttributeRuleType;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.RequestOperationType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * DTO Request defined rule for automatic role that is assignment by attribute
 * 
 * @author svandav
 * @since 8.0.0
 *
 */


@Relation(collectionRelation = "automaticRoleAttributeRuleRequests")
public class IdmAutomaticRoleAttributeRuleRequestDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@NotNull
	@Embedded(dtoClass = IdmAutomaticRoleRequestDto.class)
	private UUID request;
	@Embedded(dtoClass = IdmFormAttributeDto.class)
	private UUID formAttribute;
	@Size(max = DefaultFieldLengths.NAME)
	private String attributeName;
	private AutomaticRoleAttributeRuleType type = AutomaticRoleAttributeRuleType.IDENTITY;
	@Size(max = DefaultFieldLengths.LOG)
	private String value;
	private AutomaticRoleAttributeRuleComparison comparison = AutomaticRoleAttributeRuleComparison.EQUALS;
	@NotNull
	private RequestOperationType operation;
	@Embedded(dtoClass = IdmAutomaticRoleAttributeRuleDto.class)
	private UUID rule;

	public UUID getRequest() {
		return request;
	}

	public void setRequest(UUID request) {
		this.request = request;
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

	public RequestOperationType getOperation() {
		return operation;
	}

	public void setOperation(RequestOperationType operation) {
		this.operation = operation;
	}

	public UUID getRule() {
		return rule;
	}

	public void setRule(UUID rule) {
		this.rule = rule;
	}
}
