package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
import eu.bcvsolutions.idm.acc.repository.SysSystemMappingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default system entity handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSystemMappingService extends
	AbstractEventableDtoService<SysSystemMappingDto, SysSystemMapping, SysSystemMappingFilter> implements SysSystemMappingService {

	private final SysSystemMappingRepository repository;

	@Autowired
	public DefaultSysSystemMappingService(
			SysSystemMappingRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		Assert.notNull(entityEventManager);
		//
		this.repository = repository;
	}
	
	@Override
	protected Page<SysSystemMapping> findEntities(SysSystemMappingFilter filter, Pageable pageable, BasePermission... permission) {
		if (filter == null) {
			return repository.findAll(pageable);
		}
		return repository.find(filter, pageable);
	}
	
	@Override
	public List<SysSystemMappingDto> findBySystem(SysSystemDto system, SystemOperationType operation, SystemEntityType entityType){
		Assert.notNull(system);
		
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setSystemId(system.getId());
		filter.setOperationType(operation);
		filter.setEntityType(entityType);
		Page<SysSystemMappingDto> page = toDtoPage(repository.find(filter, null));
		return page.getContent();
	}
	
	@Override
	public List<SysSystemMappingDto> findByObjectClass(SysSchemaObjectClassDto objectClass, SystemOperationType operation, SystemEntityType entityType){
		Assert.notNull(objectClass);
		
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setObjectClassId(objectClass.getId());
		filter.setOperationType(operation);
		filter.setEntityType(entityType);
		Page<SysSystemMappingDto> page = toDtoPage(repository.find(filter, null));
		return page.getContent();
	}
	
	@Override
	public boolean isEnabledProtection(AccAccountDto account){
		Assert.notNull(account, "Account cannot be null!");
		Assert.notNull(account.getSystemEntity(), "SystemEntity cannot be null!");
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(account, AccAccount_.systemEntity, SysSystemEntityDto.class);
		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);
		List<SysSystemMappingDto> mappings = this.findBySystem(system, SystemOperationType.PROVISIONING, systemEntity.getEntityType());
		if(mappings.isEmpty()){
			return false;
		}
		// We assume only one mapping for provisioning and entity type.
		return this.isEnabledProtection(mappings.get(0));
	}
	
	@Override
	public Integer getProtectionInterval(AccAccountDto account){
		Assert.notNull(account, "Account cannot be null!");
		Assert.notNull(account.getSystemEntity(), "SystemEntity cannot be null!");
		SysSystemEntityDto systemEntity = DtoUtils.getEmbedded(account, AccAccount_.systemEntity, SysSystemEntityDto.class);

		SysSystemDto system = DtoUtils.getEmbedded(account, AccAccount_.system, SysSystemDto.class);
		List<SysSystemMappingDto> mappings = this.findBySystem(system, SystemOperationType.PROVISIONING, systemEntity.getEntityType());
		if(mappings.isEmpty()){
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

	private Integer getProtectionInterval(SysSystemMappingDto systemMapping){
		Assert.notNull(systemMapping, "Mapping cannot be null!");
		return systemMapping.getProtectionInterval();
	}

	private boolean isEnabledProtection(SysSystemMappingDto systemMapping){
		Assert.notNull(systemMapping, "Mapping cannot be null!");
		return systemMapping.isProtectionEnabled();
	}
}
