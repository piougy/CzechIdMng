package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;

/**
 * Default schema object class service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaObjectClassService extends AbstractReadWriteDtoService<SysSchemaObjectClassDto, SysSchemaObjectClass, SchemaObjectClassFilter>
		implements SysSchemaObjectClassService {

	private final SysSchemaAttributeService sysSchemaAttributeService;
	private final SysSystemMappingService systemMappingService;
	private final EntityManager entityManager;
	
	@Autowired
	public DefaultSysSchemaObjectClassService(
			SysSchemaObjectClassRepository repository,
			SysSchemaAttributeService sysSchemaAttributeService,
			SysSystemMappingService systemMappingService,
			EntityManager entityManager) {
		super(repository);
		//
		Assert.notNull(sysSchemaAttributeService, "Schema attribute service is required!");
		Assert.notNull(systemMappingService);
		Assert.notNull(entityManager);
		//
		this.sysSchemaAttributeService = sysSchemaAttributeService;
		this.systemMappingService = systemMappingService;
		this.entityManager = entityManager;
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaObjectClassDto schemaObjectClass, BasePermission... permission) {
		Assert.notNull(schemaObjectClass);
		//
		// remove all schema attributes for 
		SchemaAttributeFilter filter = new SchemaAttributeFilter();
		filter.setObjectClassId(schemaObjectClass.getId());
		sysSchemaAttributeService.find(filter, null).forEach(schemaAttribute -> {
			sysSchemaAttributeService.delete(schemaAttribute);
		});	
		// delete all mappings
		systemMappingService.findByObjectClass(schemaObjectClass, null, null).forEach(systemMapping -> {
			systemMappingService.delete(systemMapping);
		});
		//
		super.delete(schemaObjectClass, permission);
	}

	@Override
	public SysSchemaObjectClassDto clone(UUID id) {
		SysSchemaObjectClassDto original = this.get(id);
		Assert.notNull(original, "Schema must be found!");
		
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}
}
