package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.exception.CoreException;

/**
 * Test never ends processor
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class NeverEndingProcessor extends CoreEventProcessor<IdmIdentityDto> {

	public static boolean wait = false;
	public static final EventType WAIT = (EventType) () -> "WAIT";
	
	public NeverEndingProcessor() {
		super(WAIT);
	}
	
	@Override
	public boolean supports(EntityEvent<?> event) {
		return super.supports(event);
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		// TODO Auto-generated method stub
		int counter = 0;
		while (wait && counter < 50) { // max wait is 15s
			try {
				counter++;
				Thread.sleep(300L);
			} catch (Exception ex) {
				throw new CoreException(ex);
			}
		}
		return null;
	}

}
