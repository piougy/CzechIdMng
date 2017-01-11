package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaObjectClassFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass;
import eu.bcvsolutions.idm.acc.repository.SysSchemaObjectClassRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemEntityHandlingService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

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
	private final SysSystemEntityHandlingService systemEntityHandlingService;
	
	@Autowired
	public DefaultSysSchemaObjectClassService(
			SysSchemaObjectClassRepository repository,
			SysSchemaAttributeService sysSchemaAttributeService,
			SysSystemEntityHandlingService systemEntityHandlingService) {
		super(repository);
		//
		Assert.notNull(sysSchemaAttributeService, "Schema attribute service is required!");
		Assert.notNull(systemEntityHandlingService);
		//
		this.sysSchemaAttributeService = sysSchemaAttributeService;
		this.systemEntityHandlingService = systemEntityHandlingService;
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
		systemEntityHandlingService.findByObjectClass(schemaObjectClass, null, null).forEach(systemEntityHandling -> {
			systemEntityHandlingService.delete(systemEntityHandling);
		});
		//
		super.delete(schemaObjectClass);
	}
}
