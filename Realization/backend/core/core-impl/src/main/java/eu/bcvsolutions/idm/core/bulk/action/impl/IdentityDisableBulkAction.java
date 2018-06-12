package eu.bcvsolutions.idm.core.bulk.action.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Disable given identities
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component("identityDisableBulkAction")
@Description("Disable given identities.")
public class IdentityDisableBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "identity-disable-bulk-action";
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		dto = identityService.disable(dto.getId());
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected BasePermission[] getPermissionForEntity() {
		BasePermission[] permissions= {
				IdmBasePermission.UPDATE
		};
		return permissions;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 100;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
