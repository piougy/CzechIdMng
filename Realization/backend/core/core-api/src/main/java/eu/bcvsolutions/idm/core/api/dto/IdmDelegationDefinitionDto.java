package eu.bcvsolutions.idm.core.api.dto;

import eu.bcvsolutions.idm.core.api.domain.Embedded;
import java.util.UUID;
import org.springframework.hateoas.core.Relation;
import java.time.LocalDate;

/**
 * Definition of a delegation DTO.
 *
 * @author Vít Švanda
 *
 */
@Relation(collectionRelation = "delegationDefinitions")
public class IdmDelegationDefinitionDto extends AbstractDto {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID delegator;
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID delegate;
	@Embedded(dtoClass = IdmIdentityContractDto.class)
	private UUID delegatorContract;
	private String type;
	private LocalDate validFrom;
	private LocalDate validTill;
	private String description;

	public IdmDelegationDefinitionDto() {
		super();
	}

	public UUID getDelegator() {
		return delegator;
	}

	public void setDelegator(UUID delegator) {
		this.delegator = delegator;
	}

	public UUID getDelegate() {
		return delegate;
	}

	public void setDelegate(UUID delegate) {
		this.delegate = delegate;
	}

	public UUID getDelegatorContract() {
		return delegatorContract;
	}

	public void setDelegatorContract(UUID delegatorContract) {
		this.delegatorContract = delegatorContract;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public LocalDate getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(LocalDate validFrom) {
		this.validFrom = validFrom;
	}

	public LocalDate getValidTill() {
		return validTill;
	}

	public void setValidTill(LocalDate validTill) {
		this.validTill = validTill;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
