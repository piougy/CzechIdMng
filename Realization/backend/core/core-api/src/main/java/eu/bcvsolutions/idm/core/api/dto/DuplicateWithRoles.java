package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Duplicates with {@link IdmConceptRoleRequestDto} and
 * {@link IdmIdentityRoleDto} or another {@link IdmConceptRoleRequestDto}
 *
 * @author Ondrej Kopr
 *
 */
public class DuplicateWithRoles implements BaseDto {

	private static final long serialVersionUID = 1L;

	private List<UUID> concepts;

	private List<UUID> identityRoles;

	@Override
	public Serializable getId() {
		return null;
	}

	@Override
	public void setId(Serializable id) {
	}

	public List<UUID> getConcepts() {
		if (concepts == null) {
			return new ArrayList<UUID>();
		}
		return concepts;
	}

	public void setConcepts(List<UUID> concepts) {
		this.concepts = concepts;
	}

	public List<UUID> getIdentityRoles() {
		if (identityRoles == null) {
			return new ArrayList<UUID>();
		}
		return identityRoles;
	}

	public void setIdentityRoles(List<UUID> identityRoles) {
		this.identityRoles = identityRoles;
	}

	public void addIdentityRole(UUID id) {
		List<UUID> duplicates = this.getIdentityRoles();
		duplicates.add(id);
		this.setIdentityRoles(duplicates);
	}

	public void addConcept(UUID id) {
		List<UUID> duplicates = this.getConcepts();
		duplicates.add(id);
		this.setConcepts(duplicates);
	}
}
