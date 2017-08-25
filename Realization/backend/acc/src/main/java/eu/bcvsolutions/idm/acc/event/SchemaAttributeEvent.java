package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysSchemaAttributeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link SysSchemaAttributeDto}
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

public class SchemaAttributeEvent extends CoreEvent<SysSchemaAttributeDto> {

	private static final long serialVersionUID = -1775099897461328488L;

	/**
	 * 
	 * Supported schema attribute event
	 *
	 */
	public enum SchemaAttributeEventType implements EventType {
		DELETE;
	}
	
	public SchemaAttributeEvent(SchemaAttributeEventType operation, SysSchemaAttributeDto content) {
		super(operation, content);
	}
	
	public SchemaAttributeEvent(SchemaAttributeEventType operation, SysSchemaAttributeDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
