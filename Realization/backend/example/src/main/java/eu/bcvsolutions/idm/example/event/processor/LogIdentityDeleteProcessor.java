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
 * Log identity's username after identity is deleted.
 * 
 * @author Radek Tomiška
 *
 */
@Enabled(ExampleModuleDescriptor.MODULE_ID)
@Component("exampleLogIdentityDeleteProcessor")
@Description("Logs after identity is deleted")
public class LogIdentityDeleteProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {
	
	/**
	 * Processor's identifier - has to be unique by module
	 */
	public static final String PROCESSOR_NAME = "log-identity-delete-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(LogIdentityDeleteProcessor.class);

	public LogIdentityDeleteProcessor() {
		// processing identity DELETE event only
		super(IdentityEventType.DELETE);
	}

	@Override
	public String getName() {
		// processor's identifier - has to be unique by module
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		// event content - identity
		IdmIdentityDto deletedIdentity = event.getContent();
		// log
		LOG.info("Identity [{},{}] was deleted.", deletedIdentity.getUsername(), deletedIdentity.getId());
		// result
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		// right after identity delete
		return CoreEvent.DEFAULT_ORDER + 1;
	}
}