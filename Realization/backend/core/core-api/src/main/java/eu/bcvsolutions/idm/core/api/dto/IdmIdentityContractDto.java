package eu.bcvsolutions.idm.core.api.dto;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Identity contract - working position
 *
 * @author Svanda
 */
@Relation(collectionRelation = "identityContracts")
public class IdmIdentityContractDto extends AbstractDto implements ValidableEntity {

    private static final long serialVersionUID = 1L;

    @Embedded(dtoClass = IdmIdentityDto.class)
    private UUID identity;
    private LocalDate validFrom;
    private LocalDate validTill;
    private String position;
    private boolean externe;
    private boolean disabled;
	private ContractState state;
    private boolean main;
    @Embedded(dtoClass = IdmTreeNodeDto.class)
    private UUID workPosition;
    private String description;
    
    public IdmIdentityContractDto() {
	}
    
    public IdmIdentityContractDto(UUID id) {
		super(id);
	}

    public UUID getIdentity() {
        return identity;
    }

    public void setIdentity(UUID identity) {
        this.identity = identity;
    }

    @Override
    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    @Override
    public LocalDate getValidTill() {
        return validTill;
    }

    public void setValidTill(LocalDate validTill) {
        this.validTill = validTill;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isExterne() {
        return externe;
    }

    public void setExterne(boolean externe) {
        this.externe = externe;
    }
    
    public boolean isDisabled() {
        return state == null ? disabled : state.isDisabled();
    }
    
    public boolean isExcluded() {
        return state == ContractState.EXCLUDED;
    }

    public void setMain(boolean main) {
        this.main = main;
    }

    public boolean isMain() {
        return main;
    }

    public void setWorkPosition(UUID workPosition) {
        this.workPosition = workPosition;
    }

    public UUID getWorkPosition() {
        return workPosition;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
    
    public void setState(ContractState state) {
		this.state = state;
	}
    
    public ContractState getState() {
		return state;
	}
    
    @Override
    public boolean isValid(LocalDate targetDate) {
    	return ValidableEntity.super.isValid(targetDate) && !isDisabled();
    }
    
    @Override
    public boolean isValidNowOrInFuture() {
    	return ValidableEntity.super.isValidNowOrInFuture() && !isDisabled();
    }
    
}
