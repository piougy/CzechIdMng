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
	
    private Set<UUID> roles;

	public Set<UUID> getRoles() {
		return roles;
	}

	public void setRoles(Set<UUID> roles) {
		this.roles = roles;
	}

 }