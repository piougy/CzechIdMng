package eu.bcvsolutions.idm.core.model.dto;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * IdentityContract with additional infos:
 * - contains prime identity's working position
 * - contains all work position's parents in tree structure and contract's working position as path 
 * 
 * TODO: use tree node dto
 * 
 * @author Radek Tomiška
 *
 */
public class WorkPositionDto extends AbstractDto {

	private static final long serialVersionUID = 6839506093315671159L;
	private IdmIdentityDto identity;
	private IdmIdentityContractDto contract;
	private List<IdmTreeNode> path;
	
	public WorkPositionDto() {
	}
	
	public WorkPositionDto(IdmIdentityDto identity, IdmIdentityContractDto contract) {
		super(contract);
		this.identity = identity;
		this.contract = contract;
	}

	public IdmIdentityDto getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentityDto identity) {
		this.identity = identity;
	}

	public IdmIdentityContractDto getContract() {
		return contract;
	}

	public void setContract(IdmIdentityContractDto contract) {
		this.contract = contract;
	}

	public List<IdmTreeNode> getPath() {
		return path;
	}

	public void setPath(List<IdmTreeNode> path) {
		this.path = path;
	}
}
