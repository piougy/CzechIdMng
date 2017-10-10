package eu.bcvsolutions.idm.acc.dto;

import java.util.ArrayList;
import java.util.List;

import org.springframework.hateoas.core.Relation;

import eu.bcvsolutions.idm.core.api.dto.IdmContractGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;

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

	private List<IdmContractGuaranteeDto> guarantees;

	public List<IdmContractGuaranteeDto> getGuarantees() {
		if (this.guarantees == null) {
			this.guarantees = new ArrayList<>();
		}
		return this.guarantees;
	}

	public void setGuarantees(List<IdmContractGuaranteeDto> guarantees) {
		this.guarantees = guarantees;
	}

}
