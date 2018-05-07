package eu.bcvsolutions.idm.core.model.event.processor.contract;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmContractSliceDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ContractSliceProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.model.event.ContractSliceEvent.ContractSliceEventType;

/**
 * Persists contract slice.
 * 
 * @author svandav
 *
 */
@Component
@Description("Persists contract slice.")
public class ContractSliceSaveProcessor extends CoreEventProcessor<IdmContractSliceDto>
		implements ContractSliceProcessor {

	public static final String PROCESSOR_NAME = "contract-slice-save-processor";
	//
	@Autowired
	private IdmContractSliceService service;

	public ContractSliceSaveProcessor() {
		super(ContractSliceEventType.UPDATE, ContractSliceEventType.CREATE,  ContractSliceEventType.EAV_SAVE);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmContractSliceDto> process(EntityEvent<IdmContractSliceDto> event) {
		IdmContractSliceDto slice = event.getContent();
		slice = service.saveInternal(slice);
		event.setContent(slice);

		return new DefaultEventResult<>(event, this);
	}

}
