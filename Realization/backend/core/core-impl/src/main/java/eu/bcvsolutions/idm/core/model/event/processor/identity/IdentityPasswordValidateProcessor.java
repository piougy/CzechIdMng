package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Validate identity password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Validates identity's password, when password is changed.")
public class IdentityPasswordValidateProcessor extends AbstractIdentityPasswordValidateProcessor {

	public static final String PROCESSOR_NAME = "identity-password-validate-processor";

	protected final SecurityService securityService;

	@Autowired
	public IdentityPasswordValidateProcessor(
		SecurityService securityService,
		IdmPasswordService passwordService,
		IdmPasswordPolicyService passwordPolicyService,
		AuthenticationManager authenticationManager,
		IdentityConfiguration identityConfiguration) {
		super(identityConfiguration, passwordService, authenticationManager, passwordPolicyService, securityService, IdentityEventType.PASSWORD);
		//
		Assert.notNull(securityService);
		//
		this.securityService = securityService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return super.getOrder() - 1100;
	}

	@Override
	protected boolean requiresOriginalPassword() {
		return !securityService.isAdmin();
	}
}
