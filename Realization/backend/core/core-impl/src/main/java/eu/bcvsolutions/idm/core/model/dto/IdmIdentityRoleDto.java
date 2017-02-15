package eu.bcvsolutions.idm.core.model.dto;

import java.io.Serializable;
import java.util.UUID;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * IdentityRole from WF
 * 
 * @author svanda
 * @author Radek Tomiška
 *
 */
public class IdmIdentityRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	private UUID identityContractId;
	private UUID roleId;
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

	public UUID getRoleId() {
		return roleId;
	}

	public void setRoleId(UUID roleId) {
		this.roleId = roleId;
	}
	
	public void setIdentityContractId(Serializable identityContractId) {
		this.identityContractId = EntityUtils.toUuid(identityContractId);
	}
	
	public UUID getIdentityContractId() {
		return identityContractId;
	}
}