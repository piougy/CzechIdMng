package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.filter.SchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
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

	private final SysSystemAttributeMappingService systeAttributeMappingService;

	@Autowired
	public DefaultSysSchemaAttributeService(
			SysSchemaAttributeRepository repository,
			SysSystemAttributeMappingService systeAttributeMappingService) {
		super(repository);
		//
		Assert.notNull(systeAttributeMappingService);
		//
		this.systeAttributeMappingService = systeAttributeMappingService;
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
}
