package eu.bcvsolutions.idm.rpt.dto;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;

/**
 * DTO that contains data for report duplicity on assigned roles
 *
 * @author Ondrej Kopr
 * @since 9.5.0
 *
 */
public class RptIdentityRoleByRoleDeduplicationDto implements Serializable {

	private static final long serialVersionUID = 1L;

	@NotNull
	private IdmIdentityDto identity;
	@NotNull
	private IdmIdentityContractDto identityContract;
	@NotNull
	private IdmTreeNodeDto workPosition;
	@NotNull
	private List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicity;

	public IdmIdentityContractDto getIdentityContract() {
		return identityContract;
	}

	public void setIdentityContract(IdmIdentityContractDto identityContract) {
		this.identityContract = identityContract;
	}

	public List<RptIdentityRoleByRoleDeduplicationDuplicityDto> getDuplicity() {
		return duplicity;
	}

	public void setDuplicity(List<RptIdentityRoleByRoleDeduplicationDuplicityDto> duplicity) {
		this.duplicity = duplicity;
	}

	public IdmTreeNodeDto getWorkPosition() {
		return workPosition;
	}

	public void setWorkPosition(IdmTreeNodeDto workPosition) {
		this.workPosition = workPosition;
	}

	public IdmIdentityDto getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentityDto identity) {
		this.identity = identity;
	}

}
