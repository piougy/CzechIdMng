package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.MappingAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityService;

/**
 * Mapping attribute to system for role
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysRoleSystemAttributeService
		extends AbstractReadWriteEntityService<SysRoleSystemAttribute, RoleSystemAttributeFilter>
		implements SysRoleSystemAttributeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysRoleSystemAttributeService.class);
	@Autowired(required = false)
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private SysSystemAttributeMappingService systeAttributeMappingService;
	@Autowired
	private GroovyScriptService groovyScriptService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private ApplicationContext applicationContext;
	private AccAccountManagementService accountManagementService;
	private ProvisioningService provisioningService;

	@Autowired
	public DefaultSysRoleSystemAttributeService(SysRoleSystemAttributeRepository repository) {
		super(repository);
	}

	@Override
	public SysRoleSystemAttribute save(SysRoleSystemAttribute entity) {
		
		// Check if exist some else attribute which is defined like unique identifier
		if (entity.isUid()) {
			RoleSystemAttributeFilter filter = new RoleSystemAttributeFilter();
			filter.setIsUid(Boolean.TRUE);
			filter.setRoleSystemId(entity.getRoleSystem().getId());
			
			List<SysRoleSystemAttribute> list = this.find(filter, null).getContent();
			
			if (list.size() > 0 && !list.get(0).getId().equals(entity.getId())) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_MORE_UID, ImmutableMap.of("role",
						entity.getRoleSystem().getRole().getName(), "system", entity.getRoleSystem().getSystem().getName()));
			}
		}
		
		// We will check exists definition for extended attribute
		Class<?> entityType = entity.getSystemAttributeMapping().getSystemMapping().getEntityType().getEntityType();
		if (entity.isExtendedAttribute() && FormableEntity.class.isAssignableFrom(entityType)) {
			AttributeMapping mappingAttributeDto = new MappingAttributeDto();
			mappingAttributeDto.setSchemaAttribute(entity.getSystemAttributeMapping().getSchemaAttribute());
			fillOverloadedAttribute(entity, mappingAttributeDto);
			systeAttributeMappingService.createExtendedAttributeDefinition(mappingAttributeDto, entityType);
		}
		
		// We will do script validation (on compilation errors), before save
		if (entity.getTransformScript() != null) {
			groovyScriptService.validateScript(entity.getTransformScript());
		}
		
		SysRoleSystemAttribute roleSystemAttribute = super.save(entity);

		// RoleSystemAttribute was changed. We need do ACC management for all
		// connected identities
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setRoleSystemId(entity.getRoleSystem().getId());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		// TODO: move to filter and use distinct
		List<IdmIdentity> identities = new ArrayList<>();
		identityAccounts.stream().forEach(identityAccount -> {
			if (!identities.contains(identityAccount.getIdentity())) {
				identities.add(identityService.get(identityAccount.getIdentity()));
			}
		});
		identities.stream().forEach(identity -> {		
			LOG.debug("Call account management for identity [{}]", identity.getUsername());
			boolean provisioningRequired = getAccountManagementService().resolveIdentityAccounts(identity);
			if(provisioningRequired){
				LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
				getProvisioningService().doProvisioning(identity);
			}
		});

		return roleSystemAttribute;
	}
	
	@Override
	public void fillOverloadedAttribute(SysRoleSystemAttribute overloadingAttribute,
			AttributeMapping overloadedAttribute) {
		overloadedAttribute.setName(overloadingAttribute.getName());
		overloadedAttribute.setEntityAttribute(overloadingAttribute.isEntityAttribute());
		overloadedAttribute.setConfidentialAttribute(overloadingAttribute.isConfidentialAttribute());
		overloadedAttribute.setExtendedAttribute(overloadingAttribute.isExtendedAttribute());
		overloadedAttribute.setIdmPropertyName(overloadingAttribute.getIdmPropertyName());
		overloadedAttribute.setTransformToResourceScript(overloadingAttribute.getTransformScript());
		overloadedAttribute.setUid(overloadingAttribute.isUid());
		overloadedAttribute.setDisabledAttribute(overloadingAttribute.isDisabledDefaultAttribute());
		overloadedAttribute.setStrategyType(overloadingAttribute.getStrategyType());
		overloadedAttribute.setSendAlways(overloadingAttribute.isSendAlways());
		overloadedAttribute.setSendOnlyIfNotNull(overloadingAttribute.isSendOnlyIfNotNull());
	}
	
	private AccAccountManagementService getAccountManagementService() {
		if (accountManagementService == null) {
			accountManagementService = applicationContext.getBean(AccAccountManagementService.class);
		}
		return accountManagementService;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private ProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(ProvisioningService.class);
		}
		return provisioningService;
	}
}
