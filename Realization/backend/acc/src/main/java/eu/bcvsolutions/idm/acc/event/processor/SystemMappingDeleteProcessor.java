package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.acc.dto.filter.RoleSystemFilter;
import eu.bcvsolutions.idm.acc.dto.filter.SystemAttributeMappingFilter;
import eu.bcvsolutions.idm.acc.event.SystemMappingEvent.SystemMappingEventType;
import eu.bcvsolutions.idm.acc.service.api.SysRoleSystemService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemAttributeMappingService;
import eu.bcvsolutions.idm.acc.service.api.SysSystemMappingService;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Processor remove all handled attributes for {@link SysSystemMappingDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Component("accSystemMappingDeleteProcessor")
@Description("Remove all handled attributes. Ensures referential integrity. Cannot be disabled.")
public class SystemMappingDeleteProcessor extends CoreEventProcessor<SysSystemMappingDto> {

	private static final String PROCESSOR_NAME = "system-mapping-delete-processor";
	
	private final SysSystemAttributeMappingService systemAttributeMappingService;
	private final SysRoleSystemService roleSystemService;
	private final SysSystemMappingService systemMappingService;
	
	@Autowired
	public SystemMappingDeleteProcessor(
			SysSystemAttributeMappingService systemAttributeMappingService,
			SysRoleSystemService roleSystemService,
			SysSystemMappingService systemMappingService) {
		super(SystemMappingEventType.DELETE);
		//
		Assert.notNull(roleSystemService);
		Assert.notNull(systemAttributeMappingService);
		Assert.notNull(systemMappingService);
		//
		this.roleSystemService = roleSystemService;
		this.systemAttributeMappingService = systemAttributeMappingService;
		this.systemMappingService = systemMappingService;
	}

	@Override
	public EventResult<SysSystemMappingDto> process(EntityEvent<SysSystemMappingDto> event) {
		SysSystemMappingDto systemMapping = event.getContent();
		//
		// remove all handled attributes
		SystemAttributeMappingFilter filter = new SystemAttributeMappingFilter();
		filter.setSystemMappingId(systemMapping.getId());
		systemAttributeMappingService.find(filter, null).forEach(systemAttributeMapping -> {
			systemAttributeMappingService.delete(systemAttributeMapping);
		});
		//
		// delete mapped roles
		RoleSystemFilter roleSystemFilter = new RoleSystemFilter();
		roleSystemFilter.setSystemMappingId(systemMapping.getId());
		roleSystemService.find(roleSystemFilter, null).forEach(roleSystem -> {
			roleSystemService.delete(roleSystem);
		});
		//
		systemMappingService.deleteInternal(systemMapping);
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
