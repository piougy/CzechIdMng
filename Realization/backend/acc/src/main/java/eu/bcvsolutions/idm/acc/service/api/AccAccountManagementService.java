package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

public interface AccAccountManagementService {

	/**
	 * Create or delete accounts for this identity according their roles
	 * @param identity
	 * @return
	 */
	boolean resolveIdentityAccounts(IdmIdentity identity);

	/**
	 * Identity role is deleting, we have to delete linked identity accounts
	 * @param entity
	 */
	void deleteIdentityAccount(IdmIdentityRoleDto entity);
	
	/**
	 * Return UID for this entity and roleSystem. First will be find and use
	 * transform script from roleSystem attribute. If isn't UID attribute for
	 * roleSystem defined, then will be use default UID attribute handling.
	 * 
	 * @param identity
	 * @param roleSystem
	 * @return
	 */
	String generateUID(AbstractEntity entity, SysRoleSystemDto roleSystem);
}