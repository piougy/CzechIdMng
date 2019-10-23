package eu.bcvsolutions.idm.core.model.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EventType;

/**
 * Events for code lists
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
public class ConfigurationEvent extends CoreEvent<IdmConfigurationDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported core configuration events
	 *
	 */
	public enum ConfigurationEventType implements EventType {
		CREATE, 
		UPDATE,
		DELETE
	}
	
	public ConfigurationEvent(ConfigurationEventType operation, IdmConfigurationDto content) {
		super(operation, content);
	}
	
	public ConfigurationEvent(ConfigurationEventType operation, IdmConfigurationDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}