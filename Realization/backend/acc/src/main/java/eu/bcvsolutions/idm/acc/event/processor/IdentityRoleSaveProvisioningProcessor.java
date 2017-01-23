package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.security.api.domain.Enabled;

/**
 * Identity role account management after save
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes account management and provisioing after identity role is saved.")
public class IdentityRoleSaveProvisioningProcessor extends AbstractEntityEventProcessor<IdmIdentityRole> {

	public static final String PROCESSOR_NAME = "identity-role-save-provisioning-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleSaveProvisioningProcessor.class);
	private final AccAccountManagementService accountManagementService;
	private final ProvisioningService provisioningService;

	@Autowired
	public IdentityRoleSaveProvisioningProcessor(
			AccAccountManagementService accountManagementService,
			ProvisioningService provisioningService) {
		super(IdentityRoleEventType.CREATE, IdentityRoleEventType.UPDATE);
		//
		Assert.notNull(accountManagementService);
		Assert.notNull(provisioningService);
		//
		this.accountManagementService = accountManagementService;
		this.provisioningService = provisioningService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> event) {
		//
		LOG.debug("Call account management for idnetity [{}]", event.getContent().getIdentity().getUsername());
		boolean provisioningRequired = accountManagementService.resolveIdentityAccounts(event.getContent().getIdentity());
		if (provisioningRequired) {
			LOG.debug("Call provisioning for idnetity [{}]", event.getContent().getIdentity().getUsername());
			provisioningService.doProvisioning(event.getContent().getIdentity());
		}
		//
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}