package eu.bcvsolutions.idm.core.bulk.action.impl.identity;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.IdentityState;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.domain.IdmBasePermission;

/**
 * Recalculate identity state (according to its current contracts) - just evaluate its state and if it is disabled.
 * Feature helps administrators in special cases, when HR processes didn't process everything correctly, 
 * or there were some other reasons why the state of identity was inconsistent with the state of its contracts.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
@Enabled(CoreModuleDescriptor.MODULE_ID)
@Component(IdentityEvaluateStateBulkAction.NAME)
@Description("Recalculate identity state (according to its current contracts) - just evaluate its state and if it is disabled.")
public class IdentityEvaluateStateBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "core-identity-evaluate-state-bulk-action";
	//
	@Autowired private IdmIdentityService identityService;
	
	@Override
	public String getName() {
		return IdentityEvaluateStateBulkAction.NAME;
	}

	@Override
	protected OperationResult processDto(IdmIdentityDto identity) {
		IdentityState state = identityService.evaluateState(identity.getId());
		if (identity.getState() != state) {
			identity.setState(state);
			identityService.save(identity, IdmBasePermission.UPDATE); // permission evaluated before, but just for be sure ...
		}
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		authorities.add(CoreGroupPermission.IDENTITY_UPDATE);
		//
		return authorities;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 3000;
	}
}
