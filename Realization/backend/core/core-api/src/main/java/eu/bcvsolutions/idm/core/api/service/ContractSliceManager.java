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

	IdmIdentityContractDto updateContractBySlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices);

	
	
}
