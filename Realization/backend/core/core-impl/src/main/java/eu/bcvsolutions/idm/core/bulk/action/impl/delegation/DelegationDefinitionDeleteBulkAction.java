package eu.bcvsolutions.idm.core.bulk.action.impl.delegation;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDefinitionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationDefinitionFilter;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationDefinitionService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete delegation definiton.
 *
 * @author Vít Švanda
 */
@Component(DelegationDefinitionDeleteBulkAction.NAME)
@Description("Delete delegation definitons.")
public class DelegationDefinitionDeleteBulkAction extends AbstractRemoveBulkAction<IdmDelegationDefinitionDto, IdmDelegationDefinitionFilter> {

	public static final String NAME = "core-delegation-definition-delete-bulk-action";
	//
	@Autowired
	private IdmDelegationDefinitionService service;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.DELEGATIONDEFINITION_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmDelegationDefinitionDto, IdmDelegationDefinitionFilter> getService() {
		return service;
	}
}
