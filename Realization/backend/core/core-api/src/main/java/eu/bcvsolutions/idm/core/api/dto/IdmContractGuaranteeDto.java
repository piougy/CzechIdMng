package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Identity contract's guarantee
 * 
 * @author Radek Tomiška
 *
 */
@Relation(collectionRelation = "contractGuarantees")
public class IdmContractGuaranteeDto extends AbstractDto implements ExternalIdentifiable {

	private static final long serialVersionUID = 1127070160913630052L;

	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
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
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
}
