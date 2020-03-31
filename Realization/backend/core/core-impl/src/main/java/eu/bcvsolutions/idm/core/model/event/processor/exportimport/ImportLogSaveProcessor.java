package eu.bcvsolutions.idm.core.model.event.processor.exportimport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmImportLogDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.ImportLogProcessor;
import eu.bcvsolutions.idm.core.api.service.IdmImportLogService;
import eu.bcvsolutions.idm.core.model.event.ExportImportEvent.ExportImportEventType;

/**
 * Processor for save import log.
 * 
 * @author Vít Švanda
 *
 */
@Component(ImportLogSaveProcessor.PROCESSOR_NAME)
@Description("Processor for save import log")
public class ImportLogSaveProcessor extends CoreEventProcessor<IdmImportLogDto> implements ImportLogProcessor {
	public static final String PROCESSOR_NAME = "import-log-save-processor";

	private final IdmImportLogService service;

	@Autowired
	public ImportLogSaveProcessor(IdmImportLogService service) {
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
	public EventResult<IdmImportLogDto> process(EntityEvent<IdmImportLogDto> event) {
		IdmImportLogDto dto = event.getContent();
		dto = service.saveInternal(dto);
		event.setContent(dto);

		return new DefaultEventResult<>(event, this);
	}
}
