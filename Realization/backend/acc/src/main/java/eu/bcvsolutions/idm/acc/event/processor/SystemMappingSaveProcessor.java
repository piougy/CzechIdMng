package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.acc.domain.AccResultCode;
import eu.bcvsolutions.idm.acc.domain.SystemEntityType;
import eu.bcvsolutions.idm.acc.domain.SystemOperationType;
import eu.bcvsolutions.idm.acc.dto.SysSchemaObjectClassDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.SysSystemMappingFilter;
import eu.bcvsolutions.idm.acc.entity.SysSchemaObjectClass_;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysSchemaObjectClassService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;

/**
 * Processor for save {@link SysSystemMappingDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("accSystemMappingSaveProcessor")
@Description("Save system mapping, also check duplicity. Cannot be disabled.")
public class SystemMappingSaveProcessor extends CoreEventProcessor<SysSystemMappingDto> {

	private static final String PROCESSOR_NAME = "system-mapping-save-processor";
	
	private final SysSystemMappingService systemMappingService;
	private final SysSchemaObjectClassService schemaObjectClassService;
	
	@Autowired
	public SystemMappingSaveProcessor(
			SysSystemMappingService systemMappingService,
			SysSchemaObjectClassService schemaObjectClassService) {
		super(SystemMappingEventType.CREATE, SystemMappingEventType.UPDATE);
		//
		Assert.notNull(systemMappingService);
		Assert.notNull(schemaObjectClassService);
		//
		this.systemMappingService = systemMappingService;
		this.schemaObjectClassService = schemaObjectClassService;
		
	}
	
	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto dto = event.getContent();
		// it is not possible get schema from embedded - new entity
		SysSchemaObjectClassDto schema = schemaObjectClassService.get(dto.getObjectClass());
		SysSystemDto system = DtoUtils.getEmbedded(schema, SysSchemaObjectClass_.system, SysSystemDto.class);
		//
		// for tree type is possible has more than one provisioning, both only for one tree type
		if (dto.getOperationType() == SystemOperationType.PROVISIONING) {
			// check if exists mapping
			List<SysSystemMappingDto> anotherMapping = getMapping(dto.getEntityType(), schema.getSystem(), dto.getTreeType());
			// if list not empty throw error with duplicate mapping
			if (anotherMapping
					.stream()
					.filter(mapping -> { 
						return !mapping.getId().equals(dto.getId()); 
					})
					.findFirst()
					.isPresent()) {
				throw new ResultCodeException(
						AccResultCode.SYSTEM_MAPPING_FOR_ENTITY_EXISTS,
						ImmutableMap.of(
								"system", system.getName(), 
								"entityType", dto.getEntityType()));
			}			
		}
		//
		SysSystemMappingDto result = systemMappingService.saveInternal(dto);
		// update content
		event.setContent(result);
		//
		return new DefaultEventResult<>(event, this);
	}
	
	private List<SysSystemMappingDto> getMapping(SystemEntityType entityType, UUID systemId, UUID treeTypeId) {
		SysSystemMappingFilter filter = new SysSystemMappingFilter();
		filter.setEntityType(entityType);
		filter.setTreeTypeId(treeTypeId);
		filter.setOperationType(SystemOperationType.PROVISIONING);
		filter.setSystemId(systemId);
		return systemMappingService.find(filter, null).getContent();
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
