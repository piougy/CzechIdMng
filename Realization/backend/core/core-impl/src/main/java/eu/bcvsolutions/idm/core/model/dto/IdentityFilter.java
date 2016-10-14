package eu.bcvsolutions.idm.core.model.dto;

import eu.bcvsolutions.idm.core.api.dto.QuickFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Filter for identities
 * 
 * @author Radek Tomiška
 *
 */
public class IdentityFilter extends QuickFilter {
	/**
	 * Subordinates for given identity
	 */
	private IdmIdentity subordinatesFor;

	public IdmIdentity getSubordinatesFor() {
		return subordinatesFor;
	}

	public void setSubordinatesFor(IdmIdentity subordinatesFor) {
		this.subordinatesFor = subordinatesFor;
	}
}
