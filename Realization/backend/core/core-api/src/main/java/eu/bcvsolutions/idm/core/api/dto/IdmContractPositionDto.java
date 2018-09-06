package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import io.swagger.annotations.ApiModelProperty;

/**
 * Identity contract's other positions
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
@Relation(collectionRelation = "contractPositions")
public class IdmContractPositionDto extends AbstractDto implements ExternalIdentifiable {

	private static final long serialVersionUID = 1127070160913630052L;

	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@Embedded(dtoClass = IdmIdentityContractDto.class)
	private UUID identityContract;
	@Embedded(dtoClass = IdmTreeNodeDto.class)
	private UUID workPosition;
	@Size(max = DefaultFieldLengths.NAME)
	private String position;
	
	public IdmContractPositionDto() {
	}
	
	public IdmContractPositionDto(UUID identityContract, UUID treeNodeId) {
		this.identityContract = identityContract;
		this.workPosition = treeNodeId;
	}

	public UUID getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(UUID identityContract) {
		this.identityContract = identityContract;
	}
	
	public UUID getWorkPosition() {
		return workPosition;
	}

	public void setWorkPosition(UUID workPosition) {
		this.workPosition = workPosition;
	}
	
	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	
	@Override
	public String getExternalId() {
		return externalId;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
}
