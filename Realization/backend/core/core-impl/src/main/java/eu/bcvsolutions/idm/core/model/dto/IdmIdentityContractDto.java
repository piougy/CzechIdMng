package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import javax.validation.constraints.Size;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.DefaultFieldLengths;
import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.entity.ValidableEntity;

/**
 * Identity contract - working position
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "identityContracts")
public class IdmIdentityContractDto extends AbstractDto implements ValidableEntity, Disableable {
	
	private static final long serialVersionUID = 8606180830493472930L;
	
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;
	private LocalDate validFrom;
	private LocalDate validTill;
	private String position;
	private boolean main = true;
	private boolean externe;
	private boolean disabled;
	@Embedded(dtoClass = IdmTreeNodeDto.class)
	private UUID workPosition;
	@Size(max = DefaultFieldLengths.DESCRIPTION)
	private String description;
	
	public IdmIdentityContractDto() {
	}
	
	public IdmIdentityContractDto(Auditable auditable) {
		super(auditable);
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

	/**
	 * Externe working position
	 * 
	 * @return
	 */
	public boolean isExterne() {
		return externe;
	}

	public void setExterne(boolean externe) {
		this.externe = externe;
	}
	
	@Override
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setWorkPosition(UUID workPosition) {
		this.workPosition = workPosition;
	}
	
	public UUID getWorkPosition() {
		return workPosition;
	}

	/**
	 * main ~= default identity contract
	 * 
	 * @return
	 */
	public boolean isMain() {
		return main;
	}

	public void setMain(boolean main) {
		this.main = main;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
