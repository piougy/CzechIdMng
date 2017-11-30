package eu.bcvsolutions.idm.rpt.dto;

import org.joda.time.LocalDate;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;

/**
 * Identity - role, used in reports
 * 
 * @author Radek Tomiška
 *
 */
public class RptIdentityRoleDto extends AbstractDto {

	private static final long serialVersionUID = 1L;
	//
	private IdmIdentityDto identity;
	private IdmRoleDto role;
    private LocalDate validFrom;
    private LocalDate validTill;
    
    public RptIdentityRoleDto() {
	}
    
    public RptIdentityRoleDto(IdmIdentityDto identity) {
    	this.identity = identity;
    }
    
    public RptIdentityRoleDto(IdmRoleDto role) {
    	this.role = role;
    }
    
    /**
     * 
     * @param identity
     * @param identityRole required
     */
    public RptIdentityRoleDto(IdmIdentityDto identity, IdmIdentityRoleDto identityRole) {
    	super(identityRole);
    	//
    	this.identity = identity;
    	this.role = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.role.getName(), IdmRoleDto.class);
    	this.validFrom = identityRole.getValidFrom();
    	this.validTill = identityRole.getValidTill();
	}
    
    public IdmIdentityDto getIdentity() {
		return identity;
	}
    
    public void setIdentity(IdmIdentityDto identity) {
		this.identity = identity;
	}

	public IdmRoleDto getRole() {
		return role;
	}

	public void setRole(IdmRoleDto role) {
		this.role = role;
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
}
