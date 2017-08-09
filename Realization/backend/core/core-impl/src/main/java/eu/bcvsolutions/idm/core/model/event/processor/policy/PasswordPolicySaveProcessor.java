package eu.bcvsolutions.idm.core.model.event.processor.policy;

import org.apache.commons.lang3.math.NumberUtils;
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
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy_;
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
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_DEFAULT_TYPE,
					ImmutableMap.of("name", entity.getName()));
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
	 * Method check attributes of password policy TODO: send all error message
	 * at once?
	 * 
	 * @param entity
	 * @return true, if password policy attribute are valid, otherwise throw
	 *         error
	 */
	private boolean validatePasswordPolicyAttributes(IdmPasswordPolicy entity) {
		// check negative values
		checkNegativeValues(entity);
		// password policy can contains null value
		int maximumLength = returnZeroValue(entity.getMaxPasswordLength());
		int minimumLength = returnZeroValue(entity.getMinPasswordLength());

		if (maximumLength != 0 && maximumLength < minimumLength) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_LENGTH_LOWER);
		}

		int minimumLengthAll = returnZeroValue(entity.getMinLowerChar());
		minimumLengthAll += returnZeroValue(entity.getMinUpperChar());
		minimumLengthAll += returnZeroValue(entity.getMinSpecialChar());
		minimumLengthAll += returnZeroValue(entity.getMinNumber());
		if (maximumLength != 0 && (minimumLengthAll > maximumLength)) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_ALL_MIN_REQUEST_ARE_HIGHER);
		}

		int maxPasswordAge = returnZeroValue(entity.getMaxPasswordAge());
		int minPasswordAge = returnZeroValue(entity.getMinPasswordAge());
		if (maxPasswordAge != 0 && maxPasswordAge < minPasswordAge) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_AGE_LOWER);
		}
		// check minRulesToFulfill and rules
		if (entity.isEnchancedControl()) {
			// get number of not required rules and compare to minFulfill rules
			int rules = entity.getNotRequiredRules();
			//
			// check with minRulesToFulfill
			if (entity.getMinRulesToFulfill() != null && entity.getMinRulesToFulfill().intValue() > rules) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_RULE, ImmutableMap.of("rules", rules));
			}
		}
		return true;
	}

	private void checkNegativeValues(IdmPasswordPolicy entity) {
		if (returnZeroValue(entity.getMaxHistorySimilar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.maxHistorySimilar.getName()));
		}
		if (returnZeroValue(entity.getMaxPasswordAge()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.maxPasswordAge.getName()));
		}
		if (returnZeroValue(entity.getMaxPasswordLength()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.maxPasswordLength.getName()));
		}
		if (returnZeroValue(entity.getMinLowerChar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minLowerChar.getName()));
		}
		if (returnZeroValue(entity.getMinNumber()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minNumber.getName()));
		}
		if (returnZeroValue(entity.getMinPasswordAge()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minPasswordAge.getName()));
		}
		if (returnZeroValue(entity.getMinPasswordLength()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minPasswordLength.getName()));
		}
		if (returnZeroValue(entity.getMinRulesToFulfill()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minRulesToFulfill.getName()));
		}
		if (returnZeroValue(entity.getMinSpecialChar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minSpecialChar.getName()));
		}
		if (returnZeroValue(entity.getMinUpperChar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minSpecialChar.getName()));
		}
		if (returnZeroValue(entity.getPassphraseWords()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.passphraseWords.getName()));
		}
	}

	/**
	 * Method return zero value if integer is null.
	 * 
	 * @param value
	 * @return value given in parameter or zero when value is null.
	 */
	private int returnZeroValue(Integer value) {
		if (value == null) {
			return 0;
		}
		return value.intValue();
	}
}
