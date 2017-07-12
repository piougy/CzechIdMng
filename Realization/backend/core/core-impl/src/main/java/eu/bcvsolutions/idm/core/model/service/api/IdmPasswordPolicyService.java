package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordValidationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

/**
 * Service for valdiate password by password policy, also generate password.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
public interface IdmPasswordPolicyService extends ReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> {
	
	/**
	 * Method validate password by password policy,
	 * {@link validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicy> passwordPolicyList)}}.
	 * Method throw exception with all errors after complete validate if any validation problem.
	 * 
	 * @param passwordValidationDto
	 * @param passwordPolicy
	 */
	void validate(IdmPasswordValidationDto passwordValidationDto, IdmPasswordPolicy passwordPolicy);
	
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
	void validate(IdmPasswordValidationDto passwordValidationDto, List<IdmPasswordPolicy> passwordPolicyList);
	
	/**
	 * Method return default password policy, by given type, @see {@link IdmPasswordPolicyType}
	 * 
	 * @return
	 */
	IdmPasswordPolicy getDefaultPasswordPolicy(IdmPasswordPolicyType type);

	
	/**
	 * Generate password by given password policy
	 * 
	 * @param passwordPolicy
	 * @return
	 */
	String generatePassword(IdmPasswordPolicy passwordPolicy);
	
	/**
	 * Return instance of password generator, @see {@link PasswordGenerator}
	 * 
	 * @return
	 */
	PasswordGenerator getPasswordGenerator();
	
	/**
	 * Generate password by default password policy with type {@link IdmPasswordPolicyType.GENERATE},
	 * if this type dont exist use default password policy with type {@link IdmPasswordPolicyType.VALIDATE}
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
	Integer getMaxPasswordAge(List<IdmPasswordPolicy> policyList);
	
	/**
	 * Find one password policy by name
	 * 
	 * @param name
	 * @return
	 */
	IdmPasswordPolicy findOneByName(String name);
}
