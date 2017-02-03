package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.dto.filter.PasswordFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Service for working with password.
 * Now is password connect only to entity IdmIdentity.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public interface IdmPasswordService  extends ReadWriteEntityService<IdmPassword, PasswordFilter> {
	
	/**
	 * Save password to identity. This method not validate password.
	 * 
	 * @param identity
	 * @param entity
	 * @return
	 */
	IdmPassword save(IdmIdentity identity, PasswordChangeDto passwordDto);
	
	/**
	 * Delete password by given identity
	 * 
	 * @param identity
	 */
	void delete(IdmIdentity identity);
	
	/**
	 * Return password for given identity
	 * 
	 * @param identity
	 * @return
	 */
	IdmPassword get(IdmIdentity identity);
	
	/**
	 * Check password matches a passwordToCheck
	 * 
	 * @param passwordToCheck
	 * @param password
	 * @return true if matches
	 */
	boolean checkPassword(GuardedString passwordToCheck, IdmPassword password);
	
	/**
	 * Method generate password and return hash
	 * 
	 * @param password
	 * @return
	 */
	String generateHash(GuardedString password, String salt);
	
	/**
	 * Get salt for identity
	 * 
	 * @param identity
	 * @return
	 */
	String getSalt(IdmIdentity identity);
}
