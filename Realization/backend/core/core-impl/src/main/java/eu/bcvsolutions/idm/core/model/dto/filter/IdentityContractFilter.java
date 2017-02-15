package eu.bcvsolutions.idm.core.model.dto.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;

/**
 * Filter for {@link IdmIdentityContract} entities.
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityContractFilter extends QuickFilter {

	private IdmIdentity identity;
	
	public IdmIdentity getIdentity() {
		return identity;
	}
	
	public void setIdentity(IdmIdentity identity) {
		this.identity = identity;
	}
}
