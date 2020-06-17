package eu.bcvsolutions.idm.core.api.dto;

import javax.validation.constraints.NotEmpty;

import org.springframework.hateoas.core.Relation;


/**
 * Delegation type DTO.
 * 
 * @author Vít Švanda
 * @since 10.4.0
 */
@Relation(collectionRelation = "delegationTypes")
public class DelegationTypeDto extends AbstractComponentDto {

	private static final long serialVersionUID = 1L;
	@NotEmpty
	private String ownerType;
	private boolean supportsDelegatorContract;

	public String getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(String ownerType) {
		this.ownerType = ownerType;
	}

	public boolean isSupportsDelegatorContract() {
		return supportsDelegatorContract;
	}

	public void setSupportsDelegatorContract(boolean supportsDelegatorContract) {
		this.supportsDelegatorContract = supportsDelegatorContract;
	}
}
