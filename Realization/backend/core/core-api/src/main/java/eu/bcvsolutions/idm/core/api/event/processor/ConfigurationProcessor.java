package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmConfigurationDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Configuration processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 10.0.0
 */
public interface ConfigurationProcessor extends EntityEventProcessor<IdmConfigurationDto> {
	
}
