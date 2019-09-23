package eu.bcvsolutions.idm.acc.service.impl;

import java.util.UUID;

import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.acc.dto.SysProvisioningOperationDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;

/**
 * Simple provisioning exception simulation
 * 
 * @author Radek Tomiška
 *
 */
@Component
public class TestProvisioningExceptionProcessor extends AbstractEntityEventProcessor<SysProvisioningOperationDto> {

	private boolean disabled = true;
	private UUID failOnEntityIdentifier = null; // fail only for some provisioning operations (by entity identifier).
	
	@Override
	public EventResult<SysProvisioningOperationDto> process(EntityEvent<SysProvisioningOperationDto> event) {
		if (failOnEntityIdentifier == null || event.getContent().getEntityIdentifier().equals(failOnEntityIdentifier)) {
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
	
	public void setFailOnEntityIdentifier(UUID failOnEntityIdentifier) {
		this.failOnEntityIdentifier = failOnEntityIdentifier;
	}

	/**
	 * Before provisioning
	 */
	@Override
	public int getOrder() {
		return CoreEvent.DEFAULT_ORDER - 1;
	}

	
	
}
