package eu.bcvsolutions.idm.rpt.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIncompatibleRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Identity - assigned incompatible role
 * Incompatible role definition is cloned into report dto -> prevent to role is deleted etc. 
 * 
 * @author Radek Tomiška
 * @since 9.4.0
 */
public class RptIdentityIncompatibleRoleDto implements Serializable {

	private static final long serialVersionUID = 1L;
	//
	@NotNull
	private IdmIdentityDto identity;
	@NotNull
    private IdmRoleDto directRole;
	@NotNull
    private IdmIncompatibleRoleDto incompatibleRole;
	@NotNull
    private IdmRoleDto superior;
	@NotNull
    private IdmRoleDto sub;

	public IdmIdentityDto getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentityDto identity) {
		this.identity = identity;
	}

	public IdmRoleDto getDirectRole() {
		return directRole;
	}

	public void setDirectRole(IdmRoleDto directRole) {
		this.directRole = directRole;
	}

	public IdmIncompatibleRoleDto getIncompatibleRole() {
		return incompatibleRole;
	}

	public void setIncompatibleRole(IdmIncompatibleRoleDto incompatibleRole) {
		this.incompatibleRole = incompatibleRole;
	}

	public IdmRoleDto getSuperior() {
		return superior;
	}

	public void setSuperior(IdmRoleDto superior) {
		this.superior = superior;
	}

	public IdmRoleDto getSub() {
		return sub;
	}

	public void setSub(IdmRoleDto sub) {
		this.sub = sub;
	}
}
