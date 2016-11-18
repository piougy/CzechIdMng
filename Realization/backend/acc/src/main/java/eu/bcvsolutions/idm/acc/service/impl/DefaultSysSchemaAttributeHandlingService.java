package eu.bcvsolutions.idm.acc.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeHandlingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;

/**
 * Default schema attributes handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeHandlingService extends AbstractReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter>
		implements SysSchemaAttributeHandlingService {

	@Autowired
	private SysSchemaAttributeHandlingRepository repository;

	@Override
	protected AbstractEntityRepository<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> getRepository() {
		return repository;
	}
	
	public List<SysSchemaAttributeHandling> findByEntityHandling(SysSystemEntityHandling entityHandling){
		Assert.notNull(entityHandling);
		
		SchemaAttributeHandlingFilter filter = new SchemaAttributeHandlingFilter();
		filter.setEntityHandlingId(entityHandling.getId());
		Page<SysSchemaAttributeHandling> page = repository.find(filter, null);
		return page.getContent();
	}
	
	@Override
	public Object transformValueToSystem(Object value, SysSchemaAttributeHandling attributeHandling){
		Assert.notNull(attributeHandling);
		
		// TODO transformation system
		
		return value;
	}
	
	@Override
	public Object transformValueFromSystem(Object value, SysSchemaAttributeHandling attributeHandling){
		Assert.notNull(attributeHandling);
		
		// TODO transformation system
		
		return value;
	}
}
