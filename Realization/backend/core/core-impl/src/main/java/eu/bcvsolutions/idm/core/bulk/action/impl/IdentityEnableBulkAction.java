package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Bulk action for enable given identities
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component("identityEnableBulkAction")
@Description("Enable given identities.")
public class IdentityEnableBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "identity-enable-bulk-action";
	
	@Autowired
	private IdmIdentityService identityService;
	
	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		dto = identityService.enable(dto.getId());
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getPermissionForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_UPDATE);
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 200;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
