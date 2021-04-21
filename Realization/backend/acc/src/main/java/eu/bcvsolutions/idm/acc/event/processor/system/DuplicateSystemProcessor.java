package eu.bcvsolutions.idm.acc.event.processor.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.event.processor.SystemProcessor;
import eu.bcvsolutions.idm.acc.service.api.SysSystemService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Duplicate system - general system duplication
 * 
 *  
 * @author Ondrej Husnik
 * @since 11.0.0
 */
@Component(DuplicateSystemProcessor.PROCESSOR_NAME)
@Description("Duplicate system - general system duplication. ")
public class DuplicateSystemProcessor extends AbstractEntityEventProcessor<SysSystemDto> implements SystemProcessor {
	
	public static final String PROCESSOR_NAME = "acc-duplicate-system-processor";
	
	@Autowired
	private SysSystemService systemService;
	
	public DuplicateSystemProcessor() {
		super(SystemEventType.DUPLICATE);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {
		SysSystemDto dto = event.getContent();
		Assert.notNull(dto, "System is required.");
		Assert.notNull(dto.getId(), "System id is required.");
		SysSystemDto newDto = systemService.duplicate(dto.getId());		
		event.setContent(newDto);
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -100;
	}

}
