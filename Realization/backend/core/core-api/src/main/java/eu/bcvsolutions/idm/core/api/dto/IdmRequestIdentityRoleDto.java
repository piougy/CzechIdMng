package eu.bcvsolutions.idm.core.api.dto;

import java.util.Set;
import java.util.UUID;

import org.springframework.hateoas.core.Relation;

/**
 * DTO for show changes on assigned identity roles
 *
 * @author Vít Švanda
 */
@Relation(collectionRelation = "requestIdentityRoles")
public class IdmRequestIdentityRoleDto extends IdmConceptRoleRequestDto {

	private static final long serialVersionUID = 1L;

	private UUID directRole; // direct identity role
	private UUID roleComposition; // direct role
	/**
	 * Roles for create adding concepts (use only for assign new roles). You can
	 * still use field "role" for assign one role.
	 */
	private Set<UUID> roles;
	private Set<IdmIdentityDto> candidates;

	public Set<UUID> getRoles() {
		return roles;
	}

	public void setRoles(Set<UUID> roles) {
		this.roles = roles;
	}

	public UUID getDirectRole() {
		return directRole;
	}

	public void setDirectRole(UUID directRole) {
		this.directRole = directRole;
	}

	public UUID getRoleComposition() {
		return roleComposition;
	}

	public void setRoleComposition(UUID roleComposition) {
		this.roleComposition = roleComposition;
	}

	public Set<IdmIdentityDto> getCandidates() {
		return candidates;
	}

	public void setCandidates(Set<IdmIdentityDto> candidates) {
		this.candidates = candidates;
	}
}