package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.ProvisioningOperationType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.AccTreeAccountDto;
import eu.bcvsolutions.idm.acc.dto.EntityAccountDto;
import eu.bcvsolutions.idm.acc.dto.ProvisioningAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.TreeAccountFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount;
import eu.bcvsolutions.idm.acc.entity.AccTreeAccount;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntity;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccTreeAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningExecutor;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.acc.service.api.TreeProvisioningService;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.service.api.IdmTreeNodeService;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Service for do tree provisioning
 * 
 * @author svandav
 *
 */
@Service
@Qualifier(value=DefaultTreeProvisioningService.NAME)
public class DefaultTreeProvisioningService extends AbstractProvisioningService<IdmTreeNode> implements TreeProvisioningService {
 
	public static final String NAME = "treeProvisioningService";
	public static final String PASSWORD_SCHEMA_PROPERTY_NAME = "__PASSWORD__";
	private final AccTreeAccountService treeAccountService;
	private final IdmTreeNodeService treeNodeService;
	
	@Autowired
	public DefaultTreeProvisioningService(SysSystemMappingService systemMappingService,
			SysSystemAttributeMappingService attributeMappingService, IcConnectorFacade connectorFacade,
			SysSystemService systemService, SysRoleSystemService roleSystemService,
			AccAccountManagementService accountManagementService,
			SysRoleSystemAttributeService roleSystemAttributeService, SysSystemEntityService systemEntityService,
			AccAccountService accountService, AccTreeAccountService treeAccountService,
			ProvisioningExecutor provisioningExecutor, IdmTreeNodeService treeNodeService) {
		super(systemMappingService, attributeMappingService, connectorFacade, systemService, roleSystemService,
				accountManagementService, roleSystemAttributeService, systemEntityService, accountService,
				provisioningExecutor);
		
		Assert.notNull(treeAccountService);
		Assert.notNull(treeNodeService);
		
		this.treeAccountService = treeAccountService;
		this.treeNodeService = treeNodeService;
	}

	@Override
	public void doProvisioning(IdmTreeNode node) {
		Assert.notNull(node);
		//
		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setTreeNodeId(node.getId());
		List<? extends EntityAccountDto> entityAccoutnList = treeAccountService.findDto(filter, null).getContent();
		if (entityAccoutnList == null) {
			return;
		}

		List<AccAccount> accounts = new ArrayList<>();
		entityAccoutnList.stream().filter(ia -> {
			return ia.isOwnership();
		}).forEach((treeAccount) -> {
			if (!accounts.contains(treeAccount.getAccount())) {
				accounts.add(accountService.get(treeAccount.getAccount()));
			}
		});

		accounts.stream().forEach(account -> {
			this.doProvisioning(account, node);
		});
	}
	
	public void doProvisioning(AccAccount account) {
		Assert.notNull(account);

		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setAccountId(account.getId());
		List<? extends EntityAccountDto> treeAccoutnList = treeAccountService.findDto(filter, null).getContent();
		if (treeAccoutnList == null) {
			return;
		}
		treeAccoutnList.stream().filter(treeAccount -> {
			return treeAccount.isOwnership();
		}).forEach((treeAccount) -> {
			doProvisioning(account, (IdmTreeNode) treeNodeService.get(treeAccount.getEntity()));
		});
	}

