package eu.bcvsolutions.idm.acc.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.service.api.SynchronizationService;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;

/**
 * Cancel synchronizations after server is restarted.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(AccInitSynchronizationProcessor.PROCESSOR_NAME)
@Description("Cancel synchronizations after server is restarted.")
public class AccInitSynchronizationProcessor extends AbstractInitApplicationProcessor {
	
	public static final String PROCESSOR_NAME = "acc-init-synchronization-processor";
	//
	@Autowired private SynchronizationService synchronizationService;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		//
		// Cancels all previously ran tasks.
		synchronizationService.init();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after ic init is created
		return CoreEvent.DEFAULT_ORDER - 10010;
	}
}
