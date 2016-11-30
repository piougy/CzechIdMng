package eu.bcvsolutions.idm.acc.service.api;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityAccount;
import eu.bcvsolutions.idm.core.api.service.ReadWriteEntityService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Identity accounts on target system
 * 
 * @author Radek Tomiška
 *
 */
public interface AccIdentityAccountService extends ReadWriteEntityService<AccIdentityAccount, IdentityAccountFilter> {
	
}
	