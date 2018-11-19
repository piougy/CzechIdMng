package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysAttributeControlledValueDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for system attribute controlled values
 * 
 * @author Svanda
 *
 */
public class AttributeControlledValueEvent extends CoreEvent<SysAttributeControlledValueDto> {

	private static final long serialVersionUID = 1L;

	public enum AttributeControlledValueEventType implements EventType {
		CREATE, UPDATE, DELETE;
	}

	public AttributeControlledValueEvent(AttributeControlledValueEventType operation, SysAttributeControlledValueDto content) {
		super(operation, content);
	}

	public AttributeControlledValueEvent(AttributeControlledValueEventType operation, SysAttributeControlledValueDto content,
			Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}