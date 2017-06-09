package eu.bcvsolutions.idm.acc.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;

/*
 * Role catalogue relation on account (DTO)
 * 
 */
@Relation(collectionRelation = "roleCatologueAccounts")
public class AccRoleCatalogueAccountDto extends AbstractDto implements EntityAccountDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = AccAccountDto.class)
	private UUID account;
	@Embedded(dtoClass = IdmRoleCatalogueDto.class)
	private UUID roleCatalogue;
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

	public UUID getRoleCatalogue() {
		return roleCatalogue;
	}

	public void setRoleCatalogue(UUID roleCatalogue) {
		this.roleCatalogue = roleCatalogue;
	}

	@Override
	public UUID getEntity() {
		return this.roleCatalogue;
	}

	@Override
	public void setEntity(UUID entity) {
		this.roleCatalogue = entity;
	}

}
