package eu.bcvsolutions.idm.core.api.event.processor;

import eu.bcvsolutions.idm.core.api.dto.IdmScriptDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * Script processors should implement this interface.
 * 
 * @author Radek Tomiška
 * @since 10.6.0
 */
public interface ScriptProcessor extends EntityEventProcessor<IdmScriptDto> {
	
}
