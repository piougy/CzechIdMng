package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;

/**
 * Contract-slice-account DTO
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "contractSliceAccounts")
public class AccContractSliceAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass = IdmContractSliceDto.class)
	private UUID slice;
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

	public UUID getSlice() {
		return slice;
	}

	public void setSlice(UUID slice) {
		this.slice = slice;
	}

	@Override
	public UUID getEntity() {
		return this.slice;
	}

	@Override
	public void setEntity(UUID entity) {
		this.slice = entity;
	}

}
