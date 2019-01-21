package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Task for clear all dirty states for all contract slices. 
 *
 * @author Ondrej Kopr
 *
 */

@Service
@DisallowConcurrentExecution
@Description("Clear dirty state for contract slices. During synchronization is set dirty flag for all processed slices. This task remove the flag.")
public class ClearDirtyStateForContractSliceTaskExecutor extends AbstractSchedulableTaskExecutor<OperationResult> {
	
	public static String ORIGINAL_SLICE = "originalSlice";

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ClearDirtyStateForContractSliceTaskExecutor.class);

	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private IdmContractSliceService contractSliceService;
	@Autowired
	private ContractSliceManager contractSliceManager;
	@Autowired
	private EntityManager entityManager;

	@Override
	public OperationResult process() {
		boolean canContinue = true;

		List<IdmEntityStateDto> dirtyStates = findAllDirtyStatesForSlices(null).getContent();
		if (count == null) {
			count = Long.valueOf(dirtyStates.size());
		}

		counter = 0l;

		for (IdmEntityStateDto dirtyState : dirtyStates) {
			Assert.notNull(dirtyState);
			Assert.notNull(dirtyState.getId());

			processItem(dirtyState);
			counter++;

			// flush and clear session - if LRT is wrapped in parent transaction, we need to clear it (same behavior as in stateful tasks)
 			if (getHibernateSession().isOpen()) {
 				getHibernateSession().flush();
 				getHibernateSession().clear();
 			}
 
 			canContinue &= this.updateState();
 			if (!canContinue) {
 				break;
 			}
		}

		return new OperationResult(OperationState.EXECUTED);
	}

	private Session getHibernateSession() {
		return (Session) this.entityManager.getDelegate();
	}

	/**
	 * Process one dirty state for contract slice
	 *
	 * @param dirtyState
	 */
	private void processItem(IdmEntityStateDto dirtyState) {
		try {
			if (dirtyState.getOwnerType() == null || !dirtyState.getOwnerType().equals(IdmContractSlice.class.getName())) {
				this.logItemProcessed(dirtyState, new OperationResult.Builder(OperationState.NOT_EXECUTED).build());
				return;
			}
			IdmContractSliceDto contractSliceDto = contractSliceService.get(dirtyState.getOwnerId());
			if (contractSliceDto == null) {
				DefaultResultModel model = new DefaultResultModel(CoreResultCode.NOT_FOUND,
						ImmutableMap.of("ownerId", dirtyState.getOwnerId()));
				this.logItemProcessed(dirtyState, new OperationResult.Builder(OperationState.NOT_EXECUTED).setModel(model).build());
				return;
			}
			ResultModel resultModel = dirtyState.getResult().getModel();
			
			Map<String, Object> parameters = new HashMap<>();
			if (resultModel != null) {
				parameters = resultModel.getParameters();
			}
			
			IdmContractSliceDto originalSlice = null;
			Object originalSliceAsObject = parameters.get(ORIGINAL_SLICE);
			if (originalSliceAsObject instanceof IdmContractSliceDto) {
				originalSlice = (IdmContractSliceDto) originalSliceAsObject;
			}

			// Transform saved parameters into map string and serializable value
			Map<String, Serializable> transformedParameters = new HashMap<>();
			parameters.forEach((key, value) -> {
				if (key != null && ORIGINAL_SLICE.equals(key)) {
					// skip original slice
				} else if (key != null && IdmContractSliceService.SET_DIRTY_STATE_CONTRACT_SLICE.equals(key)) {
					// remove skip recalculation for contract slice
				} else if (value == null) {
					transformedParameters.put(key, null);
				} else if (value instanceof Serializable) {
					transformedParameters.put(key, (Serializable)value);
				} else {
					LOG.error("Given value [{}] with key [{}] for parameters is not posible cast to serializable. Skip the value", value, key);
				}
			});
			
			contractSliceManager.recalculateContractSlice(contractSliceDto, originalSlice, transformedParameters);
			
			this.logItemProcessed(contractSliceDto, new OperationResult.Builder(OperationState.EXECUTED).build());
			entityStateManager.deleteState(dirtyState);
		} catch (Exception e) {
			this.logItemProcessed(dirtyState, new OperationResult.Builder(OperationState.EXCEPTION).setCause(e).build());
		}
	}

	/**
	 * Find all dirty states for contract slices
	 *
	 * @param pageable
	 * @return
	 */
	private Page<IdmEntityStateDto> findAllDirtyStatesForSlices(Pageable pageable) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setResultCode(CoreResultCode.DIRTY_STATE.getCode());
		filter.setOwnerType(IdmContractSlice.class.getName());
		return entityStateManager.findStates(filter, pageable);
	}
}
