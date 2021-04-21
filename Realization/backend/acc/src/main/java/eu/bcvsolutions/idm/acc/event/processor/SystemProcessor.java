package eu.bcvsolutions.idm.acc.event.processor;

import eu.bcvsolutions.idm.acc.dto.SysSystemDto;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;

/**
 * System processors should implement this interface.
 * 
 * @author Ondrej Husnik
 *
 */
public interface SystemProcessor extends EntityEventProcessor<SysSystemDto> {
	
}
