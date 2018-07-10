package eu.bcvsolutions.idm.core.model.event.processor.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.TokenProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmTokenService;
import eu.bcvsolutions.idm.core.model.event.TokenEvent.TokenEventType;

/**
 * Deletes tokens - ensures referential integrity.
 * 
 * @author Radek Tomiška
 * @since 8.2.0
 */
@Component
@Description("Deletes token from repository.")
public class TokenDeleteProcessor
		extends CoreEventProcessor<IdmTokenDto>
		implements TokenProcessor{
	
	public static final String PROCESSOR_NAME = "token-delete-processor";
	@Autowired private IdmTokenService service;
	
	public TokenDeleteProcessor() {
		super(TokenEventType.DELETE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmTokenDto> process(EntityEvent<IdmTokenDto> event) {
		IdmTokenDto token = event.getContent();
		//		
		service.deleteInternal(token);
		//
		return new DefaultEventResult<>(event, this);
	}
}