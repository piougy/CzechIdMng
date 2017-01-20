package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityPasswordFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityPassword;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

public interface IdmIdentityPasswordService  extends ReadWriteEntityService<IdmIdentityPassword, IdentityPasswordFilter> {
	
	/**
	 * Save password to identity. This method not validate password.
	 * 
	 * @param identity
	 * @param entity
	 * @return
	 */
	public IdmIdentityPassword save(IdmIdentity identity, PasswordChangeDto passwordDto);
	
	/**
	 * Delete password by given identity
	 * 
	 * @param identity
	 */
	public void delete(IdmIdentity identity);
	
	/**
	 * Return password for given identity
	 * 
	 * @param identity
	 * @return
	 */
	public IdmIdentityPassword get(IdmIdentity identity);
	
	/**
	 * Check password matches a passwordToCheck
	 * 
	 * @param passwordToCheck
	 * @param password
	 * @return true if matches
	 */
	public boolean checkPassword(GuardedString passwordToCheck, IdmIdentityPassword password);
	
	/**
	 * Method generate password and return hash
	 * 
	 * @param password
	 * @return
	 */
	public byte[] generateHash(GuardedString password, byte[] salt);
	
	/**
	 * Get salt for idenitity
	 * 
	 * @param identity
	 * @return
	 */
	public byte[] getSalt(IdmIdentity identity);
}
