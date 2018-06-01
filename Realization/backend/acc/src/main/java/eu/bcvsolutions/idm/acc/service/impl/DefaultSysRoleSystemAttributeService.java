package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AccIdentityAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysRoleSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccIdentityAccountFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SysRoleSystemAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystemAttribute;
import eu.bcvsolutions.idm.acc.entity.SysRoleSystem_;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
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
				SysSystemDto systemDto = DtoUtils.getEmbedded(dto, SysRoleSystem_.system);
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
			if (!identities.contains(new IdmIdentityDto(identityAccount.getIdentity()))) {
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
}
