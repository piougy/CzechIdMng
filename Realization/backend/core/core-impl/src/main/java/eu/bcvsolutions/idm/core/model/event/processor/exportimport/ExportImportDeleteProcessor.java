package eu.bcvsolutions.idm.core.model.event.processor.exportimport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ExportImportProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmExportImportService;
import eu.bcvsolutions.idm.core.model.event.ExportImportEvent.ExportImportEventType;

/**
 * Processor for delete export-import
 * 
 * @author Vít Švanda
 *
 */
@Component(ExportImportDeleteProcessor.PROCESSOR_NAME)
@Description("Processor for delete export-import")
public class ExportImportDeleteProcessor extends CoreEventProcessor<IdmExportImportDto> implements ExportImportProcessor {
	public static final String PROCESSOR_NAME = "export-import-delete-processor";

	private final IdmExportImportService service;

	@Autowired
	public ExportImportDeleteProcessor(IdmExportImportService service) {
		super(ExportImportEventType.DELETE);
		//
		Assert.notNull(service, "Service is required.");
		//
		this.service = service;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmExportImportDto> process(EntityEvent<IdmExportImportDto> event) {
		IdmExportImportDto dto = event.getContent();
		// Internal delete
		service.deleteInternal(dto);
		
		return new DefaultEventResult<>(event, this);
	}
}
