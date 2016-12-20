package eu.bcvsolutions.idm.core.model.dto;

import java.util.UUID;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;


public class IdmIdentityRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	private UUID identity;
	private UUID role;
	private LocalDate validFrom;
	private LocalDate validTill;

	public IdmIdentityRoleDto() {
	}

	public IdmIdentityRoleDto(UUID id) {
		super(id);
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

	public void setValidTill(LocalDate validTo) {
		this.validTill = validTo;
	}

	public UUID getIdentity() {
		return identity;
	}

	public void setIdentity(UUID identity) {
		this.identity = identity;
	}

	public UUID getRole() {
		return role;
	}

	public void setRole(UUID role) {
		this.role = role;
	}
	
}