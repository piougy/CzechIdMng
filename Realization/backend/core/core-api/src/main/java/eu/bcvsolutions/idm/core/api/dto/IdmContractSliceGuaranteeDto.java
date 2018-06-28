package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Embedded;

/**
 * Identity contract's slice guarantee DTO
 * 
 * @author svandav
 *
 */
@Relation(collectionRelation = "contractSliceGuarantees")
public class IdmContractSliceGuaranteeDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmContractSliceDto.class)
	private UUID contractSlice;
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID guarantee;

	public IdmContractSliceGuaranteeDto() {
	}

	public IdmContractSliceGuaranteeDto(UUID contractSlice, UUID guarantee) {
		this.contractSlice = contractSlice;
		this.guarantee = guarantee;
	}

	public UUID getContractSlice() {
		return contractSlice;
	}

	public void setContractSlice(UUID contractSlice) {
		this.contractSlice = contractSlice;
	}

	public UUID getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(UUID guarantee) {
		this.guarantee = guarantee;
	}
}
