package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmRoleCatalogueDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for role catalogue
 * 
 * @author Radek Tomiška
 *
 */
public class RoleCatalogueEvent extends CoreEvent<IdmRoleCatalogueDto> {
	
	private static final long serialVersionUID = 1L;

	/**
	 * Supported event types
	 */
	public enum RoleCatalogueEventType implements EventType {
		CREATE, UPDATE, DELETE, NOTIFY
	}

	public RoleCatalogueEvent(RoleCatalogueEventType operation, IdmRoleCatalogueDto content) {
		super(operation, content);
	}
	
	public RoleCatalogueEvent(RoleCatalogueEventType operation, IdmRoleCatalogueDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}