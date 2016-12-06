package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.service.api.IdmProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityPasswordProcessor;

/**
 * Identity's password provisioning
 * 
 * @author Radek Tomiška
 *
 */
@Order(1000)
@Component
public class IdentityPasswordProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityPasswordProvisioningProcessor.class);
	private final IdmProvisioningService provisioningService;
	
	@Autowired
	public IdentityPasswordProvisioningProcessor(IdmProvisioningService provisioningService) {
		super(IdentityOperationType.PASSWORD);
		//
		Assert.notNull(provisioningService);
		//
		this.provisioningService = provisioningService;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
		IdmIdentity identity = context.getContent();
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) context.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		Assert.notNull(identity);
		Assert.notNull(passwordChangeDto);
		//
		LOG.debug("Call provisioning for idnetity password [{}]", context.getContent().getUsername());
		provisioningService.changePassword(identity, passwordChangeDto);
		return context;
	}
}