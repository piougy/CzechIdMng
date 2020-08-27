package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.AccUniformPasswordDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for {@link AccUniformPasswordDto}
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
public class UniformPasswordEvent extends CoreEvent<AccUniformPasswordDto> {

	private static final long serialVersionUID = 1L;
	
	/**
	 * Supported uniform password events
	 *
	 */
	public enum UniformPasswordEventType implements EventType {
		CREATE, 
		UPDATE, 
		DELETE;
	}
	
	public UniformPasswordEvent(UniformPasswordEventType operation, AccUniformPasswordDto content) {
		super(operation, content);
	}
	
	public UniformPasswordEvent(UniformPasswordEventType operation, AccUniformPasswordDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}