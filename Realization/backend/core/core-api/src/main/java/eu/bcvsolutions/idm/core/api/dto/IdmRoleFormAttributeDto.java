package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Relation between role and definition of form-attribution. Is elementary part
 * of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "roleFormAttributes")
public class IdmRoleFormAttributeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	@Embedded(dtoClass = IdmFormAttributeDto.class)
	private UUID formAttribute;
	@Embedded(dtoClass = IdmRoleDto.class)
	private UUID role;
	private String defaultValue;

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
}