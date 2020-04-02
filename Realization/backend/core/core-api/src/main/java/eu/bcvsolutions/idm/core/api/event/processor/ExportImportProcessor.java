package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Export-import processors should implement this interface.
 * 
 * @author Vít Švanda
 *
 */
public interface ExportImportProcessor extends EntityEventProcessor<IdmExportImportDto> {
	
	
}
