package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.apache.http.util.Asserts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Default schema object class service
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaObjectClassService extends AbstractReadWriteEntityService<SysSchemaObjectClass, SchemaObjectClassFilter>
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
	public void delete(SysSchemaObjectClass schemaObjectClass) {
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
		super.delete(schemaObjectClass);
	}

	@Override
	public SysSchemaObjectClass clone(UUID id) {
		SysSchemaObjectClass original = this.get(id);
		Assert.notNull(original, "Schema must be found!");
		
		// We do detach this entity (and set id to null)
		entityManager.detach(original);
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}

	@Override
	public SysSchemaObjectClass duplicate(UUID id, SysSystem system) {
		Assert.notNull(id, "Id of duplication schema, must be filled!");
		Assert.notNull(system, "Parent system must be filled!");
		SysSchemaObjectClass clonedSchema = this.clone(id);
		clonedSchema.setSystem(system);
		SysSchemaObjectClass schema = this.save(clonedSchema);
		
		SchemaAttributeFilter schemaAttributesFilter = new SchemaAttributeFilter();
		schemaAttributesFilter.setObjectClassId(id);
		sysSchemaAttributeService.find(schemaAttributesFilter, null).forEach(schemaAttribute -> {
			SysSchemaAttribute clonedAttribut = sysSchemaAttributeService.clone(schemaAttribute.getId());
			clonedAttribut.setObjectClass(schema);
			clonedAttribut = sysSchemaAttributeService.save(clonedAttribut);
		});	
		
		return schema;
	}
}
