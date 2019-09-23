package eu.bcvsolutions.idm.acc.service.impl;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Simple acm exception simulation.
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class TestAccountExceptionProcessor extends AbstractEntityEventProcessor<AccAccountDto> {

	private boolean disabled = true;
	private String failOnUid = null; // fail only for some accounts (by account uid ~ e.g. identity username).
	
	@Override
	public EventResult<AccAccountDto> process(EntityEvent<AccAccountDto> event) {
		if (failOnUid == null || event.getContent().getUid().equals(failOnUid)) {
			throw new RuntimeException("test exception");
		}
		return null;
	}
	
	@Override
	public boolean isDisabled() {
		return disabled;
	}
	
	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
	
	public void setFailOnUid(String failOnUid) {
		this.failOnUid = failOnUid;
	}

	/**
	 * Before provisioning
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1;
	}

	
	
}
