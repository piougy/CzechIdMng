package eu.bcvsolutions.idm.core.model.service;

import org.activiti.engine.runtime.ProcessInstance;

import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Operations with IdmIdentity
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmIdentityService extends ReadWriteEntityService<IdmIdentity> {
	
	/**
	 * Returns identity by given username
	 * @param username
	 * @return
	 */
	IdmIdentity getByUsername(String username);

	/**
	 * Better "toString"
	 * 
	 * @param identity
	 * @return
	 */
	String getNiceLabel(IdmIdentity identity);

	/**
	 * Start workflow for change permissions
	 * @param identity
	 * @return
	 */
	ProcessInstance changePermissions(IdmIdentity identity);
	
	/**
	 * Changes given identity's password
	 * 
	 * @param identity
	 * @param passwordChangeDto
	 */
	void passwordChange(IdmIdentity identity, PasswordChangeDto passwordChangeDto);
}
