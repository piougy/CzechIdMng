package eu.bcvsolutions.idm.core.model.event.processor.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmAuthorizationPolicyService;

/**
 * Persists authorization policies.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Component
@Description("Deletes authorization policies.")
public class AuthorizationPolicyDeleteProcessor extends CoreEventProcessor<IdmAuthorizationPolicyDto> {

	private static final String PROCESSOR_NAME = "authorization-policy-delete-processor";

	private final IdmAuthorizationPolicyService service;

	@Autowired
	public AuthorizationPolicyDeleteProcessor(
			IdmAuthorizationPolicyService service) {
		super(AuthorizationPolicyEventType.DELETE);
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
	public EventResult<IdmAuthorizationPolicyDto> process(EntityEvent<IdmAuthorizationPolicyDto> event) {
		service.deleteInternal(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}

}
