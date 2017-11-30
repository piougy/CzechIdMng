package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Pre-validate identity password
 * 
 * @author Patrik Stloukal
 *
 */
@Component
@Description("Pre validates identity's password.")
public class IdentityPasswordValidateDefinitionProcessor extends CoreEventProcessor<IdmIdentityDto> 
implements IdentityProcessor {

	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdentityPasswordValidateDefinitionProcessor(IdmPasswordPolicyService passwordPolicyService) {
		super(IdentityEventType.PASSWORD_PREVALIDATION);
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		
		if (passwordChangeDto.isIdm()) {
			passwordPolicyService.preValidate();
		} else {
			IdmPasswordPolicyDto policy = new IdmPasswordPolicyDto();
			policy.setSpecialCharBase("");
			List<IdmPasswordPolicyDto> policies = new ArrayList<IdmPasswordPolicyDto>();
			policies.add(policy);
			passwordPolicyService.preValidate(policies);
		}
		
		return new DefaultEventResult<>(event, this);
	}

}
