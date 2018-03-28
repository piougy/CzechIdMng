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
@Component("exampleLogIdentityUpdateSyncProcessor")
@Description("Logs after identity is updated")
public class LogIdentityUpdateSyncProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {
	
	/**
	 * Processor's identifier - has to be unique by module
	 */
	public static final String PROCESSOR_NAME = "log-identity-update-sync-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(LogIdentityUpdateSyncProcessor.class);

	public LogIdentityUpdateSyncProcessor() {
		// processing identity UPDATE event only
		super(IdentityEventType.UPDATE);
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
		// right after identity update
		return CoreEvent.DEFAULT_ORDER + 1;
	}
}