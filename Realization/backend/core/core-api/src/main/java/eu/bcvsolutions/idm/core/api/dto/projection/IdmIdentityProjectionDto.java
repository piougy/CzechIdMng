package eu.bcvsolutions.idm.core.api.dto.projection;

import java.util.List;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import org.springframework.hateoas.core.Relation;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import io.swagger.annotations.ApiModel;

/**
 * Identity projection - "full" detail version with eavs, contract, assigned roles etc.
 * 
 * @author Radek Tomiška
 * @since 10.2.0
 */
@Relation(collectionRelation = "identities")
@ApiModel(description = "Identity domain object")
public class IdmIdentityProjectionDto extends AbstractDto {
	
	private static final long serialVersionUID = 1L;
	//
	@NotNull
	private IdmIdentityDto identity;
	private IdmIdentityContractDto contract; // prime or first contract, which can be read
	private List<IdmIdentityContractDto> otherContracts;
	private List<IdmContractPositionDto> otherPositions;
	private List<IdmIdentityRoleDto> identityRoles; // all assigned identity roles
	
	public IdmIdentityProjectionDto() {
	}
	
	public IdmIdentityProjectionDto(UUID id) {
		super(id);
	}
	
	public IdmIdentityProjectionDto(IdmIdentityDto identity) {
		// projection =~ identity
		super(identity);
		//
		this.identity = identity;
		this.setPermissions(identity.getPermissions());
	}
	
	public void setIdentity(IdmIdentityDto identity) {
		this.identity = identity;
		// projection =~ identity
		if (identity != null) {
			DtoUtils.copyAuditFields(identity, this);
			setId(identity.getId());
			setRealmId(identity.getRealmId());
		} else {
			DtoUtils.clearAuditFields(this);
			setId(null);
			setRealmId(null);
		}
	}
	
	public IdmIdentityDto getIdentity() {
		return identity;
	}
	
	public void setContract(IdmIdentityContractDto contract) {
		this.contract = contract;
	}
	
	public IdmIdentityContractDto getContract() {
		return contract;
	}

	public List<IdmIdentityContractDto> getOtherContracts() {
		if (otherContracts == null) {
			otherContracts = Lists.newArrayList();
		}
		return otherContracts;
	}

	public void setOtherContracts(List<IdmIdentityContractDto> otherContracts) {
		this.otherContracts = otherContracts;
	}

	public List<IdmContractPositionDto> getOtherPositions() {
		if (otherPositions == null) {
			otherPositions = Lists.newArrayList();
		}
		return otherPositions;
	}

	public void setOtherPositions(List<IdmContractPositionDto> otherPositions) {
		this.otherPositions = otherPositions;
	}

	public List<IdmIdentityRoleDto> getIdentityRoles() {
		if (identityRoles == null) {
			identityRoles = Lists.newArrayList();
		}
		return identityRoles;
	}

	public void setIdentityRoles(List<IdmIdentityRoleDto> identityRoles) {
		this.identityRoles = identityRoles;
	}
}
