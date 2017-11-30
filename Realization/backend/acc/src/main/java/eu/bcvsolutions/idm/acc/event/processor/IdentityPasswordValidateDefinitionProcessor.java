package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Processor with password pre-validation. Get all accounts and their distinct
 * systems.
 * 
 * @author Patrik Stloukal
 *
 */
@Component("accIdentityPasswordValidateDefinitionProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Pre validates identity's and all selected systems password, before password is changed.")
public class IdentityPasswordValidateDefinitionProcessor extends CoreEventProcessor<IdmIdentityDto>
		implements IdentityProcessor {

	@Autowired
	IdentityPasswordValidateProcessor validateProcessor;

	public IdentityPasswordValidateDefinitionProcessor() {
		super(IdentityEventType.PASSWORD_PREVALIDATION);
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		IdmIdentityDto identity = event.getContent();

		List<IdmPasswordPolicyDto> passwordPolicyList = validateProcessor.validateDefinition(identity,
				passwordChangeDto);
		validateProcessor.preValidate(passwordPolicyList);
		return new DefaultEventResult<>(event, this);

	}

}