	@Override
	public void changePassword(IdmTreeNode node, PasswordChangeDto passwordChange) {
		Assert.notNull(node);
		Assert.notNull(passwordChange);

		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setTreeNodeId(node.getId());
		List<? extends EntityAccountDto> treeAccountList = treeAccountService.findDto(filter, null).getContent();
		if (treeAccountList == null) {
			return;
		}
		
		// Distinct by accounts
		List<AccAccount> accounts = new ArrayList<>();
		treeAccountList.stream().filter(treeAccount -> {
			return treeAccount.isOwnership() && (passwordChange.isAll()
					|| passwordChange.getAccounts().contains(treeAccount.getId().toString()));
		}).forEach(treeAccount -> {
			if (!accounts.contains(treeAccount.getAccount())) {
				accounts.add(accountService.get(treeAccount.getAccount()));
			}
		});

		accounts.forEach(account -> {
			// find uid from system entity or from account
			String uid = account.getUid();
			SysSystem system = account.getSystem();
			SysSystemEntity systemEntity = account.getSystemEntity();
			//
			// Find mapped attributes (include overloaded attributes)
			List<AttributeMapping> finalAttributes = resolveMappedAttributes(uid, account, node, system, systemEntity.getEntityType());
			if (CollectionUtils.isEmpty(finalAttributes)) {
				return;
			}
			
			// We try find __PASSWORD__ attribute in mapped attributes
			Optional<? extends AttributeMapping> attriubuteHandlingOptional = finalAttributes.stream()
					.filter((attribute) -> {
						return PASSWORD_SCHEMA_PROPERTY_NAME.equals(attribute.getSchemaAttribute().getName());
					}).findFirst();
			if (!attriubuteHandlingOptional.isPresent()) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_PASSWORD_FIELD_NOT_FOUND,
						ImmutableMap.of("uid", uid));
			}
			AttributeMapping mappedAttribute = attriubuteHandlingOptional.get();

			doProvisioningForAttribute(systemEntity, mappedAttribute, passwordChange.getNewPassword(),
					ProvisioningOperationType.UPDATE, node);
		});
	}
	
	/**
	 * Prepare all mapped attribute values (= account)
	 * @param entity
	 * @param operationType
	 * @param systemEntity
	 * @param attributes
	 * @return
	 */
	@Override
	protected Map<ProvisioningAttributeDto, Object> preapareMappedAttributesValues(IdmTreeNode entity,
			ProvisioningOperationType operationType, SysSystemEntity systemEntity,
			List<? extends AttributeMapping> attributes) {
		Map<ProvisioningAttributeDto, Object> accountAttributes = new HashMap<>();
		if (ProvisioningOperationType.DELETE != operationType) { // delete - account attributes is not needed
			
			// First we will resolve attribute without MERGE strategy
			attributes.stream().filter(attribute -> {
				return !attribute.isDisabledAttribute() 
						&& AttributeMappingStrategyType.AUTHORITATIVE_MERGE != attribute.getStrategyType() 
						&& AttributeMappingStrategyType.MERGE != attribute.getStrategyType() ;
			}).forEach(attribute -> {
				ProvisioningAttributeDto provisioningAttributeDto = ProvisioningAttributeDto.createProvisioningAttributeKey(attribute);
				if(attribute.isUid()){
					// For UID attribute, we will set as value always UID form account
					// TODO: now we set UID from SystemEntity, may be UID from AccAccount will be more correct
					accountAttributes.put(provisioningAttributeDto, systemEntity.getUid());
				}else if(attribute.isEntityAttribute() && TreeSynchronizationExecutor.PARENT_FIELD.equals(attribute.getIdmPropertyName())){
					// For Tree we need do transform parent (IdmTreeNode) to resource parent format (UID of parent)
					Object idmValue = attributeMappingService.getAttributeValue(entity, attribute);
					if(idmValue instanceof IdmTreeNode){
						// Generally we expect IdmTreeNode as parent (we will do transform)
						TreeAccountFilter treeAccountFilter = new TreeAccountFilter();
						treeAccountFilter.setSystemId(attribute.getSchemaAttribute().getObjectClass().getSystem().getId());
						treeAccountFilter.setTreeNodeId(((IdmTreeNode)idmValue).getId());
						List<AccTreeAccountDto> treeAccounts =  treeAccountService.findDto(treeAccountFilter, null).getContent();
						if(treeAccounts.isEmpty()){
							throw new ProvisioningException(AccResultCode.PROVISIONING_TREE_PARENT_ACCOUNT_NOT_FOUND,
									ImmutableMap.of("parentNode", idmValue));
						}
						if(treeAccounts.size() != 1){
							throw new ProvisioningException(AccResultCode.PROVISIONING_TREE_TOO_MANY_PARENT_ACCOUNTS,
									ImmutableMap.of("parentNode", idmValue));
						}
						AccTreeAccountDto treeAccount = treeAccounts.get(0);
						String parentUid = accountService.get(treeAccount.getAccount()).getUid();
						accountAttributes.put(provisioningAttributeDto, parentUid);
					}else {
						// If is parent not instance of IdmTreeNode, then we set value without any transform
						accountAttributes.put(provisioningAttributeDto, idmValue);
					}
				}else {
					accountAttributes.put(provisioningAttributeDto, attributeMappingService.getAttributeValue(entity, attribute));
				}
			});
			
			// Second we will resolve MERGE attributes
			List<? extends AttributeMapping> attributesMerge = attributes.stream().filter(attribute -> {
				return !attribute.isDisabledAttribute() 
						&& (AttributeMappingStrategyType.AUTHORITATIVE_MERGE == attribute.getStrategyType() 
						|| AttributeMappingStrategyType.MERGE == attribute.getStrategyType());
				
			}).collect(Collectors.toList());
			
			for(AttributeMapping attributeParent : attributesMerge){
				ProvisioningAttributeDto attributeParentKey = ProvisioningAttributeDto.createProvisioningAttributeKey(attributeParent);
			
				if(!attributeParent.getSchemaAttribute().isMultivalued()){
					throw new ProvisioningException(AccResultCode.PROVISIONING_MERGE_ATTRIBUTE_IS_NOT_MULTIVALUE,
							ImmutableMap.of("object", systemEntity.getUid(), "attribute", attributeParent.getSchemaAttribute().getName()));
				}
				
				List<Object> mergedValues = new ArrayList<>();
				attributes.stream().filter(attribute -> {
					return !accountAttributes.containsKey(attributeParentKey)
							&& attributeParent.getSchemaAttribute().equals(attribute.getSchemaAttribute()) 
							&& attributeParent.getStrategyType() == attribute.getStrategyType();
				}).forEach(attribute -> {
					Object value = attributeMappingService.getAttributeValue(entity, attribute);
					// We don`t want null item in list (problem with provisioning in IC)
					if(value != null){
						// If is value collection, then we add all its items to main list!
						if(value instanceof Collection){
							Collection<?> collectionNotNull = ((Collection<?>)value).stream().filter(item -> {
								return item != null;
							}).collect(Collectors.toList());
							mergedValues.addAll(collectionNotNull);
						}else {
							mergedValues.add(value);
						}
					}
				});
				if(!accountAttributes.containsKey(attributeParentKey)){
					accountAttributes.put(attributeParentKey, mergedValues);
				}
			}
		}
		return accountAttributes;
	}


	/**
	 * Return all mapped attributes for this account (include overloaded attributes)
	 * 
	 * @param uid
	 * @param account
	 * @param entity
	 * @param system
	 * @param entityType
	 * @return
	 */
	@Override
	public List<AttributeMapping> resolveMappedAttributes(String uid, AccAccount account, IdmTreeNode entity, SysSystem system, SystemEntityType entityType) {
		TreeAccountFilter filter = new TreeAccountFilter();
		filter.setTreeNodeId(entity.getId());
		filter.setSystemId(system.getId());
		filter.setOwnership(Boolean.TRUE);
		filter.setAccountId(account.getId());
		
		List<? extends EntityAccountDto> entityAccoutnList = treeAccountService.findDto(filter, null).getContent();
		if (entityAccoutnList == null) {
			return null;
		}
		// All identity account with flag ownership on true

		// All role system attributes (overloading) for this uid and same system
		List<SysRoleSystemAttribute> roleSystemAttributesAll = findOverloadingAttributes(uid, entity, system, entityAccoutnList, entityType);

		// All default mapped attributes from system
		List<? extends AttributeMapping> defaultAttributes = findAttributeMappings(system, entityType);

		// Final list of attributes use for provisioning
		return compileAttributes(defaultAttributes, roleSystemAttributesAll);
	}
	
	@Override
	protected List<SysRoleSystemAttribute> findOverloadingAttributes(String uid, IdmTreeNode entity, SysSystem system,
			List<? extends EntityAccountDto> idenityAccoutnList, SystemEntityType entityType) {
		// Overloading attributes is not implemented for TreeNode
		return new ArrayList<>();
	}

}
