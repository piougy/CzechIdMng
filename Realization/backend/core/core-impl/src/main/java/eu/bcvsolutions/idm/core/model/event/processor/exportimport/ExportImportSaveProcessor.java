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
 * Processor for save export-import
 * 
 * @author Vít Švanda
 *
 */
@Component(ExportImportSaveProcessor.PROCESSOR_NAME)
@Description("Processor for save export-import")
public class ExportImportSaveProcessor extends CoreEventProcessor<IdmExportImportDto> implements ExportImportProcessor {
	public static final String PROCESSOR_NAME = "export-import-save-processor";

	private final IdmExportImportService service;

	@Autowired
	public ExportImportSaveProcessor(IdmExportImportService service) {
		super(ExportImportEventType.CREATE, ExportImportEventType.UPDATE);
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
		dto = service.saveInternal(dto);
		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}
}
