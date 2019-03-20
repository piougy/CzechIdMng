package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Test notify processor - for fest properties propagation.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class TestIdentityNotifyProcessor extends CoreEventProcessor<IdmIdentityDto> {
	
	public static final String TEST_PROPERTY_NAME = "test:property-one";
	public static final String TEST_PROPERTY_VALUE = "test:property-value";
	
	public TestIdentityNotifyProcessor() {
		super(IdentityEventType.NOTIFY);
	}
	
	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		event.getProperties().put(TEST_PROPERTY_NAME, TEST_PROPERTY_VALUE);
		//
		return new DefaultEventResult<>(event, this);
	}
}
