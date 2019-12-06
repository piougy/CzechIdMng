package eu.bcvsolutions.idm.acc.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;

/**
 * Contract for sync. For synchronization we need keep list of guarantees in the
 * contract.
 * 
 * @author Svanda
 *
 */

@Relation(collectionRelation = "contracts")
public class SyncIdentityContractDto extends IdmIdentityContractDto {

	private static final long serialVersionUID = 1L;

	private List<IdmIdentityDto> guarantees;
	private List<IdmTreeNodeDto> positions;

	public List<IdmIdentityDto> getGuarantees() {
		if (this.guarantees == null) {
			this.guarantees = new ArrayList<>();
		}
		return this.guarantees;
	}

	public void setGuarantees(List<IdmIdentityDto> guarantees) {
		this.guarantees = guarantees;
	}
	
	public List<IdmTreeNodeDto> getPositions() {
		if (this.positions == null) {
			this.positions = new ArrayList<>();
		}
		return positions;
	}
	
	public void setPositions(List<IdmTreeNodeDto> positions) {
		this.positions = positions;
	}

}
