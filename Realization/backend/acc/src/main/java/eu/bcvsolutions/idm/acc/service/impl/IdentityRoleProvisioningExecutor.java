package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccIdentityRoleAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityRoleAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccIdentityRoleAccount_;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccRoleAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for execute provisioning for IdentityRole
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value = IdentityRoleProvisioningExecutor.NAME)
public class IdentityRoleProvisioningExecutor extends AbstractProvisioningExecutor<IdmIdentityRoleDto> {
 
	public static final String NAME = "identityRoleProvisioningService";
	@Autowired
	private AccIdentityRoleAccountService identityRoleAccountService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public IdentityRoleProvisioningExecutor(
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, 
			IcConnectorFacade connectorFacade,
			SysSystemService systemService, 
			SysRoleSystemService identityRoleSystemService,
			SysRoleSystemAttributeService identityRoleSystemAttributeService, 
			SysSystemEntityService systemEntityService,
			AccAccountService accountService, 
			AccRoleAccountService identityRoleAccountService,
			ProvisioningExecutor provisioningExecutor, 
			EntityEventManager entityEventManager, 
			SysSchemaAttributeService schemaAttributeService,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			IdmRoleService identityRoleService) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, identityRoleSystemService,
				identityRoleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager, schemaAttributeService, schemaObjectClassService,
				systemAttributeMappingService, identityRoleService);
		//
	}
	
	public void doProvisioning(AccAccountDto account) {
		Assert.notNull(account, "Account is required.");

		AccIdentityRoleAccountFilter filter = new AccIdentityRoleAccountFilter();
		filter.setAccountId(account.getId());
		List<AccIdentityRoleAccountDto> entityAccoutnList = identityRoleAccountService.find(filter, null).getContent();
		if (entityAccoutnList == null) {
			return;
		}
		entityAccoutnList.stream().filter(entityAccount -> {
			return entityAccount.isOwnership();
		}).forEach((identityRoleAccount) -> {
			doProvisioning(account, DtoUtils.getEmbedded(identityRoleAccount, AccIdentityRoleAccount_.identityRole, IdmIdentityRoleDto.class));
		});
	}
	
	@Override
	protected List<SysRoleSystemAttributeDto> findOverloadingAttributes(IdmIdentityRoleDto dto, SysSystemDto system,
			AccAccountDto account, SystemEntityType entityType) {
		// Overrider of identity-role attributes are not supported
		return Lists.newArrayList();
	}
	

	@Override
	@SuppressWarnings("unchecked")
	protected AccIdentityRoleAccountFilter createEntityAccountFilter() {
		return new AccIdentityRoleAccountFilter();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AccIdentityRoleAccountService getEntityAccountService() {
		return identityRoleAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccIdentityRoleAccountDto();
	}

	@Override
	protected IdmIdentityRoleService getService() {
		return identityRoleService;
	}
}
