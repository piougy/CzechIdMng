package eu.bcvsolutions.idm.acc.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeHandlingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;

/**
 * Default schema attributes handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeHandlingService extends AbstractReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter>
		implements SysSchemaAttributeHandlingService {


	private SysSchemaAttributeHandlingRepository repository;
	private GroovyScriptService groovyScriptService;

	@Autowired
	public DefaultSysSchemaAttributeHandlingService(SysSchemaAttributeHandlingRepository repository,
			GroovyScriptService groovyScriptService) {
		super();
		this.repository = repository;
		this.groovyScriptService = groovyScriptService;
	}

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
	public Object transformValueToResource(Object value, SysSchemaAttributeHandling attributeHandling, AbstractEntity entity){
		Assert.notNull(attributeHandling);
		
		if(attributeHandling.getTransformToResourceScript() != null){
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, attributeHandling.getSystemEntityHandling().getSystem());
			variables.put(ENTITY_KEY, entity);
			return groovyScriptService.evaluate(attributeHandling.getTransformToResourceScript(), variables);
		}
		
		return value;
	}
	
	@Override
	public Object transformValueFromResource(Object value, SysSchemaAttributeHandling attributeHandling,  List<IcfAttribute> icfAttributes ){
		Assert.notNull(attributeHandling);
		
		if(attributeHandling.getTransformFromResourceScript() != null){
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, attributeHandling.getSystemEntityHandling().getSystem());
			variables.put(ICF_ATTRIBUTES_KEY, icfAttributes);
			return groovyScriptService.evaluate(attributeHandling.getTransformFromResourceScript(), variables);
		}

		return value;
	}
	
	@Override
	public SysSchemaAttributeHandling save(SysSchemaAttributeHandling entity) {
		// We will do script validation (on compilation errors), before save attribute handling
		
		if(entity.getTransformFromResourceScript() != null){
			groovyScriptService.validateScript(entity.getTransformFromResourceScript());
		}
		if(entity.getTransformToResourceScript() != null){
			groovyScriptService.validateScript(entity.getTransformToResourceScript());
		}
		return super.save(entity);
	}
	
}
