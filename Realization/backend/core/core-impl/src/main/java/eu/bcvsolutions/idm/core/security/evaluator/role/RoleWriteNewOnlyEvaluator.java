package eu.bcvsolutions.idm.core.security.evaluator.role;

import java.util.Set;

import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.security.api.domain.AuthorizationPolicy;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;
import eu.bcvsolutions.idm.core.security.evaluator.AbstractAuthorizationEvaluator;

/**
 * Adds permission for create new role
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Adds permission for create new role")
public class RoleWriteNewOnlyEvaluator extends AbstractAuthorizationEvaluator<IdmRole> {	
	
	@Override
	public Set<String> getPermissions(IdmRole entity, AuthorizationPolicy policy) {
		Set<String> permissions = super.getPermissions(entity, policy);	
		permissions.add(IdmBasePermission.CREATE.getName());
		return permissions;
	}
}
