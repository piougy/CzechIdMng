package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Mapping attribute to system for role
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysRoleSystemAttributeService extends
		AbstractReadWriteDtoService<SysRoleSystemAttributeDto, SysRoleSystemAttribute, SysRoleSystemAttributeFilter>
		implements SysRoleSystemAttributeService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultSysRoleSystemAttributeService.class);
	
	private final SysRoleSystemAttributeRepository repository;
	@Autowired(required = false)
	private AccIdentityAccountService identityAccountService;
	@Autowired
	private SysSystemAttributeMappingService systeAttributeMappingService;
	@Autowired
	private GroovyScriptService groovyScriptService;
	@Autowired
	private FormService formService;
	@Autowired
	private IdmIdentityService identityService;
	@Autowired
	private ApplicationContext applicationContext;
	private AccAccountManagementService accountManagementService;
	private ProvisioningService provisioningService;
	@Autowired
	private SysRoleSystemService roleSystemService;
	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSystemMappingService systemMappingService;
	@Autowired
	private IdmRoleService roleService;
	
	@Autowired private SysSchemaObjectClassService sysSchemaObjectClassService;
	@Autowired private SysSystemMappingService sysSystemMappingService;
	@Autowired private SysSchemaAttributeService sysSchemaAttributeService;
	@Autowired private SysSystemAttributeMappingService sysSystemAttributeMappingService;
	@Autowired private SysRoleSystemService sysRoleSystemService;

	@Autowired
	public DefaultSysRoleSystemAttributeService(SysRoleSystemAttributeRepository repository) {
		super(repository);
		//
		this.repository = repository;
	}
	
	@Override
	protected Page<SysRoleSystemAttribute> findEntities(SysRoleSystemAttributeFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	public SysRoleSystemAttributeDto save(SysRoleSystemAttributeDto dto, BasePermission... permission) {
		// Check if exist some else attribute which is defined like unique
		// identifier
		if (dto.isUid()) {
			SysRoleSystemAttributeFilter filter = new SysRoleSystemAttributeFilter();
			filter.setIsUid(Boolean.TRUE);
			filter.setRoleSystemId(dto.getRoleSystem());

			List<SysRoleSystemAttributeDto> list = this.find(filter, null).getContent();

			if (list.size() > 0 && !list.get(0).getId().equals(dto.getId())) {
				SysRoleSystemDto roleSystem = roleSystemService.get(dto.getRoleSystem());
				IdmRoleDto roleDto = roleService.get(roleSystem.getRole());
				SysSystemDto systemDto = DtoUtils.getEmbedded(dto, SysRoleSystem_.system, SysSystemDto.class);
				throw new ProvisioningException(AccResultCode.PROVISIONING_ROLE_ATTRIBUTE_MORE_UID,
						ImmutableMap.of("role", roleDto.getName(), "system", systemDto.getName()));
			}
		}

		// We will check exists definition for extended attribute
		SysSystemAttributeMappingDto systemAttributeMapping = systemAttributeMappingService
				.get(dto.getSystemAttributeMapping());
		SysSystemMappingDto systemMapping = systemMappingService.get(systemAttributeMapping.getSystemMapping());
		Class<?extends Identifiable> entityType = systemMapping.getEntityType().getEntityType();
		if (dto.isExtendedAttribute() && formService.isFormable(entityType)) {
			systeAttributeMappingService.createExtendedAttributeDefinition(dto, entityType);
		}

		// We will do script validation (on compilation errors), before save
		if (dto.getTransformScript() != null) {
			groovyScriptService.validateScript(dto.getTransformScript());
		}

		SysRoleSystemAttributeDto roleSystemAttribute = super.save(dto, permission);

		// RoleSystemAttribute was changed. We need do ACC management for all
		// connected identities
		AccIdentityAccountFilter filter = new AccIdentityAccountFilter();
		filter.setRoleSystemId(dto.getRoleSystem());
		List<AccIdentityAccountDto> identityAccounts = identityAccountService.find(filter, null).getContent();
		// TODO: move to filter and use distinct
		List<IdmIdentityDto> identities = new ArrayList<>();
		identityAccounts.stream().forEach(identityAccount -> {
			if (!identities.contains(identityAccount.getIdentity())) {
				// TODO: embedded
				identities.add(identityService.get(identityAccount.getIdentity()));
			}
		});
		identities.stream().forEach(identity -> {
			LOG.debug("Call account management for identity [{}]", identity.getUsername());
			boolean provisioningRequired = getAccountManagementService().resolveIdentityAccounts(identity);
			if (provisioningRequired) {
				LOG.debug("Call provisioning for identity [{}]", identity.getUsername());
				getProvisioningService().doProvisioning(identity);
			}
		});

		return roleSystemAttribute;
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
	
	@Override
	public void addRoleMappingAttribute(UUID systemId, UUID roleId, String attributeName, String transformationScript,
			String objectClassName, SysRoleSystemAttributeDto attribute) { // ObjectClassName "__ACCOUNT__"
		Assert.notNull(systemId, "SystemId cannot be null!");
		Assert.notNull(roleId, "RoleId cannot be null!");
		Assert.notNull(attributeName, "Attribute name cannot be null");
		Assert.hasLength(attributeName, "Attribute name cannot be blank");

		SysRoleSystemAttributeDto systemAttribute = attribute;
		UUID roleSystemId = getSysRoleSystem(systemId, roleId, objectClassName);
		UUID systemAttributeMappingId = getSystemAttributeMapping(systemId, attributeName, objectClassName);
		//
		//
		systemAttribute.setName(attributeName);
		systemAttribute.setRoleSystem(roleSystemId);
		systemAttribute.setSystemAttributeMapping(systemAttributeMappingId);
		//
		if (transformationScript != null) {
			systemAttribute.setTransformScript(transformationScript);
		}
		//
		SysRoleSystemAttributeDto currentSystemAttribute = getSystemAttribute(roleSystemId, attributeName);
		if (currentSystemAttribute == null) {
			this.save(systemAttribute);
		} else {
			currentSystemAttribute.setTransformScript(transformationScript);
			this.save(currentSystemAttribute);
		}

	}
	
	/**
	 * Returns existing role's system or returns newly created one. 
	 * @param systemId
	 * @param roleId
	 * @param objectClassName
	 * @return
	 */
	private UUID getSysRoleSystem(UUID systemId, UUID roleId, String objectClassName) {
		SysRoleSystemFilter filter = new SysRoleSystemFilter();
		filter.setRoleId(roleId);
		filter.setSystemId(systemId);
		List<SysRoleSystemDto> roleSystem = sysRoleSystemService.find(filter, null).getContent();
		if(roleSystem.size() == 1) {
			return roleSystem.stream().findFirst().get().getId();
		}
			SysRoleSystemDto sys = new SysRoleSystemDto();
			sys.setRole(roleId);
			sys.setSystem(systemId);
			sys.setSystemMapping(getSystemMapping(systemId, objectClassName));
			return sysRoleSystemService.save(sys).getId();
	}

	/**
	 * Returns provisioning mapping of system.
	 * @param systemId
	 * @param objectClassName
	 * @return
	 */
	private UUID getSystemMapping(UUID systemId, String objectClassName) {
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(systemId);
		filter.setOperationType(SystemOperationType.PROVISIONING);
		filter.setObjectClassId(getObjectClassId(systemId, objectClassName));

		List<SysSystemMappingDto> content = sysSystemMappingService.find(filter, null).getContent();
		if (content.isEmpty()) {
			throw new RuntimeException("Cannot find system mapping");
		}
		return content.get(0).getId();
	}

	/**
	 * Returns systems object's scheme
	 * @param systemId
	 * @param objectClassName
	 * @return
	 */
	private UUID getObjectClassId(UUID systemId, String objectClassName) {
		SysSchemaObjectClassFilter filter = new SysSchemaObjectClassFilter();
		filter.setSystemId(systemId);
		filter.setObjectClassName(objectClassName);
		List<SysSchemaObjectClassDto> objectClasses = sysSchemaObjectClassService.find(filter, null).getContent();
		if (objectClasses.isEmpty()) {
			throw new RuntimeException("Cannot find system schema");
		}
		return objectClasses.get(0).getId();
	}

	/**
	 * Returns system's attribute mapping
	 * @param systemId
	 * @param attributeName
	 * @param objectClassName
	 * @return
	 */
	private UUID getSystemAttributeMapping(UUID systemId, String attributeName, String objectClassName) {
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSystemId(systemId);
		filter.setSchemaAttributeId(getSchemaAttr(systemId, attributeName, objectClassName));

		List<SysSystemAttributeMappingDto> content = sysSystemAttributeMappingService.find(filter, null).getContent();
		if (content.isEmpty()) {
			throw new RuntimeException("Cannot find System attribute mapping");
		}
		return content.get(0).getId();
	}

	/**
	 * Returns schema attribute
	 * @param systemId
	 * @param attributeName
	 * @param objectClassName
	 * @return
	 */
	private UUID getSchemaAttr(UUID systemId, String attributeName, String objectClassName) {
		SysSchemaAttributeFilter filter = new SysSchemaAttributeFilter();
		filter.setObjectClassId(getObjectClassId(systemId, objectClassName));
		filter.setText(attributeName);
		List<SysSchemaAttributeDto> list = sysSchemaAttributeService.find(filter, null).getContent();
		if (list.isEmpty()) {
			throw new RuntimeException("Cannot find Schema attribute");
		}
		return list.get(0).getId();
	}

	/**
	 * Returns existing system attribute or null
	 * @param attr
	 * @return
	 */
	private SysRoleSystemAttributeDto getSystemAttribute(UUID roleSystem, String attributeName) {
		SysRoleSystemAttributeFilter filter = new SysRoleSystemAttributeFilter();
		filter.setRoleSystemId(roleSystem);
		List<SysRoleSystemAttributeDto> content = this.find(filter, null).getContent();
		for(SysRoleSystemAttributeDto attribute : content) {
			if (attribute.getName().equals(attributeName)) {
				return attribute;
			}
		}
		return null;
	}
}
