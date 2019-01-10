package eu.bcvsolutions.idm.core.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.Requestable;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Relation between role and definition of form-attribution. Is elementary part
 * of role form "sub-definition".
 * 
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "roleFormAttributes")
public class IdmRoleFormAttributeDto extends AbstractDto implements Requestable {

	private static final long serialVersionUID = 1L;
	//
	@Embedded(dtoClass = IdmFormAttributeDto.class)
	private UUID formAttribute;
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	private String defaultValue;
	private boolean unique;
	private BigDecimal max;
	private BigDecimal min;
	private String regex;
	private boolean required;
	@Embedded(dtoClass = IdmRequestItemDto.class)
	private UUID requestItem; // Isn't persist in the entity

	public IdmRoleFormAttributeDto() {
	}

	public IdmRoleFormAttributeDto(UUID id) {
		super(id);
	}

	public UUID getFormAttribute() {
		return formAttribute;
	}

	public void setFormAttribute(UUID formAttribute) {
		this.formAttribute = formAttribute;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public BigDecimal getMax() {
		return max;
	}

	public void setMax(BigDecimal max) {
		this.max = max;
	}

	public BigDecimal getMin() {
		return min;
	}

	public void setMin(BigDecimal min) {
		this.min = min;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}
	
	@Override
	public UUID getRequestItem() {
		return requestItem;
	}

	@Override
	public void setRequestItem(UUID requestItem) {
		this.requestItem = requestItem;
	}
}