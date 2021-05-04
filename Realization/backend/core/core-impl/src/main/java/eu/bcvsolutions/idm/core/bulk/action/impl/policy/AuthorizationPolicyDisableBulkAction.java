package eu.bcvsolutions.idm.core.bulk.action.impl.policy;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmAuthorizationPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmAuthorizationPolicyFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmAuthorizationPolicyService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;

/**
 * Disable authorization policy.
 * 
 * @author Radek Tomiška
 * @since 11.1.0
 */
@Component(AuthorizationPolicyDisableBulkAction.NAME)
@Description("Disable authorization policy.")
public class AuthorizationPolicyDisableBulkAction extends AbstractBulkAction<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> {

	public static final String NAME = "core-authorization-policy-disable-bulk-action";
	//
	@Autowired private IdmAuthorizationPolicyService service;
	
	@Override
	public String getName() {
		return AuthorizationPolicyDisableBulkAction.NAME;
	}

	@Override
	protected OperationResult processDto(IdmAuthorizationPolicyDto policy) {
		if (!policy.isDisabled()) {
			policy.setDisabled(true);
			service.save(policy);
		}
		//
		return new OperationResult.Builder(OperationState.EXECUTED).build();
	}
	
	@Override
	public ReadWriteDtoService<IdmAuthorizationPolicyDto, IdmAuthorizationPolicyFilter> getService() {
		return service;
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		authorities.add(CoreGroupPermission.AUTHORIZATIONPOLICY_UPDATE);
		//
		return authorities;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 300;
	}
	
	@Override
	protected boolean requireNewTransaction() {
		return true;
	}
}
