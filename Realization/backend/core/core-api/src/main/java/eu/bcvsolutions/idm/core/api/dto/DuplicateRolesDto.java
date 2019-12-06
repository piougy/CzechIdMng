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
 * @since 9.6.0
 */
public class DuplicateRolesDto implements BaseDto {

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
			concepts = new ArrayList<UUID>();
		}
		return concepts;
	}

	public void setConcepts(List<UUID> concepts) {
		this.concepts = concepts;
	}

	public List<UUID> getIdentityRoles() {
		if (identityRoles == null) {
			identityRoles = new ArrayList<UUID>();
		}
		return identityRoles;
	}

	public void setIdentityRoles(List<UUID> identityRoles) {
		this.identityRoles = identityRoles;
	}
}
