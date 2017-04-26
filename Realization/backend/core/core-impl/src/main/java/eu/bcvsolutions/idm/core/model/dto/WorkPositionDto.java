package eu.bcvsolutions.idm.core.model.dto;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;

/**
 * IdentityContract with additional infos:
 * - contains prime identity's working position
 * - contains all work position's parents in tree structure and contract's working position as path 
 * 
 * TODO: use dtos
 * 
 * @author Radek Tomiška
 *
 */
public class WorkPositionDto extends AbstractDto {

	private static final long serialVersionUID = 6839506093315671159L;
	private IdmIdentity identity;
	private IdmIdentityContract contract;
	private List<IdmTreeNode> path;
	
	public WorkPositionDto() {
	}
	
	public WorkPositionDto(IdmIdentity identity, IdmIdentityContract contract) {
		super(contract);
		this.identity = identity;
		this.contract = contract;
	}

	public IdmIdentity getIdentity() {
		return identity;
	}

	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}

	public IdmIdentityContract getContract() {
		return contract;
	}

	public void setContract(IdmIdentityContract contract) {
		this.contract = contract;
	}

	public List<IdmTreeNode> getPath() {
		return path;
	}

	public void setPath(List<IdmTreeNode> path) {
		this.path = path;
	}
}
