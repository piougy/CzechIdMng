package eu.bcvsolutions.idm.core.model.dto;

import java.util.Date;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;


public class IdmIdentityRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	private Long identity;
	private Long role;
	private Date validFrom;
	private Date validTill;

	public IdmIdentityRoleDto() {
	}

	public IdmIdentityRoleDto(Long id) {
		super(id);
	}

	public Date getValidFrom() {
		return validFrom;
	}

	public void setValidFrom(Date validFrom) {
		this.validFrom = validFrom;
	}

	public Date getValidTill() {
		return validTill;
	}

	public void setValidTill(Date validTo) {
		this.validTill = validTo;
	}

	public Long getIdentity() {
		return identity;
	}

	public void setIdentity(Long identity) {
		this.identity = identity;
	}

	public Long getRole() {
		return role;
	}

	public void setRole(Long role) {
		this.role = role;
	}
	
}