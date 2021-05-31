package eu.bcvsolutions.idm.core.bulk.action.impl.rolerequest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete given role requests.
 * 
 * @author Ondrej Husnik
 * 
 * @since 11.1.0
 *
 */
@Component(RoleRequestDeleteBulkAction.NAME)
@Description("Delete given roles.")
public class RoleRequestDeleteBulkAction extends AbstractRemoveBulkAction<IdmRoleRequestDto, IdmRoleRequestFilter> {

	public static final String NAME = "role-request-delete-bulk-action";
	
	@Autowired
	private IdmRoleRequestService roleRequestService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.ROLE_REQUEST_DELETE);
	}
	
	@Override
	public ReadWriteDtoService<IdmRoleRequestDto, IdmRoleRequestFilter> getService() {
		return roleRequestService;
	}
}
