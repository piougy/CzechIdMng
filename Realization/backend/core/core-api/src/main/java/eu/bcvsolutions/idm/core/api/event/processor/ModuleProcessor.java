package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.ModuleDescriptorDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Module processor - used mainly for module init.
 * 
 * @author Radek Tomiška
 * @since 10.5.0
 */
public interface ModuleProcessor extends EntityEventProcessor<ModuleDescriptorDto> {
	
}
