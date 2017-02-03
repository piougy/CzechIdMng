package eu.bcvsolutions.idm.acc.service.impl;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.AttributeMapping;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaAttribute;
import eu.bcvsolutions.idm.acc.entity.SysSystem;
import eu.bcvsolutions.idm.acc.entity.SysSystemAttributeMapping;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping;
import eu.bcvsolutions.idm.acc.exception.ProvisioningException;
import eu.bcvsolutions.idm.acc.repository.SysRoleSystemAttributeRepository;
import eu.bcvsolutions.idm.acc.repository.SysSystemAttributeMappingRepository;
import eu.bcvsolutions.idm.acc.service.api.FormPropertyManager;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.GroovyScriptService;
import eu.bcvsolutions.idm.core.eav.api.entity.FormableEntity;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormAttribute;
import eu.bcvsolutions.idm.core.eav.entity.IdmFormDefinition;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.eav.service.api.IdmFormAttributeService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttribute;
import eu.bcvsolutions.idm.ic.impl.IcAttributeImpl;
import eu.bcvsolutions.idm.ic.impl.IcPasswordAttributeImpl;
import eu.bcvsolutions.idm.ic.service.api.IcConnectorFacade;

/**
 * Default schema attributes handling
 * 
 * @author svandav
 *
 */
