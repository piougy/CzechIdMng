package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;

/**
 * Identity-role-account DTO
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "identityRoleAccounts")
public class AccIdentityRoleAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass = IdmIdentityRoleDto.class)
	private UUID identityRole;
	private boolean ownership = true;

	public UUID getAccount() {
		return account;
	}

	public void setAccount(UUID account) {
		this.account = account;
	}

	public boolean isOwnership() {
		return ownership;
	}

	public void setOwnership(boolean ownership) {
		this.ownership = ownership;
	}

	public UUID getIdentityRole() {
		return identityRole;
	}

	public void setIdentityRole(UUID contract) {
		this.identityRole = contract;
	}

	@Override
	public UUID getEntity() {
		return this.identityRole;
	}

	@Override
	public void setEntity(UUID entity) {
		this.identityRole = entity;
	}

}
