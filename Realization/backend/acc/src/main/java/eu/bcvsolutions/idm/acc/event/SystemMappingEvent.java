package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSystemMappingDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link SysSystemMappingDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SystemMappingEvent  extends CoreEvent<SysSystemMappingDto> {

	private static final long serialVersionUID = -2993824895457334827L;

	/**
	 * 
	 * Supported schema attribute event
	 *
	 */
	public enum SystemMappingEventType implements EventType {
		DELETE;
	}
	
	public SystemMappingEvent(SystemMappingEventType operation, SysSystemMappingDto content) {
		super(operation, content);
	}
	
	public SystemMappingEvent(SystemMappingEventType operation, SysSystemMappingDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
