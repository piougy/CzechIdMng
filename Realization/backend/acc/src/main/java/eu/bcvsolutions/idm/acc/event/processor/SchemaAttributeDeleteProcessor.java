package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.event.SchemaAttributeEvent.SchemaAttributeEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaAttributeService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Processor for delete {@link SysSchemaAttributeDto} also ensures referential integrity.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("accSchemaAttributeDeleteProcessor")
@Description("Remove all handled attributes. Ensures referential integrity. Cannot be disabled.")
public class SchemaAttributeDeleteProcessor extends CoreEventProcessor<SysSchemaAttributeDto> {

	private static final String PROCESSOR_NAME = "schema-attribute-delete-processor";
	
	private final SysSystemAttributeMappingService systeAttributeMappingService;
	private final SysSchemaAttributeService schemaAttributeService;
	
	@Autowired
	public SchemaAttributeDeleteProcessor(
			SysSystemAttributeMappingService systeAttributeMappingService,
			SysSchemaAttributeService schemaAttributeService) {
		super(SchemaAttributeEventType.DELETE);
		//
		Assert.notNull(systeAttributeMappingService);
		Assert.notNull(schemaAttributeService);
		//
		this.systeAttributeMappingService = systeAttributeMappingService;
		this.schemaAttributeService = schemaAttributeService;
	}
	
	@Override
	public EventResult<SysSchemaAttributeDto> process(EntityEvent<SysSchemaAttributeDto> event) {
		SysSchemaAttributeDto schemaAttribute = event.getContent();
		// remove all handled attributes
		SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());
		systeAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systeAttributeMappingService.delete(systemAttributeMapping);
		});
		//
		schemaAttributeService.deleteInternal(schemaAttribute);
		//
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER;
	}
	
	@Override
	public boolean isDisableable() {
		return false;
	}
}
