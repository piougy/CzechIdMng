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
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;

/**
 * Persists authorization policies.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Component
@Description("Persists authorization policies.")
public class AuthorizationPolicySaveProcessor extends CoreEventProcessor<IdmAuthorizationPolicyDto> {

	private static final String PROCESSOR_NAME = "authorization-policy-save-processor";

	private final IdmAuthorizationPolicyService service;

	@Autowired
	public AuthorizationPolicySaveProcessor(IdmAuthorizationPolicyService service) {
		super(AuthorizationPolicyEventType.CREATE, AuthorizationPolicyEventType.UPDATE);
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
		IdmAuthorizationPolicyDto dto = event.getContent();
		dto = service.saveInternal(dto);
		event.setContent(dto);
		//
		return new DefaultEventResult<>(event, this);
	}

}
