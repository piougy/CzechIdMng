package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;

/**
 * After disable identity - authentication tokens will be disabled.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
@Component(IdentityAfterDisabledProcessor.PROCESSOR_NAME)
@Description("After disable identity - authentication tokens will be disabled.")
public class IdentityAfterDisabledProcessor
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {

	public static final String PROCESSOR_NAME = "core-identity-after-disable-processor";
	//
	@Autowired private TokenManager tokenManager;
	
	public IdentityAfterDisabledProcessor() {
		super(IdentityEventType.UPDATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		return super.conditional(event)
				&& !event.getOriginalSource().isDisabled()
				&& event.getContent().isDisabled();
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		Assert.notNull(identity.getId(), "Identity identifier is required.");
		//
		// disabled related tokens
		tokenManager.disableTokens(identity);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 5;
	}
}
