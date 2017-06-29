package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import javax.persistence.EntityManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.utils.EntityUtils;

/**
 * Default schema attributes
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeService extends AbstractReadWriteEntityService<SysSchemaAttribute, SchemaAttributeFilter>
		implements SysSchemaAttributeService {

	private final SysSystemAttributeMappingService systeAttributeMappingService;
	private final EntityManager entityManager;

	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			SysSystemAttributeMappingService systeAttributeMappingService,
			EntityManager entityManager) {
		super(repository);
		//
		Assert.notNull(systeAttributeMappingService);
		Assert.notNull(entityManager);
		//
		this.systeAttributeMappingService = systeAttributeMappingService;
		this.entityManager = entityManager;
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttribute schemaAttribute) {
		Assert.notNull(schemaAttribute);
		// 
		// remove all handled attributes
		SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());
		systeAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systeAttributeMappingService.delete(systemAttributeMapping);
		});
		//
		super.delete(schemaAttribute);
	}
	
	@Override
	@Transactional
	public SysSchemaAttribute save(SysSchemaAttribute entity) {
		// TODO Auto-generated method stub
		return super.save(entity);
	}
	
	@Override
	public SysSchemaAttribute clone(UUID id) {
		SysSchemaAttribute original = this.get(id);
		Assert.notNull(original, "Schema attribute must be found!");
		
		// We do detach this entity (and set id to null)
		entityManager.detach(original);
		original.setId(null);
		EntityUtils.clearAuditFields(original);
		return original;
	}
}
