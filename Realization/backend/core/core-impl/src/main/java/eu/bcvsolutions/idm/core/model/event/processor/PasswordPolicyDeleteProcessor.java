package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;

/**
 * Default password policy processor for delete entity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Delete password policy processor.")
public class PasswordPolicyDeleteProcessor extends CoreEventProcessor<IdmPasswordPolicy> {
	
	public static final String PROCESSOR_NAME = "password-policy-delete-processor";
	
	private final IdmPasswordPolicyRepository passwordPolicyRepository;
	
	@Autowired
	public PasswordPolicyDeleteProcessor(IdmPasswordPolicyRepository passwordPolicyRepository) {
		super(PasswordPolicyEvenType.DELETE);
		//
		Assert.notNull(passwordPolicyRepository);
		//
		this.passwordPolicyRepository = passwordPolicyRepository;
	}
	
	@Override
	public EventResult<IdmPasswordPolicy> process(EntityEvent<IdmPasswordPolicy> event) {
		IdmPasswordPolicy entity = event.getContent();
		//
		passwordPolicyRepository.delete(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
