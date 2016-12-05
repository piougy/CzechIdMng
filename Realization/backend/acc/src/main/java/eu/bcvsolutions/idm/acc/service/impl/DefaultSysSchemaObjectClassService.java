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
	
	@Autowired
	public DefaultSysSchemaObjectClassService(
			SysSchemaObjectClassRepository repository,
			SysSchemaAttributeService sysSchemaAttributeService) {
		super(repository);
		//
		Assert.notNull(sysSchemaAttributeService, "Schema attribute service is required!");
		//
		this.sysSchemaAttributeService = sysSchemaAttributeService;
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
		//
		super.delete(schemaObjectClass);
	}
}
