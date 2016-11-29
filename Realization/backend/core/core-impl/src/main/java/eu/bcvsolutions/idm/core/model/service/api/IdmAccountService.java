package eu.bcvsolutions.idm.core.model.service.api;

import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

public interface IdmAccountService {

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
	void deleteIdentityAccount(IdmIdentityRole entity);
}