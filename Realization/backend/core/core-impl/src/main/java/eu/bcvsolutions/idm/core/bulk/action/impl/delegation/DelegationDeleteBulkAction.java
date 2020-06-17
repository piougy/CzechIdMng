package eu.bcvsolutions.idm.core.bulk.action.impl.delegation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete delegation.
 *
 * @author Vít Švanda
 */
@Component(DelegationDeleteBulkAction.NAME)
@Description("Delete delegations.")
public class DelegationDeleteBulkAction extends AbstractRemoveBulkAction<IdmDelegationDto, IdmDelegationFilter> {

	public static final String NAME = "core-delegation-delete-bulk-action";
	//
	@Autowired
	private IdmDelegationService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.DELEGATIONDEFINITION_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmDelegationDto, IdmDelegationFilter> getService() {
		return service;
	}
}
