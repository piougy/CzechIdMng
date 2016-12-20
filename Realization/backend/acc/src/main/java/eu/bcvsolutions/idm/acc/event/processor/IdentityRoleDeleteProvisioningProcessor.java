package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.security.api.domain.Enabled;

/**
 * Identity role account management before delete
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
public class IdentityRoleDeleteProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityRole> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleDeleteProvisioningProcessor.class);
	private final AccAccountManagementService accountManagementService;
	private final SysProvisioningService provisioningService;

	@Autowired
	public IdentityRoleDeleteProvisioningProcessor(
			AccAccountManagementService accountManagementService,
			SysProvisioningService provisioningService) {
		super(IdentityRoleEventType.DELETE);
		//
		Assert.notNull(accountManagementService);
		Assert.notNull(provisioningService);
		//
		this.accountManagementService = accountManagementService;
		this.provisioningService = provisioningService;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		accountManagementService.deleteIdentityAccount(event.getContent());
		//
		LOG.debug("Call provisioning for idnetity [{}]", event.getContent().getIdentity().getUsername());
		provisioningService.doProvisioning(event.getContent().getIdentity());
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return -ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}