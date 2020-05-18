package eu.bcvsolutions.idm.acc.bulk.action.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.domain.AccGroupPermission;
import eu.bcvsolutions.idm.core.api.bulk.action.AbstractBulkAction;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ForbiddenEntityException;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * Executes provisioning (publish notify event on identity)
 * 
 * @author Radek Tomiška
 */
@Enabled(AccModuleDescriptor.MODULE_ID)
@Component(IdentityProvisioningBulkAction.NAME)
@Description("Executes provisioning (publish notify event on identity)")
public class IdentityProvisioningBulkAction extends AbstractBulkAction<IdmIdentityDto, IdmIdentityFilter> {

	public static final String NAME = "acc-identity-provisioning-bulk-action";
	//
	@Autowired private IdmIdentityService identityService;
	@Autowired private SecurityService securityService;
	@Autowired private EntityEventManager entityEventManager;
	
	@Override
	protected OperationResult processDto(IdmIdentityDto dto) {
		if (!securityService.hasAnyAuthority(AccGroupPermission.SYSTEM_ADMIN)) {
			throw new ForbiddenEntityException((BaseDto)dto, AccGroupPermission.SYSTEM);
		}
		// Provisioning will be executed asynchronously by notify event
		entityEventManager.changedEntity(dto);
		//
		return new OperationResult(OperationState.EXECUTED);
	}
	
	@Override
	public List<String> getAuthorities() {
		List<String> authorities = super.getAuthorities();
		authorities.add(AccGroupPermission.SYSTEM_ADMIN);
		return authorities;
	}
	
	@Override
	protected List<String> getAuthoritiesForEntity() {
		return Lists.newArrayList(CoreGroupPermission.IDENTITY_READ, CoreGroupPermission.IDENTITY_UPDATE);
	}

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override
	public int getOrder() {
		return super.getOrder() + 1600;
	}

	@Override
	public ReadWriteDtoService<IdmIdentityDto, IdmIdentityFilter> getService() {
		return identityService;
	}
}
