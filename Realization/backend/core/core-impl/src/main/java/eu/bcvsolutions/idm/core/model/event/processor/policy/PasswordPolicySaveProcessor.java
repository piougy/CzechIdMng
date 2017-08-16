package eu.bcvsolutions.idm.core.model.event.processor.policy;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy_;
import eu.bcvsolutions.idm.core.model.event.PasswordPolicyEvent.PasswordPolicyEvenType;
import eu.bcvsolutions.idm.core.model.repository.IdmPasswordPolicyRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordPolicyService;

/**
 * Default password policy event processor for create and update password policy
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component
@Description("Validation and save password policy processor.")
public class PasswordPolicySaveProcessor extends CoreEventProcessor<IdmPasswordPolicyDto> {

	public static final String PROCESSOR_NAME = "password-policy-save-processor";

	private final IdmPasswordPolicyRepository passwordPolicyRepository;
	private final IdmPasswordPolicyService passwordPolicyService;

	@Autowired
	public PasswordPolicySaveProcessor(IdmPasswordPolicyRepository passwordPolicyRepository,
			IdmPasswordPolicyService passwordPolicyService) {
		super(PasswordPolicyEvenType.UPDATE, PasswordPolicyEvenType.CREATE);
		//
		Assert.notNull(passwordPolicyRepository);
		Assert.notNull(passwordPolicyService);
		//
		this.passwordPolicyRepository = passwordPolicyRepository;
		this.passwordPolicyService = passwordPolicyService;
	}

	@Override
	public EventResult<IdmPasswordPolicyDto> process(EntityEvent<IdmPasswordPolicyDto> event) {
		IdmPasswordPolicyDto dto = event.getContent();
		//
		if (validatePasswordPolicyAttributes(dto)) {
			if (dto.isDefaultPolicy()) {
				this.passwordPolicyRepository.updateDefaultPolicyByType(dto.getType(), dto.getId());
			}
		} else {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_DEFAULT_TYPE,
					ImmutableMap.of("name", dto.getName()));
		}
		//
		dto = passwordPolicyService.saveInternal(dto);
		event.setContent(dto);
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
	 * @param dto
	 * @return true, if password policy attribute are valid, otherwise throw
	 *         error
	 */
	private boolean validatePasswordPolicyAttributes(IdmPasswordPolicyDto dto) {
		// check negative values
		checkNegativeValues(dto);
		// password policy can contains null value
		int maximumLength = returnZeroValue(dto.getMaxPasswordLength());
		int minimumLength = returnZeroValue(dto.getMinPasswordLength());

		if (maximumLength != 0 && maximumLength < minimumLength) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_LENGTH_LOWER);
		}

		int minimumLengthAll = returnZeroValue(dto.getMinLowerChar());
		minimumLengthAll += returnZeroValue(dto.getMinUpperChar());
		minimumLengthAll += returnZeroValue(dto.getMinSpecialChar());
		minimumLengthAll += returnZeroValue(dto.getMinNumber());
		if (maximumLength != 0 && (minimumLengthAll > maximumLength)) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_ALL_MIN_REQUEST_ARE_HIGHER);
		}

		int maxPasswordAge = returnZeroValue(dto.getMaxPasswordAge());
		int minPasswordAge = returnZeroValue(dto.getMinPasswordAge());
		if (maxPasswordAge != 0 && maxPasswordAge < minPasswordAge) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_AGE_LOWER);
		}
		// check minRulesToFulfill and rules
		if (dto.isEnchancedControl()) {
			// get number of not required rules and compare to minFulfill rules
			int rules = dto.getNotRequiredRules();
			//
			// check with minRulesToFulfill
			if (dto.getMinRulesToFulfill() != null && dto.getMinRulesToFulfill().intValue() > rules) {
				throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_MAX_RULE, ImmutableMap.of("rules", rules));
			}
		}
		return true;
	}

	private void checkNegativeValues(IdmPasswordPolicyDto dto) {
		if (returnZeroValue(dto.getMaxHistorySimilar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.maxHistorySimilar.getName()));
		}
		if (returnZeroValue(dto.getMaxPasswordAge()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.maxPasswordAge.getName()));
		}
		if (returnZeroValue(dto.getMaxPasswordLength()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.maxPasswordLength.getName()));
		}
		if (returnZeroValue(dto.getMinLowerChar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minLowerChar.getName()));
		}
		if (returnZeroValue(dto.getMinNumber()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minNumber.getName()));
		}
		if (returnZeroValue(dto.getMinPasswordAge()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minPasswordAge.getName()));
		}
		if (returnZeroValue(dto.getMinPasswordLength()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minPasswordLength.getName()));
		}
		if (returnZeroValue(dto.getMinRulesToFulfill()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minRulesToFulfill.getName()));
		}
		if (returnZeroValue(dto.getMinSpecialChar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minSpecialChar.getName()));
		}
		if (returnZeroValue(dto.getMinUpperChar()) < NumberUtils.INTEGER_ZERO) {
			throw new ResultCodeException(CoreResultCode.PASSWORD_POLICY_NEGATIVE_VALUE,
					ImmutableMap.of("attribute", IdmPasswordPolicy_.minSpecialChar.getName()));
		}
		if (returnZeroValue(dto.getPassphraseWords()) < NumberUtils.INTEGER_ZERO) {
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
