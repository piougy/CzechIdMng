package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ClearDirtyStateForContractSliceTaskExecutor;

/**
 * Update/recalculate contract by slice
 * 
 * @author svandav
 *
 */
@Component
@Description("Update/recalculate contract by slice")
public class ContractSliceSaveRecalculateProcessor extends CoreEventProcessor<IdmContractSliceDto>
		implements ContractSliceProcessor {

	public static final String PROCESSOR_NAME = "contract-slice-save-recalculate-processor";
	//
	@Autowired
	private IdmContractSliceService service;
	@Autowired
	private ContractSliceManager sliceManager;
	@Autowired
	private EntityStateManager entityStateManager;

	public ContractSliceSaveRecalculateProcessor() {
		super(ContractSliceEventType.UPDATE, ContractSliceEventType.CREATE, ContractSliceEventType.EAV_SAVE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {
		IdmContractSliceDto slice = event.getContent();
		IdmContractSliceDto originalSlice = event.getOriginalSource();

		// There is conditional for executing this processor
		if (this.getBooleanProperty(IdmContractSliceService.SET_DIRTY_STATE_CONTRACT_SLICE, event.getProperties())) {
			
			// If is set slice as using as contract, set this flag to false
			if (slice.isUsingAsContract()) {
				slice.setUsingAsContract(false);
				service.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, slice,
						ImmutableMap.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));
			}

			Map<String, Serializable> properties = new HashMap<>(event.getProperties());
			// save original slice into parameters, executor ClearDirtyFlagForContractSliceTaskExecutor need him for recalculation
			properties.put(ClearDirtyStateForContractSliceTaskExecutor.ORIGINAL_SLICE, originalSlice);
			// Creates new dirty states, dirty states must be process by executor
			createDirtyState(slice, properties);
			return new DefaultEventResult<>(event, this);
		}

		// recalculation
		Map<String, Serializable> eventProperties = event.getProperties();
		sliceManager.recalculateContractSlice(slice, originalSlice, eventProperties);

		event.setContent(service.get(slice.getId()));
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmContractSliceDto> event) {
		// Skip recalculation totally skip the processor, set dirty state just set dirty for current slice and previous state
		return !this.getBooleanProperty(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, event.getProperties());
	}

	@Override
	public int getOrder() {
		// Execute after save
		return super.getOrder() + 1;
	}

	/**
	 * Create new dirty state for contract slice
	 *
	 * @param slice
	 * @param parameters
	 * @return
	 */
	private IdmEntityStateDto createDirtyState(IdmContractSliceDto slice, Map<String, Serializable> parameters) {
		Map<String, Object> transformedMarameters = new HashMap<String, Object>();
		transformedMarameters.put("entityId", slice.getId());
		transformedMarameters.putAll(parameters);
		
		DefaultResultModel resultModel = new DefaultResultModel(CoreResultCode.DIRTY_STATE, transformedMarameters);
		IdmEntityStateDto dirtyState = new IdmEntityStateDto();
		dirtyState.setResult(
				new OperationResultDto
					.Builder(OperationState.BLOCKED)
					.setModel(resultModel)
					.build());
		return entityStateManager.saveState(slice, dirtyState);
	}
}
