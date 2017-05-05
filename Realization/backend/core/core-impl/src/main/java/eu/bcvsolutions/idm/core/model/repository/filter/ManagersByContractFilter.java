package eu.bcvsolutions.idm.core.model.repository.filter;

import eu.bcvsolutions.idm.core.api.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Managers criteria builder.
 * 
 * Override in custom module for changing managers evaluation.
 * 
 * @author Radek Tomiška
 *
 */
public interface ManagersByContractFilter extends FilterBuilder<IdmIdentity, IdentityFilter> {
	
	@Override
	default String getName() {
		return IdentityFilter.PARAMETER_MANAGERS_BY_CONTRACT;
	}
}
