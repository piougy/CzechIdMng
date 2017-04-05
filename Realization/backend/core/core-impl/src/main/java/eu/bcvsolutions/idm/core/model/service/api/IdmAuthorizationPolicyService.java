package eu.bcvsolutions.idm.core.model.service.api;

import java.util.List;

import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.model.dto.filter.AuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmAuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.service.AuthorizableService;

/**
 * Assign authorization evaluator to role.
 * 
 * @author Radek Tomiška
 *
 */
public interface IdmAuthorizationPolicyService
		extends ReadWriteDtoService<IdmAuthorizationPolicyDto, IdmAuthorizationPolicy, AuthorizationPolicyFilter>,
		AuthorizableService<IdmAuthorizationPolicyDto, AuthorizationPolicyFilter> {

	/**
	 * Returns all enabled policies for given identity and entity type
	 * 
	 * @param username
	 *            identity's username
	 * @param entityType
	 * @return
	 */
	List<IdmAuthorizationPolicyDto> getEnabledPolicies(String username, Class<? extends Identifiable> entityType);
}
