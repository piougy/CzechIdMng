package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import org.joda.time.LocalDate;
import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;

/**
 * Identity contract - working position
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@Relation(collectionRelation = "identityContracts")
public class IdmIdentityContractDto extends AbstractDto implements Disableable {

	private static final long serialVersionUID = 8606180830493472930L;
	
	@Embedded(dtoClass = IdmIdentityDto.class)
	private UUID identity;
	private LocalDate validFrom;
	private LocalDate validTill;
	private String position;
	private boolean externe;
	private boolean disabled;
	@Embedded(dtoClass = IdmTreeNodeDto.class)
	private UUID workPosition;
	
	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
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
}
