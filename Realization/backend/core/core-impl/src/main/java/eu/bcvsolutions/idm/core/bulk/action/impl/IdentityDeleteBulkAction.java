package eu.bcvsolutions.idm.core.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.config.domain.IdentityConfiguration;
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

@Component("identityDeleteBulkAction")
@Description("Delete given identities.")
@Enabled(module = CoreModuleDescriptor.MODULE_ID, property = IdentityConfiguration.PROPERTY_IDENTITY_DELETE)
public class IdentityDeleteBulkAction extends AbstractRemoveBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "identity-delete-bulk-action";

	@Autowired
	private IdmIdentityService identityService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
