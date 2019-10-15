package eu.bcvsolutions.idm.core.api.dto;

import java.io.ObjectInputStream;
import java.io.ObjectInputStream.GetField;
import java.time.LocalDate;
import java.util.UUID;

import javax.validation.constraints.Size;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.domain.ExternalIdentifiable;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import io.swagger.annotations.ApiModelProperty;

/**
 * Identity contract - working position
 *
 * @author Svanda
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "identityContracts")
public class IdmIdentityContractDto extends FormableDto implements ValidableEntity, ExternalIdentifiable {

	private static final long serialVersionUID = 1L;

	@Size(max = DefaultFieldLengths.NAME)
	@ApiModelProperty(notes = "Unique external identifier.")
	private String externalId;
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;
	private LocalDate validFrom;
	private LocalDate validTill;
	@Size(max = DefaultFieldLengths.NAME)
	private String position;
	private boolean externe;
	private boolean disabled;
	private ContractState state;
	private boolean main;
	@Embedded(dtoClass = IdmTreeNodeDto.class)
	private UUID workPosition;
	private String description;
	private Boolean controlledBySlices; // Is true only if contract has some slice. Contract created by slice, cannot be
										// updated directly! Is sets only if DTO is not trimmed!

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

	public Boolean getControlledBySlices() {
		return controlledBySlices;
	}

	public void setControlledBySlices(Boolean controlledBySlices) {
		this.controlledBySlices = controlledBySlices;
	}

	@Override
	public boolean isValid(LocalDate targetDate) {
		return ValidableEntity.super.isValid(targetDate) && !isDisabled();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Contract validity and state is evaluated too.
	 */
	@Override
	public boolean isValidNowOrInFuture() {
		return ValidableEntity.super.isValidNowOrInFuture() && !isDisabled();
	}

	@Override
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	@Override
	public String getExternalId() {
		return externalId;
	}
	
	/**
	 * DTO are serialized in WF and embedded objects.
	 * We need to solve legacy issues with joda (old) vs. java time (new) usage.
	 * 
	 * @param ois
	 * @throws Exception
	 */
	private void readObject(ObjectInputStream ois) throws Exception {
		GetField readFields = ois.readFields();
		//
		externalId = (String) readFields.get("externalId", null);
		identity = (UUID) readFields.get("identity", null);
		validFrom = DtoUtils.toLocalDate(readFields.get("validFrom", null));
	    validTill = DtoUtils.toLocalDate(readFields.get("validTill", null));
		position = (String) readFields.get("position", null);
		externe = readFields.get("externe", false);
		disabled = readFields.get("disabled", false);
		state = (ContractState) readFields.get("state", null);
		main = readFields.get("main", false);
		workPosition = (UUID) readFields.get("workPosition", null);
		description = (String) readFields.get("description", null);
		controlledBySlices = (Boolean) readFields.get("controlledBySlices", null);
    }
}
