package eu.bcvsolutions.idm.core.scheduler.task.impl;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
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

			contractSliceManager.setSliceAsCurrentlyUsing(slice);
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
