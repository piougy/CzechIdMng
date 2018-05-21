package eu.bcvsolutions.idm.acc.event.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.event.PasswordChangeEvent;
import eu.bcvsolutions.idm.core.model.event.PasswordChangeEvent.PasswordChangeEventType;
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
@Description("Pre validates identity's and all selected system's password, before password is changed.")
public class IdentityPasswordPreValidateDefinitionProcessor extends CoreEventProcessor<PasswordChangeDto> {

	public static final String PROCESSOR_NAME = "identity-password-pre-validate-definition-processor-acc";
	private final IdmPasswordPolicyService passwordPolicyService;
	private final AccAccountService accountService;

	@Autowired
	public IdentityPasswordPreValidateDefinitionProcessor(IdmPasswordPolicyService passwordPolicyService,
			AccAccountService accountService) {
		super(PasswordChangeEventType.PASSWORD_PREVALIDATION);
		//
		Assert.notNull(accountService);
		Assert.notNull(passwordPolicyService);
		//
		this.passwordPolicyService = passwordPolicyService;
		this.accountService = accountService;
	}

	@Override
	public EventResult<PasswordChangeDto> process(EntityEvent<PasswordChangeDto> event) {
		PasswordChangeDto passwordChangeDto = event.getContent();

		IdmPasswordValidationDto passwordValidationDto = new IdmPasswordValidationDto();
		List<IdmPasswordPolicyDto> passwordPolicyList = validateDefinition(passwordChangeDto);
		this.passwordPolicyService.preValidate(passwordValidationDto, passwordPolicyList);
		return new DefaultEventResult<>(event, this);

	}

	public List<IdmPasswordPolicyDto> validateDefinition(PasswordChangeDto passwordChangeDto) {
		List<IdmPasswordPolicyDto> passwordPolicyList = new ArrayList<>();
		IdmPasswordPolicyDto defaultPasswordPolicy = this.passwordPolicyService
				.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);

		if (defaultPasswordPolicy == null) {
			defaultPasswordPolicy = new IdmPasswordPolicyDto();
		}
		for (String account : passwordChangeDto.getAccounts()) {
			SysSystemDto system = DtoUtils.getEmbedded(accountService.get(UUID.fromString(account)), AccAccount_.system);
			IdmPasswordPolicyDto passwordPolicy;
			//
			if (system.getPasswordPolicyValidate() == null) {
				passwordPolicy = defaultPasswordPolicy;
			} else {
				passwordPolicy = passwordPolicyService.get(system.getPasswordPolicyValidate());
			}
			if (!passwordPolicyList.contains(passwordPolicy) && passwordPolicy != null) {
				passwordPolicyList.add(passwordPolicy);
			}
		}
		if (passwordChangeDto.isIdm() && !passwordPolicyList.contains(defaultPasswordPolicy)) {
			passwordPolicyList.add(defaultPasswordPolicy);
		}
		return passwordPolicyList;
	}

	@Override
	public int getOrder() {
		return PasswordChangeEvent.DEFAULT_ORDER - 10;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
}
