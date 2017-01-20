package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

public interface IdmPasswordService  extends ReadWriteEntityService<IdmPassword, PasswordFilter> {
	
	/**
	 * Save password to identity. This method not validate password.
	 * 
	 * @param identity
	 * @param entity
	 * @return
	 */
	public IdmPassword save(IdmIdentity identity, PasswordChangeDto passwordDto);
	
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
	public IdmPassword get(IdmIdentity identity);
	
	/**
	 * Check password matches a passwordToCheck
	 * 
	 * @param passwordToCheck
	 * @param password
	 * @return true if matches
	 */
	public boolean checkPassword(GuardedString passwordToCheck, IdmPassword password);
	
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
