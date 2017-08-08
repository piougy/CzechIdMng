package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.domain.RoleType;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Role provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value=RoleProvisioningExecutor.NAME)
public class RoleProvisioningExecutor extends AbstractProvisioningExecutor<IdmRole> {
 
	public static final String NAME = "roleProvisioningService";
	private final AccRoleAccountService roleAccountService;
	private final IdmRoleService roleService;
	
	@Autowired
	public RoleProvisioningExecutor(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccRoleAccountService roleAccountService,
			ProvisioningExecutor provisioningExecutor, IdmRoleService roleService,
			EntityEventManager entityEventManager) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				accountManagementService, roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager);
		
		Assert.notNull(roleAccountService);
		Assert.notNull(roleService);
		
		this.roleAccountService = roleAccountService;
		this.roleService = roleService;
	}
	
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		RoleAccountFilter filter = new RoleAccountFilter();
		filter.setAccountId(account.getId());
		List<? extends EntityAccountDto> entityAccoutnList = roleAccountService.find(filter, null).getContent();
		if (entityAccoutnList == null) {
			return;
		}
		entityAccoutnList.stream().filter(entityAccount -> {
			return entityAccount.isOwnership();
		}).forEach((treeAccount) -> {
			doProvisioning(account, (IdmRole) roleService.get(treeAccount.getEntity()));
		});
	}
	
	@Override
	protected List<SysRoleSystemAttribute> findOverloadingAttributes(IdmRole entity, SysSystem system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType) {
		// Overloading attributes is not implemented for RoleNode
		return new ArrayList<>();
	}
	
	@Override
	protected Object getAttributeValue(String uid, IdmRole entity, AttributeMapping attribute) {
		Object idmValue = super.getAttributeValue(uid, entity, attribute);

		if (attribute.isEntityAttribute()
				&& RoleSynchronizationExecutor.ROLE_TYPE_FIELD.equals(attribute.getIdmPropertyName())) {
			// Role type enumeration we will do transform to String (name)
			if (idmValue instanceof RoleType) {
				return ((RoleType)idmValue).name();
			}
		}
		return idmValue;
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new RoleAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return roleAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccRoleAccountDto();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityService() {
		return null; // We don't have DTO service for IdmRole now
	}

	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.ROLE == delimiter;
	}
}
