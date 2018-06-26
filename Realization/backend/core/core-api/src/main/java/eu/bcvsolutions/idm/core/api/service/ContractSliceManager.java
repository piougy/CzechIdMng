package eu.bcvsolutions.idm.core.api.service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceGuaranteeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.script.ScriptEnabled;

/**
 * Manager for contract time slices
 * 
 * @author svandav
 *
 */
public interface ContractSliceManager extends ScriptEnabled  {

	/**
	 * Create or update contract by given slice
	 * 
	 * @param contract
	 * @param slice
	 * @return
	 */
	IdmIdentityContractDto updateContractBySlice(IdmIdentityContractDto contract, IdmContractSliceDto slice, Map<String, Serializable> eventProperties);

	/**
	 * Update validity till on previous slice. Previous slice will be valid till
	 * starts of validity next slice.
	 * 
	 * @param slice
	 * @param slices
	 */
	void updateValidTillOnPreviousSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices);

	/**
	 * Find unvalid contract slices
	 * 
	 * @return
	 */
	Page<IdmContractSliceDto> findUnvalidSlices(Pageable page);

	/**
	 * Sets this slice as it is currently using. It means, this slice will be marked
	 * "As currently using" and others slices will be unmarked. Slices will be
	 * copied to the parent contract.
	 * 
	 * @param slice
	 * @return 
	 */
	IdmContractSliceDto setSliceAsCurrentlyUsing(IdmContractSliceDto slice);

	/**
	 * Find slice that is currently valid (or first in future) for given contract. First find valid slice for now. If none exist then
	 * find the nearest slice valid in future. If none slice will be found, then
	 * return null.
	 * 
	 * @param contract
	 * @return
	 */
	IdmContractSliceDto findValidSlice(UUID contractId);

	/**
	 * Find next slice for given slice
	 * 
	 * @param slice
	 * @param slices
	 * @return
	 */
	IdmContractSliceDto findNextSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices);

	/**
	 * Find previous slice for given slice
	 * 
	 * @param slice
	 * @param slices
	 * @return
	 */
	IdmContractSliceDto findPreviousSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices);

	/**
	 * Find slice for given parent contract ID
	 * 
	 * @param parentContract
	 * @return
	 */
	List<IdmContractSliceDto> findAllSlices(UUID parentContract);

	/**
	 * Copy guarantees from slice to contract. Modifies only diff of current and
	 * result sets.
	 * 
	 * @param slice
	 * @param contract
	 */
	void copyGuarantees(IdmContractSliceDto slice, IdmIdentityContractDto contract);

	/**
	 * Find all guarantees for given slice
	 * 
	 * @param sliceId
	 * @return
	 */
	List<IdmContractSliceGuaranteeDto> findSliceGuarantees(UUID sliceId);

}
