package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Identity contract's guarantee
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "contractGuarantees")
public class IdmContractGuaranteeDto extends AbstractDto {

	private static final long serialVersionUID = 1127070160913630052L;

	@Embedded(dtoClass = IdmIdentityContractDto.class)
	private UUID identityContract;
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID guarantee;
	
	public IdmContractGuaranteeDto() {
	}
	
	public IdmContractGuaranteeDto(UUID identityContract, UUID guarantee) {
		this.identityContract = identityContract;
		this.guarantee = guarantee;
	}

	public UUID getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(UUID identityContract) {
		this.identityContract = identityContract;
	}

	public UUID getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(UUID guarantee) {
		this.guarantee = guarantee;
	}
}
