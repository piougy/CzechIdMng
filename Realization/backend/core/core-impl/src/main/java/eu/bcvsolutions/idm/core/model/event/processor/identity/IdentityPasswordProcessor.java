package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;

/**
 * Save identity's password
 * 
 * @author Radek Tomiška
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Component
@Description("Persist identity's password.")
public class IdentityPasswordProcessor extends CoreEventProcessor<IdmIdentityDto> {

	public static final String PROCESSOR_NAME = "identity-password-processor";
	public static final String PROPERTY_PASSWORD_CHANGE_DTO = "idm:password-change-dto";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProcessor.class);
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;

	@Autowired
	public IdentityPasswordProcessor(IdmPasswordService passwordService,
			IdmPasswordPolicyService passwordPolicyService) {
		super(IdentityEventType.PASSWORD);
		//
		Assert.notNull(passwordService);
		Assert.notNull(passwordPolicyService);
		//
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		IdmIdentityDto identity = event.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties()
				.get(PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(passwordChangeDto);
		//
		if (passwordChangeDto.isAll() || passwordChangeDto.isIdm()) { // change identity's password
			savePassword(identity, passwordChangeDto);
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
			IdmPasswordPolicy defaultValidatePolicy = passwordPolicyService
					.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
			if (defaultValidatePolicy != null && defaultValidatePolicy.getMaxPasswordAge() != null) {
				// put new valid till by default password policy
				passwordChangeDto
						.setMaxPasswordAge(DateTime.now().plusDays(defaultValidatePolicy.getMaxPasswordAge()));
			} else {
				// TODO: when not found default password policy throw error?
				passwordChangeDto.setMaxPasswordAge(null);
				LOG.debug("Default validate password policy not exists or max password age is not filled. For identity username [{}] will be valid till null.", 
						identity.getUsername());
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