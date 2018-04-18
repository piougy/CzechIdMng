package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Comparator;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Manager for automatic role
 * 
 * @author svandav
 *
 */
@Service("contractSliceManager")
public class DefaultContractSliceManager implements ContractSliceManager {

	@Autowired
	private IdmIdentityContractService contractService;

	@Override
	public IdmIdentityContractDto createContractBySlice(IdmContractSliceDto currentSlice,
			List<IdmContractSliceDto> slices) {
		Assert.notNull(currentSlice, "Contract slice cannot be null!");
		Assert.notNull(currentSlice.getIdentity());
		Assert.notNull(slices);

		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		// Contract reuses audit fields from slice
		EntityUtils.copyAuditFields(currentSlice, contract);

		// Get valid interval of whole contract
		recalculateContractValidity(contract, slices);

		convertSliceToContract(currentSlice, contract);
		// Create contract
		return contractService.save(contract);
	}

	@Override
	public IdmIdentityContractDto updateContractBySlice(IdmIdentityContractDto contract, IdmContractSliceDto slice,
			List<IdmContractSliceDto> slices) {
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slice.getIdentity());
		Assert.notNull(slice.getId(), "Contract slice have to be created!");
		Assert.notNull(slices);

		// Get valid interval of whole contract (update on change of any contract's slice)
		recalculateContractValidity(contract, slices);
		
		// Slice is sets as 'is using as contract', we will update all attributes
		if (slice.isUsingAsContract()) {
			convertSliceToContract(slice, contract);
		}
		// Create contract
		return contractService.save(contract);
	}

	/**
	 * Recalculate time validity for whole contract (from all given slices)
	 * 
	 * @param contract
	 * @param slices
	 */
	@Override
	public void recalculateContractValidity(IdmIdentityContractDto contract, List<IdmContractSliceDto> slices) {
		Comparator<IdmContractSliceDto> comparatorValidFrom = Comparator.comparing(IdmContractSliceDto::getValidFrom);

		IdmContractSliceDto minValidFromSlice = slices.stream().filter(s -> s.getValidFrom() != null)
				.min(comparatorValidFrom).orElse(null);
		IdmContractSliceDto maxValidFromSlice = slices.stream().filter(s -> s.getValidFrom() != null)
				.max(comparatorValidFrom).orElse(null);

		// Contract is valid from minimum of all 'validFrom' slices
		LocalDate validFrom = minValidFromSlice != null ? minValidFromSlice.getValidFrom() : null;
		// Contract is valid till date getting from the slice (validTill) with max of
		// 'validFrom' (last slice)
		LocalDate validTill = maxValidFromSlice != null ? maxValidFromSlice.getValidTill() : null;
		contract.setValidFrom(validFrom);
		contract.setValidTill(validTill);
	}

	/**
	 * Convert slice to the contract (does not save changes)
	 * 
	 * @param slice
	 * @param contract
	 * @param validFrom
	 *            of whole contract
	 * @param validTill
	 *            of whole contract
	 */
	private void convertSliceToContract(IdmContractSliceDto slice, IdmIdentityContractDto contract) {
		contract.setIdentity(slice.getIdentity());
		contract.setMain(slice.isMain());
		contract.setPosition(slice.getPosition());
		contract.setWorkPosition(slice.getWorkPosition());
		contract.setRealmId(slice.getRealmId());
		contract.setState(slice.getState());
		contract.setTrimmed(slice.isTrimmed());
		contract.setExterne(slice.isExterne());
		contract.setDescription(slice.getDescription());
	}

}
