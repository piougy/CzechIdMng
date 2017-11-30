package eu.bcvsolutions.idm.acc.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.plugin.core.OrderAwarePluginRegistry;
import org.springframework.plugin.core.PluginRegistry;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemEntityDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.AccAccount_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysSystemMappingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.domain.IdmScriptCategory;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.script.evaluator.AbstractScriptEvaluator;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default system entity handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSystemMappingService
		extends AbstractEventableDtoService<SysSystemMappingDto, SysSystemMapping, SysSystemMappingFilter>
		implements SysSystemMappingService {

	private final SysSystemMappingRepository repository;
	private final GroovyScriptService groovyScriptService;
	private final PluginRegistry<AbstractScriptEvaluator, IdmScriptCategory> pluginExecutors;

	@Autowired
	public DefaultSysSystemMappingService(SysSystemMappingRepository repository, EntityEventManager entityEventManager,
			GroovyScriptService groovyScriptService,
			List<AbstractScriptEvaluator> evaluators) {
		super(repository, entityEventManager);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(groovyScriptService);
		Assert.notNull(evaluators);
		//
		this.repository = repository;
		this.groovyScriptService = groovyScriptService;
		this.pluginExecutors = OrderAwarePluginRegistry.create(evaluators);
	}

	@Override
	protected Page<SysSystemMapping> findEntities(SysSystemMappingFilter filter, Pageable pageable,
			BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}

	@Override
	public List<SysSystemMappingDto> findBySystem(SysSystemDto system, SystemOperationType operation,
			SystemEntityType entityType) {
		Assert.notNull(system);
		//
		return findBySystemId(system.getId(), operation, entityType);
	}

	@Override
	public List<SysSystemMappingDto> findBySystemId(UUID systemId, SystemOperationType operation,
			SystemEntityType entityType) {
		Assert.notNull(systemId);
		//
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(systemId);
		filter.setOperationType(operation);
		filter.setEntityType(entityType);
		Page<SysSystemMappingDto> page = toDtoPage(repository.find(filter, null));
		return page.getContent();
	}

	@Override
	public List<SysSystemMappingDto> findByObjectClass(SysSchemaObjectClassDto objectClass,
			SystemOperationType operation, SystemEntityType entityType) {
		Assert.notNull(objectClass);

		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setObjectClassId(objectClass.getId());
		filter.setOperationType(operation);
		filter.setEntityType(entityType);
		Page<SysSystemMappingDto> page = toDtoPage(repository.find(filter, null));
		return page.getContent();
	}

	@Override
	public boolean isEnabledProtection(AccAccountDto account) {
		Assert.notNull(account, "Account cannot be null!");
		Assert.notNull(account.getSystemEntity(), "SystemEntity cannot be null!");
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(account, AccAccount_.systemEntity,
				SysSystemEntityDto.class);
		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);
		List<SysSystemMappingDto> mappings = this.findBySystem(system, SystemOperationType.PROVISIONING,
				systemEntity.getEntityType());
		if (mappings.isEmpty()) {
			return false;
		}
		// We assume only one mapping for provisioning and entity type.
		return this.isEnabledProtection(mappings.get(0));
	}

	@Override
	public Integer getProtectionInterval(AccAccountDto account) {
		Assert.notNull(account, "Account cannot be null!");
		Assert.notNull(account.getSystemEntity(), "SystemEntity cannot be null!");
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(account, AccAccount_.systemEntity,
				SysSystemEntityDto.class);

		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);
		List<SysSystemMappingDto> mappings = this.findBySystem(system, SystemOperationType.PROVISIONING,
				systemEntity.getEntityType());
		if (mappings.isEmpty()) {
			return -1;
		}
		// We assume only one mapping for provisioning and entity type.
		return this.getProtectionInterval(mappings.get(0));
	}

	@Override
	public SysSystemMappingDto clone(UUID id) {
		SysSystemMappingDto original = this.get(id);
		Assert.notNull(original, "Schema attribute must be found!");

		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}

	@Override
	public boolean canBeAccountCreated(AbstractDto dto, String script, SysSystemDto system) {

		if (StringUtils.isEmpty(script)) {
			return true;
		} else {
			Map<String, Object> variables = new HashMap<>();
			// variables.put(SysSystemAttributeMappingService.ACCOUNT_UID, uid);
			variables.put(SysSystemAttributeMappingService.SYSTEM_KEY, system);
			variables.put(SysSystemAttributeMappingService.ENTITY_KEY, dto);
			// add default script evaluator, for call another scripts
			variables.put(AbstractScriptEvaluator.SCRIPT_EVALUATOR,
					pluginExecutors.getPluginFor(IdmScriptCategory.TRANSFORM_TO));

			// Add access for script evaluator
			List<Class<?>> extraClass = new ArrayList<>();
			extraClass.add(AbstractScriptEvaluator.Builder.class);
			//
			Object result = groovyScriptService.evaluate(script, variables, extraClass);
			if (result instanceof Boolean) {
				return (boolean) result;
			} else {
				throw new ProvisioningException(
						AccResultCode.PROVISIONING_SCRIPT_CAN_BE_ACC_CREATED_MUST_RETURN_BOOLEAN,
						ImmutableMap.of("system", system.getCode()));
			}
		}
	}

	private Integer getProtectionInterval(SysSystemMappingDto systemMapping) {
		Assert.notNull(systemMapping, "Mapping cannot be null!");
		return systemMapping.getProtectionInterval();
	}

	private boolean isEnabledProtection(SysSystemMappingDto systemMapping) {
		Assert.notNull(systemMapping, "Mapping cannot be null!");
		return systemMapping.isProtectionEnabled();
	}
}
