package eu.bcvsolutions.idm.rpt.dto;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;

/**
 * DTO that contains data each duplicity. The DTO is used in
 * {@link RptIdentityRoleByRoleDeduplicationDto}
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 *
 */
public class RptIdentityRoleByRoleDeduplicationDuplicityDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	private LocalDate validFrom;
	@NotNull
	private LocalDate validTill;
	@NotNull
	private IdmRoleDto role;

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

	public IdmRoleDto getRole() {
		return role;
	}

	public void setRole(IdmRoleDto role) {
		this.role = role;
	}

}
