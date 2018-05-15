package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccTreeAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Tree provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value=TreeProvisioningExecutor.NAME)
public class TreeProvisioningExecutor extends AbstractProvisioningExecutor<IdmTreeNodeDto> {
 
	public static final String NAME = "treeProvisioningService";
	private final AccTreeAccountService treeAccountService;
	private final IdmTreeNodeService treeNodeService;
	
	@Autowired
	public TreeProvisioningExecutor(
			SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, 
			IcConnectorFacade connectorFacade,
			SysSystemService systemService, 
			SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, 
			SysSystemEntityService systemEntityService,
			AccAccountService accountService, 
			AccTreeAccountService treeAccountService,
			ProvisioningExecutor provisioningExecutor, 
			EntityEventManager entityEventManager, 
			SysSchemaAttributeService schemaAttributeService,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			IdmRoleService roleService,
			IdmTreeNodeService treeNodeService) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager, schemaAttributeService,
				schemaObjectClassService, systemAttributeMappingService,
				roleService);
		//
		Assert.notNull(treeAccountService);
		Assert.notNull(treeNodeService);
		//
		this.treeAccountService = treeAccountService;
		this.treeNodeService = treeNodeService;
	}
	
	public void doProvisioning(AccAccountDto account) {
		Assert.notNull(account);

		AccTreeAccountFilter filter = new AccTreeAccountFilter();
		filter.setAccountId(account.getId());
		List<AccTreeAccountDto> treeAccoutnList = treeAccountService.find(filter, null).getContent();
		if (treeAccoutnList == null) {
			return;
		}
		treeAccoutnList.stream().filter(treeAccount -> {
			return treeAccount.isOwnership();
		}).forEach((treeAccount) -> {
			doProvisioning(account, DtoUtils.getEmbedded(treeAccount, AccTreeAccount_.treeNode));
		});
	}
	
	@Override
	protected Object getAttributeValue(String uid, IdmTreeNodeDto entity, AttributeMapping attribute) {
		Object idmValue = super.getAttributeValue(uid, entity, attribute);

		if (attribute.isEntityAttribute()
				&& TreeSynchronizationExecutor.PARENT_FIELD.equals(attribute.getIdmPropertyName())) {
			// For Tree we need do transform parent (IdmTreeNode) to resource
			// parent format (UID of parent)
			if (idmValue instanceof UUID) {
				// Generally we expect IdmTreeNode as parent (we will do
				// transform)
				AccTreeAccountFilter treeAccountFilter = new AccTreeAccountFilter();
				treeAccountFilter.setSystemId(this.getSytemFromSchemaAttribute(attribute.getSchemaAttribute()).getId());
				treeAccountFilter.setTreeNodeId(((UUID) idmValue));
				List<AccTreeAccountDto> treeAccounts = treeAccountService.find(treeAccountFilter, null).getContent();
				if (treeAccounts.isEmpty()) {
					throw new ProvisioningException(AccResultCode.PROVISIONING_TREE_PARENT_ACCOUNT_NOT_FOUND,
							ImmutableMap.of("parentNode", idmValue));
				}
				if (treeAccounts.size() != 1) {
					throw new ProvisioningException(AccResultCode.PROVISIONING_TREE_TOO_MANY_PARENT_ACCOUNTS,
							ImmutableMap.of("parentNode", idmValue));
				}
				AccTreeAccountDto treeAccount = treeAccounts.get(0);
				String parentUid = accountService.get(treeAccount.getAccount()).getUid();
				return parentUid;
			} else {
				// If is parent not instance of IdmTreeNode, then we set value
				// without any transform
				return idmValue;
			}
		}
		return idmValue;
	}
	
	@Override
	protected List<SysRoleSystemAttributeDto> findOverloadingAttributes(IdmTreeNodeDto entity, SysSystemDto system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType) {
		// Overloading attributes is not implemented for TreeNode
		return new ArrayList<>();
	}
	
	@Override
	protected List<SysSystemMappingDto> findSystemMappingsForEntityType(IdmTreeNodeDto entity, SystemEntityType entityType) {
		SysSystemMappingFilter mappingFilter = new SysSystemMappingFilter();
		mappingFilter.setEntityType(entityType);
		mappingFilter.setTreeTypeId(entity.getTreeType());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		List<SysSystemMappingDto> systemMappings = systemMappingService.find(mappingFilter, null).getContent();
		return systemMappings;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected EntityAccountFilter createEntityAccountFilter() {
		return new AccTreeAccountFilter();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected AccTreeAccountService getEntityAccountService() {
		return treeAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccTreeAccountDto();
	}

	@Override
	protected IdmTreeNodeService getService() {
		return treeNodeService;
	}
}
