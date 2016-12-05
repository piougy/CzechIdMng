package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeHandlingRepository;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
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

	private final SysSchemaAttributeHandlingRepository schemaAttributeHandlingRepository;

	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			SysSchemaAttributeHandlingRepository schemaAttributeHandlingRepository) {
		super(repository);
		//
		Assert.notNull(schemaAttributeHandlingRepository);
		//
		this.schemaAttributeHandlingRepository = schemaAttributeHandlingRepository;
	}
	
	@Override
	@Transactional
	public void delete(SysSchemaAttribute schemaAttribute) {
		Assert.notNull(schemaAttribute);
		// 
		// remove all handled attributes
		schemaAttributeHandlingRepository.deleteBySchemaAttribute(schemaAttribute);
		//
		super.delete(schemaAttribute);
	}
}
