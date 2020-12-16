package eu.bcvsolutions.idm.core.bulk.action.impl.policy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete selected authorization policies.
 *
 * @author Radek Tomiška
 *
 */
@Component(AuthorizationPolicyDeleteBulkAction.NAME)
@Description("Delete selected authorization policies.")
public class AuthorizationPolicyDeleteBulkAction extends AbstractRemoveBulkAction<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> {

	public static final String NAME = "core-authorization-policy-delete-bulk-action";

	@Autowired private IdmAuthorizationPolicyService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.AUTHORIZATIONPOLICY_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
