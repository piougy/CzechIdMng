package eu.bcvsolutions.idm.core.model.repository.filter;

import eu.bcvsolutions.idm.core.api.repository.filter.FilterBuilder;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Subordinates criteria builder.
 * 
 * Override in custom module for changing subordinates evaluation.
 * 
 * @author Radek Tomiška
 *
 */
public interface SubordinatesFilter extends FilterBuilder<IdmIdentity, IdentityFilter> {
	
	@Override
	default String getPropertyName() {
		return IdentityFilter.PARAMETER_SUBORDINATES_FOR;
	}
	
}
