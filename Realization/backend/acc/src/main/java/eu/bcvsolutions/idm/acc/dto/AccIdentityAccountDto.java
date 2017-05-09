package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;

/*
 * Identity account DTO
 * 
 */
@Relation(collectionRelation = "identityAccounts")
public class AccIdentityAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;
	@Embedded(dtoClass = IdmIdentityRoleDto.class)
	private UUID identityRole; // identity account is based on identity role assign and system mapping
	@Embedded(dtoClass = SysRoleSystemDto.class)
	private UUID roleSystem;
	private boolean ownership = true;

	@Override
	public UUID getAccount() {
		return account;
	}

	@Override
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

	@Override
	public boolean isOwnership() {
		return ownership;
	}

	@Override
	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	}

	@Override
	public UUID getEntity() {
		return this.identity;
	}

	@Override
	public void setEntity(UUID entity) {
		this.identity = entity;
	}
}
