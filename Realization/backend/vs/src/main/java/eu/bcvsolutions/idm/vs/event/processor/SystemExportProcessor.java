package eu.bcvsolutions.idm.vs.event.processor;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.acc.event.SystemEvent.SystemEventType;
import eu.bcvsolutions.idm.acc.event.processor.SystemProcessor;
import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.vs.service.api.VsSystemService;

/**
 * Export VS system - exports specific parts of VS.
 * 
 *  
 * @author Ondrej Husnik
 * @since 11.1.0
 */
@Component(SystemExportProcessor.PROCESSOR_NAME)
@Description("Export system - virtual system specific export.")
public class SystemExportProcessor extends AbstractEntityEventProcessor<SysSystemDto> implements SystemProcessor {
	
	public static final String PROCESSOR_NAME = "vs-system-export-processor";

	@Autowired
	private VsSystemService vsSystemService;
	
	public SystemExportProcessor() {
		super(SystemEventType.EXPORT);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<SysSystemDto> event) {
		SysSystemDto system = event.getContent();
		Assert.notNull(system, "System is required.");
		return system.isVirtual();
	}

	@Override
	public EventResult<SysSystemDto> process(EntityEvent<SysSystemDto> event) {
		SysSystemDto system = event.getContent();
		Assert.notNull(system, "System have to be set.");
		Assert.notNull(system.getId(), "System id has to be set.");
		
		Map<String,Serializable> properties = event.getProperties();
		Serializable batchProp = properties.get(SystemProcessor.EXPORT_BATCH_PROPERTY);
		Assert.isTrue(batchProp instanceof IdmExportImportDto, "Export import batch has to be supplied.");
		IdmExportImportDto batch = (IdmExportImportDto) batchProp;
		vsSystemService.exportVsDefinition(system.getId(), batch);

		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return 100;
	}	
}
