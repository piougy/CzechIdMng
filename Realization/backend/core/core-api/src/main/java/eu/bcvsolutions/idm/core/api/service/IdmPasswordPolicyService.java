package eu.bcvsolutions.idm.core.api.service;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;

/**
 * Service for valdiate password by password policy, also generate password.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmPasswordPolicyService extends ReadWriteDtoService<IdmPasswordPolicyDto, IdmPasswordPolicyFilter> {
	
	/**
	 * Method validate password by password policy,
	 * {@link validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicy> passwordPolicyList)}}.
	 * Method throw exception with all errors after complete validate if any validation problem.
	 * 
	 * @param passwordValidationDto
	 * @param passwordPolicy
	 */
	void validate(IdmPasswordValidationDto passwordValidationDto, IdmPasswordPolicyDto passwordPolicy);
	
	/**
	 * Method validate password by default validation policy. (Default IDM policy, must exist)
	 * Method throw exception with all errors after complete validate if any validation problem.
	 * 
	 * @param passwordValidationDto
	 */
	void validate(IdmPasswordValidationDto passwordValidationDto);
	
	/**
	 * Validate password by list of password policies. Validate trought all polocies,
	 * if found some error throw exception.
	 * When isn't oldPassword null, validate for password age trought policies
	 * minimal age
	 * Method throw exception with all errors after complete validate if any validation problem.
	 * 
	 * @param passwordValidationDto
	 * @param passwordPolicyList
	 */
	void validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicyDto> passwordPolicyList);
	
	/**
	 * Method return default password policy, by given type, @see {@link IdmPasswordPolicyType}
	 * 
	 * @return {@link IdmPasswordPolicy} if was founded for {@link IdmPasswordPolicyType},
	 * or null if no default policy for given {@link IdmPasswordPolicyType} found.
	 * 
	 */
	IdmPasswordPolicyDto getDefaultPasswordPolicy(IdmPasswordPolicyType type);

	/**
	 * Generate password by given password policy
	 * 
	 * @param passwordPolicy
	 * @return
	 */
	String generatePassword(IdmPasswordPolicyDto passwordPolicy);
	
	/**
	 * Return instance of password generator, @see {@link PasswordGenerator}
	 * 
	 * @return
	 */
	PasswordGenerator getPasswordGenerator();
	
	/**
	 * Generate password by default password policy with type {@link IdmPasswordPolicyType.GENERATE},
	 * if this type doesn't exist use default password policy with type {@link IdmPasswordPolicyType.VALIDATE}
	 * 
	 * @return new password
	 */
	String generatePasswordByDefault();
	
	/**
	 * Return max password age through list of password policies
	 * 
	 * @param policyList
	 * @return
	 */
	Integer getMaxPasswordAge(List<IdmPasswordPolicyDto> policyList);
	
	/**
	 * Find one password policy by name
	 * 
	 * @param name
	 * @return
	 */
	IdmPasswordPolicyDto findOneByName(String name);

	/**
	 * Method pre-validate default password policy (non-existing password fails all enabled password policy rules)
	 * Method throw exception with all validation errors.
	 * 
	 */
	void preValidate(IdmPasswordValidationDto passwordValidationDto);
	
	/**
	 * Method pre-validate password policies (non-existing password fails all enabled password policy rules)
	 * Method throw exception with all validation errors.
	 * 
	 * @param passwordValidationDto
	 * @param passwordPolicyList
	 */
	void preValidate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicyDto> passwordPolicyList);
}
