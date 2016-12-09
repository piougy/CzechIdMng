package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default schema attributes
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeService extends AbstractReadWriteEntityService<SysSchemaAttribute, SchemaAttributeFilter>
		implements SysSchemaAttributeService {

	private final SysSchemaAttributeHandlingService schemaAttributeHandlingService;

	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			SysSchemaAttributeHandlingService schemaAttributeHandlingService) {
		super(repository);
		//
		Assert.notNull(schemaAttributeHandlingService);
		//
		this.schemaAttributeHandlingService = schemaAttributeHandlingService;
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttribute schemaAttribute) {
		Assert.notNull(schemaAttribute);
		// 
		// remove all handled attributes
		SchemaAttributeHandlingFilter filter = new SchemaAttributeHandlingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());
		schemaAttributeHandlingService.find(filter, null).forEach(schemaAttributeHandling -> {
			schemaAttributeHandlingService.delete(schemaAttributeHandling);
		});
		//
		super.delete(schemaAttribute);
	}
}
