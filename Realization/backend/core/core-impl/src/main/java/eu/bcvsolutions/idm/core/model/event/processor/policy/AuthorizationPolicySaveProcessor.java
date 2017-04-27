package eu.bcvsolutions.idm.core.model.event.processor.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.model.event.AuthorizationPolicyEvent.AuthorizationPolicyEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmAuthorizationPolicyRepository;

/**
 * Persists authorization policies.
 * 
 * @author Jan Helbich
 */
@Component
@Description("Persists authorization policies.")
public class AuthorizationPolicySaveProcessor extends CoreEventProcessor<IdmAuthorizationPolicy> {

	private static final String PROCESSOR_NAME = "authorization-policy-save-processor";

	private final IdmAuthorizationPolicyRepository repository;

	@Autowired
	public AuthorizationPolicySaveProcessor(IdmAuthorizationPolicyRepository repository) {
		super(AuthorizationPolicyEventType.CREATE, AuthorizationPolicyEventType.UPDATE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmAuthorizationPolicy> process(EntityEvent<IdmAuthorizationPolicy> event) {
		IdmAuthorizationPolicy entity = repository.save(event.getContent());
		event.setContent(entity);
		//
		return new DefaultEventResult<>(event, this);
	}

}
