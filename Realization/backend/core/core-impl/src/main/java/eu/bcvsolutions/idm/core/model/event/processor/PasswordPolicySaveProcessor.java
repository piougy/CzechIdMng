package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;

/**
 * Default password policy event processor for create and update password policy
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Validation and save password policy processor.")
public class PasswordPolicySaveProcessor extends CoreEventProcessor<IdmPasswordPolicy> {
	
	public static final String PROCESSOR_NAME = "password-policy-save-processor";
	
	private final IdmPasswordPolicyRepository passwordPolicyRepository;
	
	@Autowired
	public PasswordPolicySaveProcessor(IdmPasswordPolicyRepository passwordPolicyRepository) {
		super(PasswordPolicyEvenType.UPDATE, PasswordPolicyEvenType.CREATE);
		//
		Assert.notNull(passwordPolicyRepository);
		//
		this.passwordPolicyRepository = passwordPolicyRepository;
	}
	
	@Override
	public EventResult<IdmPasswordPolicy> process(EntityEvent<IdmPasswordPolicy> event) {
		IdmPasswordPolicy entity = event.getContent();
		//
		if (validatePasswordPolicyAttributes(entity)) {
			if (entity.isDefaultPolicy()) {
				this.passwordPolicyRepository.updateDefaultPolicyByType(entity.getType(), entity.getId());
			}
		} else {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_DEFAULT_TYPE, ImmutableMap.of("name", entity.getName()));
		}
		//
		passwordPolicyRepository.save(entity);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	/**
	 * Method check attributes of password policy
	 * TODO: send all error message at once?
	 * 
	 * @param entity
	 * @return true, if password policy attribute are valid, otherwise throw error
	 */
	private boolean validatePasswordPolicyAttributes(IdmPasswordPolicy entity) {
		if (entity.getMaxPasswordLength() != 0 && entity.getMaxPasswordLength() < entity.getMinPasswordLength()) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_LENGTH_LOWER);
		}
		if (entity.getMaxPasswordLength() != 0 && (entity.getMinLowerChar() + entity.getMinNumber() + entity.getMinSpecialChar() + entity.getMinUpperChar() > 
				entity.getMaxPasswordLength())) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_ALL_MIN_REQUEST_ARE_HIGHER);
		}
		if (entity.getMaxPasswordAge() != 0 && entity.getMaxPasswordAge() < entity.getMinPasswordAge()) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_AGE_LOWER);
		}
		// check minRulesToFulfill and rules
		if (entity.isEnchancedControl()) {
			// get number of not required rules and compare to minFulfill rules
			int rules = entity.getNotRequiredRules();
			//
			// check with minRulesToFulfill
			if (entity.getMinRulesToFulfill() > rules) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_RULE, ImmutableMap.of("rules", rules));
			}
		}
		return true;
	}
}
