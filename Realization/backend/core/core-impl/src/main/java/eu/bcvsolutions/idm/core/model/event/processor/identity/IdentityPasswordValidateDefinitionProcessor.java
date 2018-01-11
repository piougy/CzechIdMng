package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.PasswordChangeProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.event.PasswordChangeEvent;
import eu.bcvsolutions.idm.core.model.event.PasswordChangeEvent.PasswordChangeEventType;

/**
 * Pre-validate identity password
 * 
 * @author Patrik Stloukal
 *
 */
@Component
@Description("Pre validates identity's password.")
public class IdentityPasswordValidateDefinitionProcessor extends CoreEventProcessor<PasswordChangeDto> 
implements PasswordChangeProcessor {

	public static final String PROCESSOR_NAME = "identity-password-validate-definition-processor";
	private final IdmPasswordPolicyService passwordPolicyService;
	
	@Autowired
	public IdentityPasswordValidateDefinitionProcessor(IdmPasswordPolicyService passwordPolicyService) {
		super(PasswordChangeEventType.PASSWORD_PREVALIDATION);
		this.passwordPolicyService = passwordPolicyService;
	}
	
	@Override
	public EventResult<PasswordChangeDto> process(EntityEvent<PasswordChangeDto> event) {
		IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
		if (event.getContent().isIdm()) {
			passwordPolicyService.preValidate(passwordValidationDto);
		} else {
			passwordPolicyService.preValidate(passwordValidationDto, new ArrayList<IdmPasswordPolicyDto>());
		}
		
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return PasswordChangeEvent.DEFAULT_ORDER;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

}
