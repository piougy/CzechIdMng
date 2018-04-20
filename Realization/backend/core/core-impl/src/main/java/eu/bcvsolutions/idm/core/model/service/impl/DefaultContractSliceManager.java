package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;

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
	@Autowired
	private IdmContractSliceService contractSliceService;
	@Autowired
	private EntityManager entityManager;

	@Override
	@Transactional
	public IdmIdentityContractDto createContractBySlice(IdmContractSliceDto slice,
			List<IdmContractSliceDto> slices) {
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slice.getIdentity());
		Assert.notNull(slices);

		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		// Contract reuses audit fields from slice
		EntityUtils.copyAuditFields(slice, contract);

		// Get valid interval of whole contract
		recalculateContractValidity(contract, slices);
		// Previous slice will be valid till starts of validity next slice
		updateValidTillOnPreviousSlice(slice, slices);

		convertSliceToContract(slice, contract);
		// Create contract
		return contractService.save(contract);
	}

	@Override
	@Transactional
	public IdmIdentityContractDto updateContractBySlice(IdmIdentityContractDto contract, IdmContractSliceDto slice,
			List<IdmContractSliceDto> slices) {
		
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slice.getIdentity());
		Assert.notNull(slice.getId(), "Contract slice have to be created!");
		Assert.notNull(slices);

		// Get valid interval of whole contract (update on change of any contract's
		// slice)
		recalculateContractValidity(contract, slices);

		// Slice is sets as 'is using as contract', we will update all attributes
		if (slice.isUsingAsContract()) {
			convertSliceToContract(slice, contract);
		}
		// Previous slice will be valid till starts of validity next slice
		updateValidTillOnPreviousSlice(slice, slices);
		// Create contract
		return contractService.save(contract);
	}

	/**
	 * Update validity till on previous slice. Previous slice will be valid till
	 * starts of validity next slice.
	 * 
	 * @param slice
	 * @param slices
	 */
	@Override
	@Transactional
	public void updateValidTillOnPreviousSlice(IdmContractSliceDto slice, List<IdmContractSliceDto> slices) {
		Assert.notNull(slice, "Contract slice cannot be null!");
		Assert.notNull(slices);
		if (slice.getValidFrom() == null) {
			return;
		}
 
		Comparator<IdmContractSliceDto> comparatorValidFrom = Comparator.comparing(IdmContractSliceDto::getValidFrom);
		IdmContractSliceDto previousSlice = slices.stream() //
				.filter(s -> !s.equals(slice) && s.getValidFrom() != null
						&& s.getValidFrom().isBefore(slice.getValidFrom())) //
				.max(comparatorValidFrom) //
				.orElse(null); //
		if (previousSlice == null) {
			return;
		}
		// Previous slice will be valid till starts of validity next slice
		previousSlice.setValidTill(slice.getValidFrom().minusDays(1));
		contractSliceService.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, previousSlice,
				ImmutableMap.of(IdmContractSliceService.SKIP_CREATE_OR_UPDATE_PARENT_CONTRACT, true)));
	}
	

	/**
	 * Recalculate time validity for whole contract (from all given slices)
	 * 
	 * @param contract
	 * @param slices
	 */
	@Override
	@Transactional
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

	@Override
	public Page<IdmContractSliceDto> findUnvalidSlices(Pageable page) {
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setShouldBeUsingAsContract(Boolean.TRUE);
		sliceFilter.setUsingAsContract(Boolean.FALSE);
		
		return contractSliceService.find(sliceFilter, page);
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
