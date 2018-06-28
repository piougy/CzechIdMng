package eu.bcvsolutions.idm.core.api.dto.filter;

import java.util.UUID;

/**
 * Filter for contract's slice guarantees
 * 
 * @author svandav
 *
 */
public class IdmContractSliceGuaranteeFilter implements BaseFilter {

	private UUID contractSliceId;
	private UUID guaranteeId;

	public UUID getContractSliceId() {
		return contractSliceId;
	}

	public void setContractSliceId(UUID contractSliceId) {
		this.contractSliceId = contractSliceId;
	}

	public UUID getGuaranteeId() {
		return guaranteeId;
	}

	public void setGuaranteeId(UUID guaranteeId) {
		this.guaranteeId = guaranteeId;
	}
}
