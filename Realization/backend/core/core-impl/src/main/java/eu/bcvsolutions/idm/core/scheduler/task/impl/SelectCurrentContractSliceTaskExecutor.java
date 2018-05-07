package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
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
public class SelectCurrentContractSliceTaskExecutor extends AbstractSchedulableTaskExecutor<OperationResult> {

	private static final Logger LOG = LoggerFactory.getLogger(SelectCurrentContractSliceTaskExecutor.class);
	@Autowired
	private ContractSliceManager contractSliceManager;

	@Override
	@Transactional
	public OperationResult process() {
		// Found all unvalid slices
		List<IdmContractSliceDto> unvalidSlices = contractSliceManager.findUnvalidSlices(null).getContent();
		boolean canContinue = true;
		//
		this.counter = 0L;
		this.count = Long.valueOf(unvalidSlices.size());
		StringBuilder duplicitiesMessage = null;
		List<UUID> invalidContracts = new ArrayList<>();

		for (IdmContractSliceDto slice : unvalidSlices) {
			// Start recalculation
			List<IdmContractSliceDto> duplicatedSlices = unvalidSlices.stream()
					.filter(s -> s.getParentContract().equals(slice.getParentContract())).collect(Collectors.toList());
			if (duplicatedSlices.size() > 1) {
				String errorMsg = MessageFormat.format(
						"We found more then once slice [{1}] which should be use as contract. This is not allowed. None from this slices will be used as contract. It means contract [{0}] is in incorrect state now!",
						slice.getParentContract(), duplicatedSlices.size());
				LOG.warn(errorMsg);
				if (duplicitiesMessage == null) {
					duplicitiesMessage = new StringBuilder();
				}
				if (!invalidContracts.contains(slice.getParentContract())) {
					duplicitiesMessage.append(slice.getParentContract()).append(",");
					invalidContracts.add(slice.getParentContract());
				}

			} else {
				contractSliceManager.setSliceAsCurrentlyUsing(slice);
				//
				counter++;
			}
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		if (duplicitiesMessage != null) {
			return new OperationResult.Builder(OperationState.EXCEPTION)
					.setException(new ResultCodeException(CoreResultCode.CONTRACT_SLICE_DUPLICATE_CANDIDATES,
							ImmutableMap.of("contracts", duplicitiesMessage.toString())))
					.build();
		}

		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}

	@Override
	protected OperationResult end(OperationResult result, Exception ex) {
		if (result.getException() != null) {
			return super.end(result, (Exception) result.getException());
		}
		return super.end(result, ex);
	}

}
