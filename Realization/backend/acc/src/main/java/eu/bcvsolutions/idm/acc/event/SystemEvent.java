package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link SysSystemDto}
 * 
 * @author svandav
 *
 */

public class SystemEvent extends CoreEvent<SysSystemDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * 
	 * Supported schema attribute event
	 *
	 */
	public enum SystemEventType implements EventType {
		CREATE, UPDATE, DELETE, EAV_SAVE;
	}

	public SystemEvent(SystemEventType operation, SysSystemDto content) {
		super(operation, content);
	}

	public SystemEvent(SystemEventType operation, SysSystemDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
