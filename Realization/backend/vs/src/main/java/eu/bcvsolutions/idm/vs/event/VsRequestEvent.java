package eu.bcvsolutions.idm.vs.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.vs.dto.VsRequestDto;

/**
 * Events for virtual system request
 * @author svandav
 *
 */
public class VsRequestEvent extends CoreEvent<VsRequestDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported events
	 *
	 */
	public enum VsRequestEventType implements EventType {
		REALIZE_CREATE, REALIZE_UPDATE, REALIZE_DELETE, EXCECUTE
	}
	
	public VsRequestEvent(VsRequestEventType operation, VsRequestDto content) {
		super(operation, content);
	}
	
	public VsRequestEvent(VsRequestEventType operation, VsRequestDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}