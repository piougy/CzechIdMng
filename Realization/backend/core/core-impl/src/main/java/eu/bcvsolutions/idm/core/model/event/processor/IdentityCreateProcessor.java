package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Processor for only create user, for now is there only validate password
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Order(-10)
@Component
public class IdentityCreateProcessor extends CoreEventProcessor<IdmIdentity>{
	
private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdentityCreateProcessor(
			IdmPasswordPolicyService passwordPolicyService) {
		super(IdentityEventType.CREATE);
		//
		Assert.notNull(passwordPolicyService);
		//
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		GuardedString password = event.getContent().getPassword();
		
		// when create identity password can be null
		if (password != null) {
			IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
			passwordValidationDto.setPassword(password);
			// validate create new password by default password policy
			this.passwordPolicyService.validate(passwordValidationDto);
		}
		
		return new DefaultEventResult<>(event, this);
	}

}
