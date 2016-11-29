package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SchemaAttributeHandlingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttributeHandling;
import eu.bcvsolutions.idm.acc.entity.SysSystemEntityHandling;
import eu.bcvsolutions.idm.acc.repository.SysSchemaAttributeHandlingRepository;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeHandlingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.repository.AbstractEntityRepository;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.eav.service.api.FormService;
import eu.bcvsolutions.idm.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.icf.api.IcfAttribute;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Default schema attributes handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSchemaAttributeHandlingService
		extends AbstractReadWriteEntityService<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter>
		implements SysSchemaAttributeHandlingService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DefaultSysSchemaAttributeHandlingService.class);

	private SysSchemaAttributeHandlingRepository repository;
	private GroovyScriptService groovyScriptService;
	private FormService formService;
	private IdmFormAttributeService formAttributeService;
	private SysSystemService systemService;

	@Autowired
	public DefaultSysSchemaAttributeHandlingService(SysSchemaAttributeHandlingRepository repository,
			GroovyScriptService groovyScriptService, FormService formService,
			IdmFormAttributeService formAttributeService, SysSystemService systemService) {
		super();
		Assert.notNull(repository);
		Assert.notNull(groovyScriptService);
		Assert.notNull(formService);
		Assert.notNull(formAttributeService);
		Assert.notNull(systemService);

		this.formService = formService;
		this.repository = repository;
		this.groovyScriptService = groovyScriptService;
		this.formAttributeService = formAttributeService;
		this.systemService = systemService;
	}

	@Override
	protected AbstractEntityRepository<SysSchemaAttributeHandling, SchemaAttributeHandlingFilter> getRepository() {
		return repository;
	}

	public List<SysSchemaAttributeHandling> findByEntityHandling(SysSystemEntityHandling entityHandling) {
		Assert.notNull(entityHandling);

		SchemaAttributeHandlingFilter filter = new SchemaAttributeHandlingFilter();
		filter.setEntityHandlingId(entityHandling.getId());
		Page<SysSchemaAttributeHandling> page = repository.find(filter, null);
		return page.getContent();
	}

	@Override
	public Object transformValueToResource(Object value, SysSchemaAttributeHandling attributeHandling,
			AbstractEntity entity) {
		Assert.notNull(attributeHandling);

		if (attributeHandling.getTransformToResourceScript() != null) {
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, attributeHandling.getSystemEntityHandling().getSystem());
			variables.put(ENTITY_KEY, entity);
			return groovyScriptService.evaluate(attributeHandling.getTransformToResourceScript(), variables);
		}

		return value;
	}

	@Override
	public Object transformValueFromResource(Object value, SysSchemaAttributeHandling attributeHandling,
			List<IcfAttribute> icfAttributes) {
		Assert.notNull(attributeHandling);

		if (attributeHandling.getTransformFromResourceScript() != null) {
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
		// We will do script validation (on compilation errors), before save
		// attribute handling

		if (entity.getTransformFromResourceScript() != null) {
			groovyScriptService.validateScript(entity.getTransformFromResourceScript());
		}
		if (entity.getTransformToResourceScript() != null) {
			groovyScriptService.validateScript(entity.getTransformToResourceScript());
		}
		if (entity.isExtendedAttribute()) {
			IdmFormDefinition definition = formService.getDefinition(entity.getSystemEntityHandling().getEntityType().getEntityType().getCanonicalName());
			if (definition != null) {
				IdmFormAttribute defAttribute = definition.getMappedAttributeByName(entity.getIdmPropertyName());
				if (defAttribute == null) {
					log.info(MessageFormat.format(
							"IdmFormAttribute for identity and property {0} not found. We will create definition now.",
							entity.getIdmPropertyName()));

					IdmFormAttribute attributeDefinition = convertSchemaAttributeHandling(entity, definition);

					definition.getFormAttributes().add(attributeDefinition);
					formAttributeService.save(attributeDefinition);
				}
			}
		}
		return super.save(entity);
	}

	/**
	 * Convert schema attribute handling to Form attribute
	 * 
	 * @param entity
	 * @param definition
	 * @return
	 */
	private IdmFormAttribute convertSchemaAttributeHandling(SysSchemaAttributeHandling entity,
			IdmFormDefinition definition) {

		SysSchemaAttribute schemaAttribute = entity.getSchemaAttribute();
		IdmFormAttribute attributeDefinition = new IdmFormAttribute();
		attributeDefinition.setSeq((short) 0);
		attributeDefinition.setName(entity.getIdmPropertyName());
		attributeDefinition.setDisplayName(entity.getIdmPropertyName());
		attributeDefinition.setPersistentType(systemService.convertPropertyType(schemaAttribute.getClassType()));
		attributeDefinition.setRequired(schemaAttribute.isRequired());
		attributeDefinition.setMultiple(schemaAttribute.isMultivalued());
		attributeDefinition.setReadonly(!schemaAttribute.isUpdateable());
		attributeDefinition.setConfidential(schemaAttribute.getClassType().equals(GuardedString.class.getName()));
		attributeDefinition.setFormDefinition(definition);
		attributeDefinition.setDescription(
				MessageFormat.format("Genereted by schema attribute {0} in resource {1}. Created by SYSTEM.",
						schemaAttribute.getName(), schemaAttribute.getObjectClass().getSystem().getName()));
		return attributeDefinition;
	}

}
