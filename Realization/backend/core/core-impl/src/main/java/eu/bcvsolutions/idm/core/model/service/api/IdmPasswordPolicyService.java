package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.PasswordGenerator;
import eu.bcvsolutions.idm.core.model.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.core.model.entity.IdmPasswordPolicy;

public interface IdmPasswordPolicyService extends ReadWriteEntityService<IdmPasswordPolicy, PasswordPolicyFilter> {
	
	/**
	 * Method validate password by password policy,
	 * {@link validate(String password, List<IdmPasswordPolicy> passwordPolicyList, IdmIdentityPassword oldPassword)}}.
	 * 
	 * @param password
	 * @param passwordPolicy
	 * @param oldPassword
	 * @return true if password is valid or throw exception
	 */
	public boolean validate(String password, IdmPasswordPolicy passwordPolicy, IdmPassword oldPassword);
	
	/**
	 * Validate password by given password policy 
	 * 
	 * @param password
	 * @param passwordPolicy
	 * @return
	 */
	public boolean validate(String password, IdmPasswordPolicy passwordPolicy);
	
	/**
	 * Validate password by default password policy
	 * 
	 * @param password
	 * @return
	 */
	public boolean validate(String password);
	
	/**
	 * Validate password by default validation password policy.
	 * 
	 * @param password
	 * @param oldPassword
	 * @return true if password is valid or throw exception
	 */
	public boolean validate(String password, IdmPassword oldPassword);
	
	/**
	 * Validate password by list of password policies. Validate trought all polocies,
	 * if found some error throw exception.
	 * When isn't @param oldPassword null, validate for password age trought policies
	 * minimal age
	 * 
	 * @param password
	 * @param passwordPolicyList
	 * @param oldPassword
	 * @return true if password is valid or throw exception
	 */
	public boolean validate(String password, List<IdmPasswordPolicy> passwordPolicyList, IdmPassword oldPassword);
	
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
	
	/**
	 * Return max password age through list of password policies
	 * 
	 * @param policyList
	 * @return
	 */
	public Integer getMaxPasswordAge(List<IdmPasswordPolicy> policyList);
}
