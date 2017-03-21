package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.domain.Disableable;
import eu.bcvsolutions.idm.core.api.domain.Embedded;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdentityDto;

/**
 * Identity contract - working position
 * 
 * @author Svanda
 */

public class IdmIdentityContractDto extends AbstractDto implements Disableable {

	private static final long serialVersionUID = 1L;

	@Embedded(dtoClass = IdentityDto.class)
	private UUID identity;
	private LocalDate validFrom;
	private LocalDate validTill;
	@Embedded(dtoClass = IdentityDto.class)
	private UUID guarantee;
	private String position;
	private boolean externe;
	private boolean disabled;

	// private IdmTreeNode workPosition;
	
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

	public UUID getGuarantee() {
		return guarantee;
	}

	public void setGuarantee(UUID guarantee) {
		this.guarantee = guarantee;
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
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public boolean isDisabled() {
		return disabled;
	}
}
