package eu.bcvsolutions.idm.core.model.event.processor.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;

/**
 * Default password policy processor for delete entity
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Delete password policy processor.")
public class PasswordPolicyDeleteProcessor extends CoreEventProcessor<IdmPasswordPolicyDto> {
	
	public static final String PROCESSOR_NAME = "password-policy-delete-processor";
	
	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public PasswordPolicyDeleteProcessor(IdmPasswordPolicyService passwordPolicyService) {
		super(PasswordPolicyEvenType.DELETE);
		//
		Assert.notNull(passwordPolicyService);
		//
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	public EventResult<IdmPasswordPolicyDto> process(EntityEvent<IdmPasswordPolicyDto> event) {
		IdmPasswordPolicyDto dto = event.getContent();
		//
		passwordPolicyService.deleteInternal(dto);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
