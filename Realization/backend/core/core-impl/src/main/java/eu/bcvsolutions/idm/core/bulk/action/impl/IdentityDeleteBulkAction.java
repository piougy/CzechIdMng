package eu.bcvsolutions.idm.core.bulk.action.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;

/**
 * Delete given identities
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

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
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
