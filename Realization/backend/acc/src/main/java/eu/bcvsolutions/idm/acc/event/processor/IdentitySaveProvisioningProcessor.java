package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.service.api.IdmProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Identity provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Order(1000)
@Component
public class IdentitySaveProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentitySaveProvisioningProcessor.class);
	private final IdmProvisioningService provisioningService;
	
	@Autowired
	public IdentitySaveProvisioningProcessor(IdmProvisioningService provisioningService) {
		super(IdentityOperationType.SAVE);
		//
		Assert.notNull(provisioningService);
		//
		this.provisioningService = provisioningService;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
		Assert.notNull(context.getContent());
		//
		LOG.debug("Call provisioning for idnetity [{}]", context.getContent().getUsername());
		provisioningService.doProvisioning(context.getContent());
		return context;
	}
	
}