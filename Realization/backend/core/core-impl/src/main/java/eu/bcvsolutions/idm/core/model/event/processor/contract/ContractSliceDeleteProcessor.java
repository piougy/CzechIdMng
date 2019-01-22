package eu.bcvsolutions.idm.core.model.event.processor.contract;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceGuaranteeFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.api.service.ContractSliceManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceGuaranteeService;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.entity.IdmContractSlice;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.scheduler.task.impl.ClearDirtyStateForContractSliceTaskExecutor;

/**
 * Deletes contract slice - ensures referential integrity.
 * 
 * @author svandav
 *
 */
@Component
@Description("Deletes contract slice.")
public class ContractSliceDeleteProcessor extends CoreEventProcessor<IdmContractSliceDto>
		implements ContractSliceProcessor {

	public static final String PROCESSOR_NAME = "contract-slice-delete-processor";
	private final IdmContractSliceService service;
	@Autowired
	private ContractSliceManager contractSliceManager;
	@Autowired
	private IdmIdentityContractService contractService;
	@Autowired
	private IdmContractSliceGuaranteeService contractGuaranteeService;
	@Autowired
	private EntityStateManager entityStateManager;
	@Autowired
	private IdmContractSliceService contractSliceService;

	@Autowired
	public ContractSliceDeleteProcessor(IdmContractSliceService service) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(service);
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {
		IdmContractSliceDto slice = event.getContent();

		// If dirty state property is presents, then will be slice only marked for
		// delete. Deleting is provided by ClearDirtyStateForContractSliceTaskExecutor!
		if (this.getBooleanProperty(IdmContractSliceService.SET_DIRTY_STATE_CONTRACT_SLICE, event.getProperties())) {

			// If is set slice as using as contract, set this flag to false
			if (slice.isUsingAsContract()) {
				slice.setUsingAsContract(false);
				service.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, slice,
						ImmutableMap.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));
			}

			Map<String, Serializable> properties = new HashMap<>(event.getProperties());
			// Creates new dirty states, dirty states must be process by executor
			createDeleteDirtyState(slice, properties);
			return new DefaultEventResult<>(event, this);
		}

		// delete dirty states for contract slice
		findAllDirtyStatesForSlices(slice.getId()).forEach(dirtyState -> {
			entityStateManager.deleteState(dirtyState);
		});

		// delete contract slice guarantees
		IdmContractSliceGuaranteeFilter filter = new IdmContractSliceGuaranteeFilter();
		filter.setContractSliceId(slice.getId());
		contractGuaranteeService.find(filter, null).forEach(guarantee -> {
			contractGuaranteeService.delete(guarantee);
		});

		UUID contractId = slice.getParentContract();
		if (contractId != null) {
			List<IdmContractSliceDto> slices = contractSliceManager.findAllSlices(contractId);
			if (slices.size() == 1) {
				// Internal delete of slice
				service.deleteInternal(slice);
				// This slice is last for parent contract. We will also deleted the parent
				// contract;
				contractService.deleteById(contractId);
				return new DefaultEventResult<>(event, this);
			}

			// Find next slice
			IdmContractSliceDto nextSlice = contractSliceManager.findNextSlice(slice, slices);

			// Internal delete of slice
			service.deleteInternal(slice);

			// If exists next slice, then update valid till on previous slice
			if (nextSlice != null) {
				contractSliceManager.updateValidTillOnPreviousSlice(nextSlice,
						contractSliceManager.findAllSlices(contractId));
			} else {
				// If next slice doesn't exists, then we need to find previous slice (last after deleting) and set valid till to infinity.
				IdmContractSliceDto previousSlice = contractSliceManager.findPreviousSlice(slice, contractSliceManager.findAllSlices(contractId));
				if(previousSlice != null) {
					// Previous slice will be valid till infinity
					previousSlice.setValidTill(null);
					contractSliceService.publish(new ContractSliceEvent(ContractSliceEventType.UPDATE, previousSlice,
							ImmutableMap.of(IdmContractSliceService.SKIP_RECALCULATE_CONTRACT_SLICE, Boolean.TRUE)));
				}
			}

			IdmContractSliceDto validSlice = contractSliceManager.findValidSlice(contractId);
			if (validSlice != null) {
				// Set next slice as is currently using in contract
				contractSliceManager.setSliceAsCurrentlyUsing(validSlice, event.getProperties());
			}
		} else {
			service.deleteInternal(slice);
		}

		//
		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Find all dirty states for contract slices
	 *
	 * @param pageable
	 * @return
	 */
	private List<IdmEntityStateDto> findAllDirtyStatesForSlices(UUID sliceId) {
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setResultCode(CoreResultCode.DIRTY_STATE.getCode());
		filter.setOwnerType(IdmContractSlice.class.getName());
		filter.setOwnerId(sliceId);
		return entityStateManager.findStates(filter, null).getContent();
	}
	
	/**
	 * Create new dirty state for delete of contract slice
	 *
	 * @param slice
	 * @param parameters
	 * @return
	 */
	private IdmEntityStateDto createDeleteDirtyState(IdmContractSliceDto slice, Map<String, Serializable> parameters) {
		Map<String, Object> transformedMarameters = new HashMap<String, Object>();
		transformedMarameters.put("entityId", slice.getId());
		// Mark state for delete the slice
		transformedMarameters.put(ClearDirtyStateForContractSliceTaskExecutor.TO_DELETE, Boolean.TRUE);
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
