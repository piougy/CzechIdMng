package eu.bcvsolutions.idm.core.model.event.processor.identity;

import java.util.LinkedHashMap;
import java.util.Map;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmAccountDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Save identity's password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Persist identity's password.")
public class IdentityPasswordProcessor extends AbstractIdentityPasswordProcessor {

	public static final String PROCESSOR_NAME = "identity-password-processor";
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProcessor.class);
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;
	private final IdentityConfiguration identityConfiguration;

	@Autowired
	public IdentityPasswordProcessor(IdmPasswordService passwordService,
			IdmPasswordPolicyService passwordPolicyService,
			IdentityConfiguration identityConfiguration) {
		super(passwordService, IdentityEventType.PASSWORD);
		//
		Assert.notNull(passwordService);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(identityConfiguration);
		//
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
		this.identityConfiguration = identityConfiguration;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		// check if is allowed change password for IdM
		if (identityConfiguration.isAllowedChangePasswordForIdm()) {
			return super.process(event);
		}
		//
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(PROPERTY_PASSWORD_CHANGE_DTO);
		if (passwordChangeDto.isIdm()) {
			IdmIdentityDto identity = event.getContent();
			Map<String, Object> parameters = new LinkedHashMap<>();
			parameters.put("account", new IdmAccountDto(
					identity.getId(), 
					true, 
					identity.getUsername()));
			return new DefaultEventResult.Builder<>(event, this).setResult(
					new OperationResult.Builder(OperationState.NOT_EXECUTED)
					.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_CHANGE_ACCOUNT_FAILED, parameters))
					.build()
					).build();
		}
		return new DefaultEventResult<>(event, this);
	}

	/**
	 * Saves identity's password and fill valid till from password policy
	 * 
	 * @param identity
	 * @param newPassword
	 */
	protected void savePassword(IdmIdentityDto identity, PasswordChangeDto passwordChangeDto) {
		LOG.debug("Saving password for identity [{}].", identity.getUsername());
		// 
		if (passwordChangeDto.getMaxPasswordAge() == null) {
			IdmPasswordPolicyDto defaultValidatePolicy = passwordPolicyService
					.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			if (defaultValidatePolicy != null && defaultValidatePolicy.getMaxPasswordAge() != null) {
				// put new valid till by default password policy
				passwordChangeDto.setMaxPasswordAge(DateTime.now().plusDays(defaultValidatePolicy.getMaxPasswordAge()));
			} else {
				passwordChangeDto.setMaxPasswordAge(null);
				LOG.warn("Default validate password policy not exists or max password age is not filled."
						+ " For identity username [{}] will be valid till null.", identity.getUsername());
			}
		}
		this.passwordService.save(identity, passwordChangeDto);
	}

	/**
	 * Delete identity's password from confidential storage
	 * 
	 * @param identity
	 */
	protected void deletePassword(IdmIdentityDto identity) {
		LOG.debug("Deleting password for identity [{}]. ", identity.getUsername());
		this.passwordService.delete(identity);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}
}
