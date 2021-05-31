package eu.bcvsolutions.idm.acc.event.processor;

import com.google.common.collect.ImmutableMap;
import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.dto.AbstractSysSyncConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSyncRoleConfigDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.entity.SysSyncRoleConfig_;
import eu.bcvsolutions.idm.acc.entity.SysSystemMapping_;
import eu.bcvsolutions.idm.acc.service.api.SysSyncConfigService;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemAttributeMappingFilter;
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
	private SysSyncConfigService syncConfigService;
	
	@Autowired
	public SchemaAttributeDeleteProcessor(
			SysSystemAttributeMappingService systeAttributeMappingService,
			SysSchemaAttributeService schemaAttributeService) {
		super(SchemaAttributeEventType.DELETE);
		//
		Assert.notNull(systeAttributeMappingService, "Service is required.");
		Assert.notNull(schemaAttributeService, "Service is required.");
		//
		this.systeAttributeMappingService = systeAttributeMappingService;
		this.schemaAttributeService = schemaAttributeService;
	}
	
	@Override
	public EventResult<SysSchemaAttributeDto> process(EntityEvent<SysSchemaAttributeDto> event) {
		SysSchemaAttributeDto schemaAttribute = event.getContent();
		// remove all handled attributes
		SysSystemAttributeMappingFilter filter = new SysSystemAttributeMappingFilter();
		filter.setSchemaAttributeId(schemaAttribute.getId());
		systeAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systeAttributeMappingService.delete(systemAttributeMapping);
		});

		List<AbstractSysSyncConfigDto> syncConfigs = syncConfigService.findRoleConfigByMemberIdentifierAttribute(schemaAttribute.getId());
		if (syncConfigs.size() > 0){
			SysSystemMappingDto systemMappingDto = DtoUtils.getEmbedded(syncConfigs.get(0), SysSyncRoleConfig_.systemMapping, SysSystemMappingDto.class);
			SysSchemaObjectClassDto objectClassDto = DtoUtils.getEmbedded(systemMappingDto, SysSystemMapping_.objectClass, SysSchemaObjectClassDto.class);
			SysSystemDto systemDto = DtoUtils.getEmbedded(objectClassDto, SysSchemaObjectClass_.system, SysSystemDto.class);
			
			throw new ResultCodeException(AccResultCode.ATTRIBUTE_MAPPING_DELETE_FAILED_USED_IN_SYNC,
					ImmutableMap.of("attribute", schemaAttribute.getName(), "system", systemDto.getName()));
		}
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
