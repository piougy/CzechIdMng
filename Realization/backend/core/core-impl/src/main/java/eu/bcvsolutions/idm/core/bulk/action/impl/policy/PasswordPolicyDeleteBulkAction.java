package eu.bcvsolutions.idm.core.bulk.action.impl.policy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordPolicyFilter;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete selected password policies.
 *
 * @author Radek Tomiška
 * @since 11.0.0
 */
@Component(PasswordPolicyDeleteBulkAction.NAME)
@Description("Delete selected password policies.")
public class PasswordPolicyDeleteBulkAction extends AbstractRemoveBulkAction<IdmPasswordPolicyDto, IdmPasswordPolicyFilter> {

	public static final String NAME = "core-password-policy-delete-bulk-action";

	@Autowired private IdmPasswordPolicyService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.PASSWORDPOLICY_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmPasswordPolicyDto, IdmPasswordPolicyFilter> getService() {
		return service;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
