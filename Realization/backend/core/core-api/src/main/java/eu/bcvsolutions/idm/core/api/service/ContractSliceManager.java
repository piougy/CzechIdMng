package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;

/**
 * Manager for contract time slices
 * 
 * @author svandav
 *
 */
public interface ContractSliceManager {

	IdmIdentityContractDto createContractBySlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices);

	IdmIdentityContractDto updateContractBySlice(IdmIdentityContractDto contract, IdmContractSliceDto slice,
			List<IdmContractSliceDto> slices);

	/**
	 * Recalculate time validity for whole contract (from all given slices)
	 * 
	 * @param contract
	 * @param slices
	 */
	void recalculateContractValidity(IdmIdentityContractDto contract, List<IdmContractSliceDto> slices);

	
	
}
