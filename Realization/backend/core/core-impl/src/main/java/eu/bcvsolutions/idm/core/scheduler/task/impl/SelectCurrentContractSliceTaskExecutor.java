package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Recalculate current using slices as contract. Find all slices which should be
 * for actual date using as contract and copy their values to parent contracts.
 *
 * @author svandav
 *
 */

@Service
@DisallowConcurrentExecution
@Description("Recalcullate current using slices as contract. Find all slices which should be for actual date using as contract and copy their values to parent contracts.")
public class SelectCurrentContractSliceTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {


	@Autowired
	private ContractSliceManager contractSliceManager;
	@Autowired
	private IdmContractSliceService contractSliceService;

	@Override
	@Transactional
	public Boolean process() {
		// Found all unvalid slices
		Page<IdmContractSliceDto> unvalidSlicesPage = contractSliceManager.findUnvalidSlices(null);
		boolean canContinue = true;
		//
		this.counter = 0L;
		this.count = Long.valueOf(unvalidSlicesPage.getTotalElements());
		
		for (IdmContractSliceDto slice : unvalidSlicesPage) {
			// Start recalculation

			// Only one slice can be marked as 'is using as contract' (for one parent
			// contract)
			if (slice.getParentContract() != null) {
				// Find other slices with this contract and marked "is using as contract"
				// (usually should be returned only one)
				IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
				sliceFilter.setParentContract(slice.getParentContract());
				sliceFilter.setUsingAsContract(Boolean.TRUE);
				List<IdmContractSliceDto> otherSlices = contractSliceService.find(sliceFilter, null).getContent();

				// To all this slices set "using as contract" on false
				otherSlices.forEach(s -> {
					s.setUsingAsContract(false);
					// We want only save data, not update contract by slice
					contractSliceService.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, s,
							ImmutableMap.of(IdmContractSliceService.SKIP_CREATE_OR_UPDATE_PARENT_CONTRACT, true)));
				});
			}
			slice.setUsingAsContract(true);
			// Copy to contract is ensures the save slice processor. We have to only set
			// attribute 'Is using as contract' to true.
			contractSliceService.save(slice);
			//
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		//
		return Boolean.TRUE;
	}

}