@Service
public class DefaultSysSystemAttributeMappingService
		extends AbstractReadWriteEntityService<SysSystemAttributeMapping, SystemAttributeMappingFilter>
		implements SysSystemAttributeMappingService {

	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
			.getLogger(DefaultSysSystemAttributeMappingService.class);

	private final SysSystemAttributeMappingRepository repository;
	private final GroovyScriptService groovyScriptService;
	private final FormService formService;
	private final IdmFormAttributeService formAttributeService;
	private final SysRoleSystemAttributeRepository roleSystemAttributeRepository;
	private final FormPropertyManager formPropertyManager;
	
	@Autowired
	public DefaultSysSystemAttributeMappingService(
			SysSystemAttributeMappingRepository repository,
			GroovyScriptService groovyScriptService, 
			FormService formService,
			IdmFormAttributeService formAttributeService, 
			SysRoleSystemAttributeRepository roleSystemAttributeRepository,
			FormPropertyManager formPropertyManager) {
		super(repository);
		//
		Assert.notNull(groovyScriptService);
		Assert.notNull(formService);
		Assert.notNull(formAttributeService);
		Assert.notNull(roleSystemAttributeRepository);
		Assert.notNull(formPropertyManager);
		//
		this.formService = formService;
		this.repository = repository;
		this.groovyScriptService = groovyScriptService;
		this.formAttributeService = formAttributeService;
		this.roleSystemAttributeRepository = roleSystemAttributeRepository;
		this.formPropertyManager = formPropertyManager;
		
	}

	public List<SysSystemAttributeMapping> findBySystemMapping(SysSystemMapping systemMapping) {
		Assert.notNull(systemMapping);
		//
		SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMapping.getId());
		Page<SysSystemAttributeMapping> page = repository.find(filter, null);
		return page.getContent();
	}

	@Override
	public Object transformValueToResource(Object value, AttributeMapping attributeMapping,
			AbstractEntity entity) {
		Assert.notNull(attributeMapping);
		//
		return transformValueToResource(value, attributeMapping.getTransformToResourceScript(), entity,
				attributeMapping.getSchemaAttribute().getObjectClass().getSystem());
	}

	@Override
	public Object transformValueToResource(Object value, String script, AbstractEntity entity, SysSystem system) {
		if (!StringUtils.isEmpty(script)) {
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, system);
			variables.put(ENTITY_KEY, entity);
			return groovyScriptService.evaluate(script, variables);
		}

		return value;
	}

	@Override
	public Object transformValueFromResource(Object value, AttributeMapping attributeMapping,
			List<IcAttribute> icAttributes) {
		Assert.notNull(attributeMapping);
		//
		return transformValueFromResource(value, attributeMapping.getTransformFromResourceScript(), icAttributes,
				attributeMapping.getSchemaAttribute().getObjectClass().getSystem());
	}

	@Override
	public Object transformValueFromResource(Object value, String script, List<IcAttribute> icAttributes,
			SysSystem system) {

		if (!StringUtils.isEmpty(script)) {
			Map<String, Object> variables = new HashMap<>();
			variables.put(ATTRIBUTE_VALUE_KEY, value);
			variables.put(SYSTEM_KEY, system);
			variables.put(IC_ATTRIBUTES_KEY, icAttributes);
			return groovyScriptService.evaluate(script, variables);
		}

		return value;
	}

	@Override
	@Transactional
	public SysSystemAttributeMapping save(SysSystemAttributeMapping entity) {
		// Check if exist some else attribute which is defined like unique identifier
		if (entity.isUid()) {			
			SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
			filter.setSystemMappingId(entity.getSystemMapping().getId());
			filter.setIsUid(Boolean.TRUE);
			List<SysSystemAttributeMapping> list = this.find(filter, null).getContent();
			
			if (list.size() > 0 && !list.get(0).getId().equals(entity.getId())) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_MORE_UID, ImmutableMap.of("system", entity.getSystemMapping().getSystem().getName()));
			}
		}
		
		// We will do script validation (on compilation errors), before save
		// attribute handling

		if (entity.getTransformFromResourceScript() != null) {
			groovyScriptService.validateScript(entity.getTransformFromResourceScript());
		}
		if (entity.getTransformToResourceScript() != null) {
			groovyScriptService.validateScript(entity.getTransformToResourceScript());
		}
		Class<?> entityType = entity.getSystemMapping().getEntityType().getEntityType();
		if (entity.isExtendedAttribute() && FormableEntity.class.isAssignableFrom(entityType)) {
			createExtendedAttributeDefinition(entity, entityType);
		}
		return super.save(entity);
	}

	/**
	 * Check on exists EAV definition for given attribute. If the definition not exist, then we try create it.
	 * @param entity
	 * @param entityType
	 */
	@Override
	@Transactional
	public void createExtendedAttributeDefinition(AttributeMapping entity, Class<?> entityType) {
		IdmFormDefinition definition = formService
				.getDefinition(entityType.getCanonicalName());
		if (definition != null) {
			IdmFormAttribute defAttribute = definition.getMappedAttributeByName(entity.getIdmPropertyName());
			if (defAttribute == null) {
				log.info(MessageFormat.format(
						"IdmFormAttribute for identity and property {0} not found. We will create definition now.",
						entity.getIdmPropertyName()));

				IdmFormAttribute attributeDefinition = convertMappingAttribute(entity, definition);

				definition.getFormAttributes().add(attributeDefinition);
				formAttributeService.save(attributeDefinition);
			}
		}
	}
	
	@Override
	@Transactional
	public void delete(SysSystemAttributeMapping entity) {
		Assert.notNull(entity);
		// delete attributes
		roleSystemAttributeRepository.deleteBySystemAttributeMapping(entity);
		//
		super.delete(entity);
	}
	
	/**
	 * Create instance of IC attribute for given name. Given idm value will be
	 * transformed to resource.
	 * 
	 * @param attributeMapping
	 * @param idmValue
	 * @return
	 */
	@Override
	public IcAttribute createIcAttribute(AttributeMapping attributeMapping, Object idmValue) {
		SysSchemaAttribute schemaAttribute = attributeMapping.getSchemaAttribute();
		// Check type of value
		try {
			Class<?> classType = Class.forName(schemaAttribute.getClassType());
			
			// If is multivalue and value is list, then we will iterate list and check every item on correct type
			if (schemaAttribute.isMultivalued() && idmValue instanceof List){
				((List<?>)idmValue).stream().forEachOrdered(value ->{
					if (value != null && !(classType.isAssignableFrom(value.getClass()))) {
						throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
								ImmutableMap.of("attribute", attributeMapping.getIdmPropertyName(), "schemaAttributeType",
										schemaAttribute.getClassType(), "valueType", value.getClass().getName()));
					}
				});
				
			// Check single value on correct type
			}else if (idmValue != null && !(classType.isAssignableFrom(idmValue.getClass()))) {
				throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_VALUE_WRONG_TYPE,
						ImmutableMap.of("attribute", attributeMapping.getName(), "schemaAttributeType",
								schemaAttribute.getClassType(), "valueType", idmValue.getClass().getName()));
			}
		} catch (ClassNotFoundException | ProvisioningException e) {
			throw new ProvisioningException(AccResultCode.PROVISIONING_ATTRIBUTE_TYPE_NOT_FOUND,
					ImmutableMap.of("attribute", attributeMapping.getName(), "schemaAttributeType",
							schemaAttribute.getClassType()),
					e);
		}

		IcAttribute icAttributeForUpdate = null;
		if (IcConnectorFacade.PASSWORD_ATTRIBUTE_NAME.equals(schemaAttribute.getName())) {
			// Attribute is password type
			icAttributeForUpdate = new IcPasswordAttributeImpl(schemaAttribute.getName(), (GuardedString) idmValue);

		} else {
			if(idmValue instanceof List){
				@SuppressWarnings("unchecked")
				List<Object> values = (List<Object>)idmValue;
				icAttributeForUpdate = new IcAttributeImpl(schemaAttribute.getName(), values, true);
			}else{
				icAttributeForUpdate = new IcAttributeImpl(schemaAttribute.getName(), idmValue);
			}
		}
		return icAttributeForUpdate;
	}

	/**
	 * Convert schema attribute handling to Form attribute
	 * 
	 * @param entity
	 * @param definition
	 * @return
	 */
	private IdmFormAttribute convertMappingAttribute(AttributeMapping entity,
			IdmFormDefinition definition) {

		SysSchemaAttribute schemaAttribute = entity.getSchemaAttribute();
		IdmFormAttribute attributeDefinition = new IdmFormAttribute();
		attributeDefinition.setSeq((short) 0);
		attributeDefinition.setName(entity.getIdmPropertyName());
		attributeDefinition.setDisplayName(entity.getName());
		attributeDefinition.setPersistentType(formPropertyManager.getPersistentType(schemaAttribute.getClassType()));
		attributeDefinition.setRequired(schemaAttribute.isRequired());
		attributeDefinition.setMultiple(schemaAttribute.isMultivalued());
		attributeDefinition.setReadonly(!schemaAttribute.isUpdateable());
		attributeDefinition.setConfidential(entity.isConfidentialAttribute());
		attributeDefinition.setFormDefinition(definition);
		attributeDefinition.setDescription(
				MessageFormat.format("Genereted by schema attribute {0} in resource {1}. Created by SYSTEM.",
						schemaAttribute.getName(), schemaAttribute.getObjectClass().getSystem().getName()));
		return attributeDefinition;
	}

}
