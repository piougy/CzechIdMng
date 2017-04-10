package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdentityDto;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityRoleDto;

/*
 * Identity account DTO
 * 
 */

public class AccIdentityAccountDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	
	@Embedded(dtoClass=AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass=IdentityDto.class)
	private UUID identity;
	@Embedded(dtoClass=IdmIdentityRoleDto.class)
	private UUID identityRole; // identity account is based on identity role asing and  system mapping
	@Embedded(dtoClass=SysRoleSystemDto.class)
	private UUID roleSystem;
	private boolean ownership = true;
	
	public UUID getAccount() {
		return account;
	}
	public void setAccount(UUID account) {
		this.account = account;
	}
	public UUID getIdentity() {
		return identity;
	}
	public void setIdentity(UUID identity) {
		this.identity = identity;
	}
	public UUID getIdentityRole() {
		return identityRole;
	}
	public void setIdentityRole(UUID identityRole) {
		this.identityRole = identityRole;
	}
	public UUID getRoleSystem() {
		return roleSystem;
	}
	public void setRoleSystem(UUID roleSystem) {
		this.roleSystem = roleSystem;
	}
	public boolean isOwnership() {
		return ownership;
	}
	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	} 
}
