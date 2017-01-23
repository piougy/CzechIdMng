package eu.bcvsolutions.idm.acc.event.processor;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityPasswordProcessor;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.security.api.domain.Enabled;

/**
 * Processor with password validation.
 * Get all accounts and their distinct systems.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("accIdentityPasswordValidateProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Validates identity's and all selected systems password, when password is changed.")
public class IdentityPasswordValidateProcessor extends AbstractEntityEventProcessor<IdmIdentity> {
	
	public static final String PROCESSOR_NAME = "identity-password-validate-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordValidateProcessor.class);
	private final IdmPasswordPolicyService passwordPolicyService;
	private final AccIdentityAccountService identityAccountService; 
	
	@Autowired
	public IdentityPasswordValidateProcessor(IdmPasswordPolicyService passwordPolicyService,
			AccIdentityAccountService identityAccountService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(passwordPolicyService);
		//
		this.passwordPolicyService = passwordPolicyService;
		this.identityAccountService = identityAccountService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		IdmIdentity identity = event.getContent();
		//
		Assert.notNull(passwordChangeDto);
		Assert.notNull(identity);
		//
		LOG.debug("Call validate password for systems and default password policy for identity username [{}]", event.getContent().getUsername());
		//
		List<IdmPasswordPolicy> passwordPolicyList = new ArrayList<>();
		//
		// Find user accounts
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(identity.getId());
		List<AccIdentityAccount> identityAccounts = identityAccountService.find(filter, null).getContent();
		//
		// get default password policy
		IdmPasswordPolicy defaultPasswordPolicy = this.passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		//
		if (passwordChangeDto.isIdm()) {
			passwordPolicyList.add(defaultPasswordPolicy);
		}
		//
		// get systems, only ownership
		identityAccounts.stream().filter(identityAccount -> {
			return identityAccount.isOwnership() && (passwordChangeDto.isAll()
					|| passwordChangeDto.getAccounts().contains(identityAccount.getId().toString()));
		}).forEach(identityAccount -> {
			IdmPasswordPolicy passwordPolicy = identityAccount.getAccount().getSystem().getPasswordPolicy();
			// if passwordPolicy is null use default password policy for validate
			if (passwordPolicy == null) {
				passwordPolicy = defaultPasswordPolicy;
			}
			if (!passwordPolicyList.contains(passwordPolicy)) {
				passwordPolicyList.add(passwordPolicy);
			}
		});
		//
		// validate TODO: validate for admin?
		this.passwordPolicyService.validate(passwordChangeDto.getNewPassword().asString(), passwordPolicyList, null);
		//
		// if change password for idm iterate by all policies and get min attribute of
		// max password age and set it into DTO, for save password processor
		if (passwordChangeDto.isIdm()) {
			Integer maxAgeInt = this.passwordPolicyService.getMaxPasswordAge(passwordPolicyList);
			if (maxAgeInt != null) {
				DateTime maxPasswordAge = new DateTime();
				// set into DTO, in identity password save processor was add into IdmIdentityPassword
				passwordChangeDto.setMaxPasswordAge(maxPasswordAge.plusDays(maxAgeInt));
			}
		}
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PASSWORD_VALIDATION_ORDER;
	}

}
