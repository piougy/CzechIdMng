package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Delete given identities
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = "idm.pub.core.identity.delete")
@Component("identityDeleteBulkAction")
@Description("Delete given identities.")
public class IdentityDeleteBulkAction extends AbstractRemoveBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "identity-delete-bulk-action";

	@Autowired
	private IdmIdentityService identityService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getOrder() {
		return super.getOrder() + 300;
	}

	@Override
	protected List<String> getPermissionForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
