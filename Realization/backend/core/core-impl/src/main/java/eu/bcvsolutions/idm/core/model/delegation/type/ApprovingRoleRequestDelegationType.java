package eu.bcvsolutions.idm.core.model.delegation.type;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractDelegationType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.workflow.model.dto.WorkflowTaskInstanceDto;
import org.springframework.stereotype.Component;

/**
 *
 * Delegation type for workflow tasks approving request with identity-roles.
 *
 * @disabled This delegation type is disabled by default!
 * @author Vít Švanda
 */
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = ApprovingRoleRequestDelegationType.PROPERTY_DELEGATION_ROLE_REQUEST_TYPE)
@Component(ApprovingRoleRequestDelegationType.NAME)
public class ApprovingRoleRequestDelegationType extends AbstractDelegationType {

	public static final String NAME = "approving-role-request-delegation-type";
	/**
	 * By default is this delegation type disabled.
	 */
	public static final String PROPERTY_DELEGATION_ROLE_REQUEST_TYPE
			= ConfigurationService.IDM_PUBLIC_PROPERTY_PREFIX + "core.delegation.type." + NAME;

	@Override
	public Class<? extends BaseDto> getOwnerType() {
		return WorkflowTaskInstanceDto.class;
	}

	@Override
	public boolean isSupportsDelegatorContract() {
		return false;
	}

	@Override
	public int getOrder() {
		return 20;
	}
}
