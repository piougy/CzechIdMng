package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.repository.SysSyncConfigRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemMappingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default system entity handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSystemMappingService extends
		AbstractReadWriteEntityService<SysSystemMapping, SystemMappingFilter> implements SysSystemMappingService {

	private final SysSystemMappingRepository repository;
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysSyncConfigRepository syncConfigRepository;

	@Autowired
	public DefaultSysSystemMappingService(
			SysSystemMappingRepository repository,
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysSyncConfigRepository syncConfigRepository) {
		super(repository);
		//
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(syncConfigRepository);
		//
		this.repository = repository;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.syncConfigRepository = syncConfigRepository;
	}
	
	@Override
	public List<SysSystemMapping> findBySystem(SysSystem system, SystemOperationType operation, SystemEntityType entityType){
		Assert.notNull(system);
		
		SystemMappingFilter filter = new SystemMappingFilter();
		filter.setSystemId(system.getId());
		filter.setOperationType(operation);
		filter.setEntityType(entityType);
		Page<SysSystemMapping> page = repository.find(filter, null);
		return page.getContent();
	}
	
	@Override
	public List<SysSystemMapping> findByObjectClass(SysSchemaObjectClass objectClass, SystemOperationType operation, SystemEntityType entityType){
		Assert.notNull(objectClass);
		
		SystemMappingFilter filter = new SystemMappingFilter();
		filter.setObjectClassId(objectClass.getId());
		filter.setOperationType(operation);
		filter.setEntityType(entityType);
		Page<SysSystemMapping> page = repository.find(filter, null);
		return page.getContent();
	}
	
	@Override
	@Transactional
	public void delete(SysSystemMapping systemMapping) {
		Assert.notNull(systemMapping);
		// 
		if (syncConfigRepository.countBySystemMapping(systemMapping) > 0) {
			throw new ResultCodeException(AccResultCode.SYSTEM_MAPPING_DELETE_FAILED_USED_IN_SYNC, ImmutableMap.of("mapping", systemMapping.getName()));
		}
		// remove all handled attributes
		SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMapping.getId());
		systemAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systemAttributeMappingService.delete(systemAttributeMapping);
		});
		//
		super.delete(systemMapping);
	}
}
