package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.EntityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
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
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.ReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmRoleService;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do Tree provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value=TreeProvisioningExecutor.NAME)
public class TreeProvisioningExecutor extends AbstractProvisioningExecutor<IdmTreeNode> {
 
	public static final String NAME = "treeProvisioningService";
	private final AccTreeAccountService treeAccountService;
	private final IdmTreeNodeRepository treeNodeRepository;
	
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
			IdmTreeNodeRepository treeNodeRepository,
			EntityEventManager entityEventManager, 
			SysSchemaAttributeService schemaAttributeService,
			SysSchemaObjectClassService schemaObjectClassService,
			SysSystemAttributeMappingService systemAttributeMappingService,
			IdmRoleService roleService) {
		
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				accountManagementService, roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor, entityEventManager, schemaAttributeService,
				schemaObjectClassService, systemAttributeMappingService,
				roleService);
		Assert.notNull(treeAccountService);
		Assert.notNull(treeNodeRepository);
		//
		this.treeAccountService = treeAccountService;
		this.treeNodeRepository = treeNodeRepository;
	}
	
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setAccountId(account.getId());
		List<? extends EntityAccountDto> treeAccoutnList = treeAccountService.find(filter, null).getContent();
		if (treeAccoutnList == null) {
			return;
		}
		treeAccoutnList.stream().filter(treeAccount -> {
			return treeAccount.isOwnership();
		}).forEach((treeAccount) -> {
			doProvisioning(account, treeNodeRepository.findOne(treeAccount.getEntity()));
		});
	}
	
	@Override
	protected Object getAttributeValue(String uid, IdmTreeNode entity, AttributeMapping attribute) {
		Object idmValue = super.getAttributeValue(uid, entity, attribute);

		if (attribute.isEntityAttribute()
				&& TreeSynchronizationExecutor.PARENT_FIELD.equals(attribute.getIdmPropertyName())) {
			// For Tree we need do transform parent (IdmTreeNode) to resource
			// parent format (UID of parent)
			if (idmValue instanceof IdmTreeNode) {
				// Generally we expect IdmTreeNode as parent (we will do
				// transform)
				TreeAccountFilter treeAccountFilter = new TreeAccountFilter();
				treeAccountFilter.setSystemId(this.getSytemFromSchemaAttribute(attribute.getSchemaAttribute()).getId());
				treeAccountFilter.setTreeNodeId(((IdmTreeNode) idmValue).getId());
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
	protected List<SysRoleSystemAttributeDto> findOverloadingAttributes(IdmTreeNode entity, SysSystem system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType) {
		// Overloading attributes is not implemented for TreeNode
		return new ArrayList<>();
	}
	
	@Override
	protected List<SysSystemMappingDto> findSystemMappingsForEntityType(IdmTreeNode entity, SystemEntityType entityType) {
		SystemMappingFilter mappingFilter = new SystemMappingFilter();
		mappingFilter.setEntityType(entityType);
		mappingFilter.setTreeTypeId(entity.getTreeType().getId());
		mappingFilter.setOperationType(SystemOperationType.PROVISIONING);
		List<SysSystemMappingDto> systemMappings = systemMappingService.find(mappingFilter, null).getContent();
		return systemMappings;
	}

	@Override
	protected EntityAccountFilter createEntityAccountFilter() {
		return new TreeAccountFilter();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityAccountService() {
		return treeAccountService;
	}

	@Override
	protected EntityAccountDto createEntityAccountDto() {
		return new AccTreeAccountDto();
	}

	@SuppressWarnings("rawtypes")
	@Override
	protected ReadWriteDtoService getEntityService() {
		return null; // We don't have DTO service for IdmTreeNode now
	}

	@Override
	public boolean supports(SystemEntityType delimiter) {
		return SystemEntityType.TREE == delimiter;
	}
}
