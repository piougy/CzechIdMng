package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleCatalogue_;

/**
 * Processor for automatic creation of role catalogue mapped attributes by common schema attributes.
 *
 * @author Vít Švanda
 */
@Component("accSystemMappingRoleCatalogueAutoAttributesProcessor")
@Description("Processor for automatic creation of identity mapped attributes by common schema attributes.")
public class SystemMappingRoleCatalogueAutoAttributesProcessor extends AbstractSystemMappingAutoAttributesProcessor {

	private static final String PROCESSOR_NAME = "system-mapping-auto-role-catalogue-attributes-processor";

	@Autowired
	public SystemMappingRoleCatalogueAutoAttributesProcessor() {
		super(SystemMappingEventType.CREATE);
	}

	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto dto = event.getContent();
		UUID schemaId = dto.getObjectClass();
		if (schemaId == null) {
			return new DefaultEventResult<>(event, this);
		}

		List<SysSchemaAttributeDto> schemaAttributes = getSchemaAttributes(schemaId);

		// UID - code attribute
		SysSchemaAttributeDto primarySchemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getPrimaryKeyCatalogue());
		if (primarySchemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, primarySchemaAttribute, IdmRoleCatalogue_.code.getName(), true);
		}

		// Name attribute
		SysSchemaAttributeDto schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getNameCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmRoleCatalogue_.name.getName(), false);
		}

		// Description attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getDescriptionCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmRoleCatalogue_.description.getName(), false);
		}

		// Parent attribute
		schemaAttribute = getSchemaAttributeByCatalogue(schemaAttributes, this.getParentCatalogue());
		if (schemaAttribute != null) {
			createAttributeMappingBySchemaAttribute(dto, schemaAttribute, IdmRoleCatalogue_.parent.getName(), false);
		}

		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	SystemEntityType getSystemEntityType() {
		return SystemEntityType.ROLE_CATALOGUE;
	}
	

	/**
	 * Code catalogue for primary key (UID). Order in the catalogue is use in search.
	 */
	private Set<String> getPrimaryKeyCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("__NAME__");
		catalogue.add("__UID__");
		catalogue.add("code");
		catalogue.add("id");

		return catalogue;
	}

	/**
	 * Code catalogue for name. Order in the catalogue is use in search.
	 */
	private Set<String> getNameCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("name");
		catalogue.add("title");
		catalogue.add("displayname");
		catalogue.add("display_name");
		catalogue.add("label");

		return catalogue;
	}

	/**
	 * Code catalogue for parent. Order in the catalogue is use in search.
	 */
	private Set<String> getParentCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("parent");

		return catalogue;
	}

	/**
	 * Code catalogue for description. Order in the catalogue is use in search.
	 */
	private Set<String> getDescriptionCatalogue() {
		Set<String> catalogue = Sets.newLinkedHashSet();
		catalogue.add("description");
		catalogue.add("notes");
		catalogue.add("note");

		return catalogue;
	}



	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER + 20;
	}

}
