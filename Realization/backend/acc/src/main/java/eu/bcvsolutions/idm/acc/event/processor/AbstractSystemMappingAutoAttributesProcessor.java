package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemAttributeMappingDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSchemaAttributeFilter;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventType;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
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
	protected void createAttributeMappingBySchemaAttribute(SysSystemMappingDto dto, SysSchemaAttributeDto schemaAttribute, String propertyName, boolean isUID) {
		SysSystemAttributeMappingDto mappingAttribute = new SysSystemAttributeMappingDto();
		mappingAttribute.setIdmPropertyName(propertyName);
		mappingAttribute.setSchemaAttribute(schemaAttribute.getId());
		mappingAttribute.setUid(isUID);
		mappingAttribute.setSystemMapping(dto.getId());
		mappingAttribute.setCached(true);
		mappingAttribute.setEntityAttribute(true);
		mappingAttribute.setName(schemaAttribute.getName().trim());

		systemAttributeMappingService.save(mappingAttribute);
	}

	/**
	 * Find schema attributes for given schema.
	 */
	protected List<SysSchemaAttributeDto> getSchemaAttributes(UUID schemaId) {
		SysSchemaAttributeFilter schemaAttributeFilter = new SysSchemaAttributeFilter();
		schemaAttributeFilter.setObjectClassId(schemaId);

		List<SysSchemaAttributeDto> schemaAttributes = schemaAttributeService.find(schemaAttributeFilter, null).getContent();
		return schemaAttributes;
	}
}
