package eu.bcvsolutions.idm.core.api.event;

import java.io.Serializable;
import java.util.Map;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;

/**
 * Events for modules.
 * 
 * @author Radek Tomiška
 * @since 10.4.0
 */
public class ModuleDescriptorEvent extends CoreEvent<ModuleDescriptorDto> {

	private static final long serialVersionUID = 1L;

	/**
	 * Supported module events.
	 */
	public enum ModuleDescriptorEventType implements EventType {
		INIT, ENABLE, DISABLE
	}
	
	public ModuleDescriptorEvent(ModuleDescriptorEventType operation, ModuleDescriptorDto content) {
		super(operation, content);
	}
	
	public ModuleDescriptorEvent(ModuleDescriptorEventType operation, ModuleDescriptorDto content, Map<String, Serializable> properties) {
		super(operation, content, properties);
	}

}