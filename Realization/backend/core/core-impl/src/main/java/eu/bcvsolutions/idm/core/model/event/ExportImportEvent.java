package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmExportImportDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for export-import
 * 
 * @author Vít Švanda
 *
 */
public class ExportImportEvent extends CoreEvent<IdmExportImportDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum ExportImportEventType implements EventType {
		CREATE, UPDATE, DELETE
	}
	
	public ExportImportEvent(ExportImportEventType operation, IdmExportImportDto content) {
		super(operation, content);
	}
	
	public ExportImportEvent(ExportImportEventType operation, IdmExportImportDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}