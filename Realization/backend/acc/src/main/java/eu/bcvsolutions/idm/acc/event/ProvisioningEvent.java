package eu.bcvsolutions.idm.acc.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for provisioning
 * 
 * @author Svanda
 *
 */
public class ProvisioningEvent extends CoreEvent<AccAccountDto> {

	public static final int DEFAULT_PROVISIONING_ORDER = 1000;
	public static final int DEFAULT_PASSWORD_VALIDATION_ORDER = -1000;

	private static final long serialVersionUID = 1L;
	
	
	/**
	 * Supported identity events
	 *
	 */
	public enum ProvisioningEventType implements EventType {
		START;
	}
	
	public ProvisioningEvent(ProvisioningEventType operation, AccAccountDto content) {
		super(operation, content);
	}
	
	public ProvisioningEvent(ProvisioningEventType operation, AccAccountDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}