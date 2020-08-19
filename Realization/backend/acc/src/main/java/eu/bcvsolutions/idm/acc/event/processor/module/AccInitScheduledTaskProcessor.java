package eu.bcvsolutions.idm.acc.event.processor.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.InitAccScheduledTask;
import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.AbstractInitApplicationProcessor;

/**
 * Schedule acc long running tasks.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(AccInitScheduledTaskProcessor.PROCESSOR_NAME)
@ConditionalOnProperty(prefix = "scheduler", name = "enabled", matchIfMissing = true)
@Description("Schedule acc long running tasks.")
public class AccInitScheduledTaskProcessor extends AbstractInitApplicationProcessor {

	public static final String PROCESSOR_NAME = "acc-init-scheduled-task-processor";
	//
	@Autowired private InitAccScheduledTask accScheduledTaskInitializer;
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<ModuleDescriptorDto> process(EntityEvent<ModuleDescriptorDto> event) {
		accScheduledTaskInitializer.init();
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// after all
		return CoreEvent.DEFAULT_ORDER + 10100;
	}

}
