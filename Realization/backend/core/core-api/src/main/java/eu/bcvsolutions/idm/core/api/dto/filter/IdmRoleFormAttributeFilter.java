package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleFormAttributeDto;

/**
 * Filter for relation between role and definition of form-attribution. Is
 * elementary part of role form "subdefinition".
 * 
 * @author Vít Švanda
 *
 */
public class IdmRoleFormAttributeFilter extends DataFilter {

	public IdmRoleFormAttributeFilter() {
		this(new LinkedMultiValueMap<>());
	}

	public IdmRoleFormAttributeFilter(MultiValueMap<String, Object> data) {
		super(IdmRoleFormAttributeDto.class, data);
	}

	private UUID role;
	private UUID formDefinition;

	public UUID getFormDefinition() {
		return formDefinition;
	}

	public void setFormDefinition(UUID formDefinition) {
		this.formDefinition = formDefinition;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}
}
