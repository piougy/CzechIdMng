package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

@Service
public interface IdmPasswordPolicyService extends ReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> {
	
	/**
	 * Method validate password by password policy
	 * 
	 * @param password
	 * @param passwordPolicy
	 * @return true if password is valid or throw exception
	 */
	public boolean validate(String password, IdmPasswordPolicy passwordPolicy);
	
	/**
	 * Validate password by default validation password policy.
	 * 
	 * @param password
	 * @return true if password is valid or throw exception
	 */
	public boolean validate(String password);
	
	/**
	 * Validate password by list of password policies. Validate trought all polocies,
	 * if found some error throw exception.
	 * 
	 * @param password
	 * @param passwordPolicyList
	 * @return true if password is valid or throw exception
	 */
	public boolean validate(String password, List<IdmPasswordPolicy> passwordPolicyList);
	
	/**
	 * Method return default password policy, by given type, @see {@link IdmPasswordPolicyType}
	 * 
	 * @return
	 */
	public IdmPasswordPolicy getDefaultPasswordPolicy(IdmPasswordPolicyType type);

	
	/**
	 * Generate password by given password policy
	 * 
	 * @param passwordPolicy
	 * @return
	 */
	public String generatePassword(IdmPasswordPolicy passwordPolicy);
	
	/**
	 * Return instance of password generator, @see {@link PasswordGenerator}
	 * 
	 * @return
	 */
	public PasswordGenerator getPasswordGenerator();
	
	/**
	 * Generate password by default password policy with type {@link IdmPasswordPolicyType.GENERATE},
	 * if this type dont exist use default password policy with type {@link IdmPasswordPolicyType.VALIDATE}
	 * 
	 * @return new password
	 */
	public String generatePasswordByDefault();
}
