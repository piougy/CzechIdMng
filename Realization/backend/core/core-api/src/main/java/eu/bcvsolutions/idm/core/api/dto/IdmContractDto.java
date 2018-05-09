package eu.bcvsolutions.idm.core.api.dto;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Contract for the identity. Represents whole contract. Validity from / till
 * represents validity for whole contract (across all time slices). Keeps DTO of
 * the current time slice. It is not intended for the update of the contract,
 * but only for read current state.
 *
 * @author Svanda
 */
@Relation(collectionRelation = "contracts")
public class IdmContractDto implements Serializable, ValidableEntity {

	private static final long serialVersionUID = 1L;

	private LocalDate validFrom;
	private LocalDate validTill;
	private IdmIdentityContractDto currentTimeSlice;

	public IdmContractDto(IdmIdentityContractDto currentTimeSlice, LocalDate validFrom, LocalDate validTill) {
		super();
		Assert.notNull(currentTimeSlice, "Current time slice must be filled!");
		this.currentTimeSlice = currentTimeSlice;
		this.validFrom = validFrom;
		this.validTill = validTill;
	}

	public UUID getIdentity() {
		return getCurrentTimeSlice().getIdentity();
	}

	public boolean isDisabled() {
		return getCurrentTimeSlice().isDisabled();
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

	public IdmIdentityContractDto getCurrentTimeSlice() {
		return currentTimeSlice;
	}

	public void setCurrentTimeSlice(IdmIdentityContractDto currentTimeSlice) {
		this.currentTimeSlice = currentTimeSlice;
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
