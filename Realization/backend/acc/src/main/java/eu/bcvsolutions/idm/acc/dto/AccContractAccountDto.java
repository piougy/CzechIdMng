package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;

/**
 * Contract-account DTO
 * @author svandav
 *
 */
@Relation(collectionRelation = "contractAccounts")
public class AccContractAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;
	
	@Embedded(dtoClass=AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass=IdmIdentityContractDto.class)
	private UUID contract;
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
	

	public UUID getContract() {
		return contract;
	}
	public void setContract(UUID contract) {
		this.contract = contract;
	}
	@Override
	public UUID getEntity() {
		return this.contract;
	}
	
	@Override
	public void setEntity(UUID entity) {
		this.contract = entity;
	} 
	
}
