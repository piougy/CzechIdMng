package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.SysConnectorServerDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Remote server event.
 * 
 * @author Radek Tomiška
 * @since 10.8.0
 */
public class RemoteServerEvent extends CoreEvent<SysConnectorServerDto> {

	private static final long serialVersionUID = 1L;

	public enum RemoteServerEventType implements EventType {
		CREATE, UPDATE, DELETE;
	}

	public RemoteServerEvent(RemoteServerEventType operation, SysConnectorServerDto content) {
		super(operation, content);
	}

	public RemoteServerEvent(RemoteServerEventType operation, SysConnectorServerDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}
}
