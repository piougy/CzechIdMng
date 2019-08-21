package eu.bcvsolutions.idm.core.bulk.action.impl.contract;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractRemoveBulkAction;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Delete contracted positions.
 * 
 * @author Radek Tomiška
 * @since 9.7.3
 */
@Component(IdentityContractDeleteBulkAction.NAME)
@Description("Delete contracted positions.")
public class IdentityContractDeleteBulkAction extends AbstractRemoveBulkAction<IdmIdentityContractDto, IdmIdentityContractFilter> {

	public static final String NAME = "core-identity-contract-delete-bulk-action";
	//
	@Autowired private IdmIdentityContractService service;
	
	@Override
	public String getName() {
		return NAME;
	}

	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITYCONTRACT_DELETE);
	}

	@Override
	public ReadWriteDtoService<IdmIdentityContractDto, IdmIdentityContractFilter> getService() {
		return service;
	}
}
