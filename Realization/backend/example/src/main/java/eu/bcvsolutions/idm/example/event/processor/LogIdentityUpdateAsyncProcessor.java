package eu.bcvsolutions.idm.example.event.processor;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.example.ExampleModuleDescriptor;

/**
 * Log identity's username after identity is updated.
 * 
 * @author Radek Tomiška
 *
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component("exampleLogIdentityUpdateAsyncProcessor")
@Description("Logs after identity is updated")
public class LogIdentityUpdateAsyncProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {
	
	/**
	 * Processor's identifier - has to be unique by module
	 */
	public static final String PROCESSOR_NAME = "log-identity-update-async-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(LogIdentityUpdateAsyncProcessor.class);

	public LogIdentityUpdateAsyncProcessor() {
		// processing identity NOTIFY event only
		super(IdentityEventType.NOTIFY);
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		// we want to process original UPDATE event only
		// async NOTIFY event is published for CREATE, UPDATE, EAV_SAVE event types
		return super.conditional(event) && IdentityEventType.UPDATE.name().equals(event.getParentType());
	}

	@Override
	public String getName() {
		// processor's identifier - has to be unique by module
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		// event content - identity
		IdmIdentityDto updateddIdentity = event.getContent();
		// log
		LOG.info("Identity [{},{}] was updated.", updateddIdentity.getUsername(), updateddIdentity.getId());
		// result
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// notify event has their own process line - we can use default order
		return CoreEvent.DEFAULT_ORDER;
	}
}