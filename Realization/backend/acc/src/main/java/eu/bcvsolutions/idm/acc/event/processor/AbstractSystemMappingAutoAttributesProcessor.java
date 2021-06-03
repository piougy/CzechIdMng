package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.domain.AttributeMappingStrategyType;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.ic.api.IcAttributeInfo;
import eu.bcvsolutions.idm.ic.api.IcObjectClassInfo;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract processor for automatic creation of a mapped attributes by common schema attributes.
 *
 * @author Vít Švanda
 */

public abstract class AbstractSystemMappingAutoAttributesProcessor extends CoreEventProcessor<SysSystemMappingDto> {

	@Autowired
	private SysSystemAttributeMappingService systemAttributeMappingService;
	@Autowired
	private SysSchemaAttributeService schemaAttributeService;
	@Autowired
	private LookupService lookupService;

	public AbstractSystemMappingAutoAttributesProcessor(EventType... type) {
		super(type);
	}

	/**
	 * Search for an schema attribute by given catalogue (and by order in the catalogue).
	 */
	protected SysSchemaAttributeDto getSchemaAttributeByCatalogue(List<SysSchemaAttributeDto> schemaAttributes, Set<String> catalogue) {
		// First search for all schema attribute for all catalog.
		List<SysSchemaAttributeDto> attributes = schemaAttributes
				.stream()
				.filter(attribute -> catalogue
						.stream()
						.anyMatch(
								key -> attribute.getClassType().equals(String.class.getCanonicalName())
										&& attribute.getName().trim().equalsIgnoreCase(key)
						))
				.collect(Collectors.toList());
		// Find first by order in catalogue.
		SysSchemaAttributeDto resultAttribute = null;
		for (String key : catalogue) {
			resultAttribute = attributes
					.stream()
					.filter(attribute -> attribute.getName().trim().equalsIgnoreCase(key))
					.findFirst()
					.orElse(null);
			if (resultAttribute != null) {
				break;
			}
		}
		return resultAttribute;
	}

	/**
	 * Create and save mapped attribute by schema attribute.
	 */
	protected SysSystemAttributeMappingDto createAttributeMappingBySchemaAttribute(SysSystemMappingDto dto, SysSchemaAttributeDto schemaAttribute, String propertyName, boolean isUID) {
		SysSystemAttributeMappingDto mappingAttribute = new SysSystemAttributeMappingDto();
		if (Strings.isBlank(propertyName)) {
			mappingAttribute.setEntityAttribute(false);
			mappingAttribute.setIdmPropertyName(null);
		} else {
			mappingAttribute.setEntityAttribute(true);
			mappingAttribute.setIdmPropertyName(propertyName);
		}
		if (schemaAttribute.isMultivalued()) {
			mappingAttribute.setStrategyType(AttributeMappingStrategyType.AUTHORITATIVE_MERGE);
		}
		mappingAttribute.setSchemaAttribute(schemaAttribute.getId());
		mappingAttribute.setUid(isUID);
		mappingAttribute.setSystemMapping(dto.getId());
		mappingAttribute.setCached(true);
		mappingAttribute.setName(schemaAttribute.getName().trim());

		return systemAttributeMappingService.save(mappingAttribute);
	}

	/**
	 * Specific method for password attribute mapping  
	 */
	protected SysSystemAttributeMappingDto createAttributeMappingForPassword(SysSystemMappingDto dto, List<SysSchemaAttributeDto> schemaAttributes) {
		SysSchemaAttributeDto passwordSchemaAttr = schemaAttributes.stream().filter(attr -> {
			return IcAttributeInfo.PASSWORD.equals(attr.getName())
					&& GuardedString.class.getCanonicalName().equals(attr.getClassType());
		}).findFirst().orElse(null);

		SysSystemAttributeMappingDto mappingAttribute = null;
		if (passwordSchemaAttr != null) {
			mappingAttribute = createAttributeMappingBySchemaAttribute(dto, passwordSchemaAttr, null, false);
			mappingAttribute.setPasswordAttribute(true);
			return systemAttributeMappingService.save(mappingAttribute);
		}
		return mappingAttribute;
	}

	/**
	 * Create and save mapped attribute by schema attribute and given script to system.
	 */
	protected SysSystemAttributeMappingDto createAttributeMappingByScriptToResource(SysSystemMappingDto dto, SysSchemaAttributeDto schemaAttribute, String script) {
		return createAttributeMappingByScriptToResource(dto, schemaAttribute, script, true);
	}

	/**
	 * Create and save mapped attribute by schema attribute and given script to system.
	 */
	protected SysSystemAttributeMappingDto createAttributeMappingByScriptToResource(
			SysSystemMappingDto dto,
			SysSchemaAttributeDto schemaAttribute,
			String script,
			boolean toResource) {
		SysSystemAttributeMappingDto mappingAttribute = new SysSystemAttributeMappingDto();
		mappingAttribute.setSchemaAttribute(schemaAttribute.getId());
		mappingAttribute.setUid(false);
		if (toResource) {
			mappingAttribute.setTransformToResourceScript(script);
		}else {
			mappingAttribute.setTransformFromResourceScript(script);
		}
		mappingAttribute.setSystemMapping(dto.getId());
		mappingAttribute.setCached(true);
		mappingAttribute.setEntityAttribute(false);
		mappingAttribute.setName(schemaAttribute.getName().trim());

		return systemAttributeMappingService.save(mappingAttribute);
	}

	/**
	 * Find schema attributes for given schema.
	 */
	protected List<SysSchemaAttributeDto> getSchemaAttributes(UUID schemaId) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemaId);

		return schemaAttributeService.find(schemaAttributeFilter, null).getContent();
	}

	abstract SystemEntityType getSystemEntityType();

	/**
	 * Attributes will be generated only for defined operation type.
	 */
	protected SystemOperationType getSystemOperationType() {
		return SystemOperationType.PROVISIONING;
	}

	/**
	 * Attributes will be generated only for defined schema type.
	 */
	protected String getSchemaType() {
		return IcObjectClassInfo.ACCOUNT;
	}

	@Override
	public boolean conditional(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto systemMappingDto = event.getContent();
		if (getSystemEntityType() != systemMappingDto.getEntityType()) {
			return false;
		}
		// Attributes will be generated only for defined operation type.
		if (getSystemOperationType() != systemMappingDto.getOperationType()) {
			return false;
		}
		// Attributes will be generated only for defined schema.
		SysSchemaObjectClassDto objectClassDto = lookupService.lookupEmbeddedDto(systemMappingDto, SysSystemMapping_.objectClass.getName());
		if (!getSchemaType().equals(objectClassDto.getObjectClassName())) {
			return false;
		}
		if (event.getBooleanProperty(SysSystemMappingService.ENABLE_AUTOMATIC_CREATION_OF_MAPPING)) {
			return super.conditional(event);
		}
		return false;
	}
}
