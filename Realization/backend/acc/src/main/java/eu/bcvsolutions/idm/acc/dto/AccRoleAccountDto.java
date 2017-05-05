package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * Role account DTO
 * @author svandav
 *
 */
@Relation(collectionRelation = "roleAccounts")
public class AccRoleAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;
	
	@Embedded(dtoClass=AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass=IdmRoleDto.class)
	private UUID role;
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
	
	public UUID getRole() {
		return role;
	}
	public void setRole(UUID role) {
		this.role = role;
	}
	@Override
	public UUID getEntity() {
		return this.role;
	}
	@Override
	public void setEntity(UUID entity) {
		this.role = entity;
	} 
	
}
