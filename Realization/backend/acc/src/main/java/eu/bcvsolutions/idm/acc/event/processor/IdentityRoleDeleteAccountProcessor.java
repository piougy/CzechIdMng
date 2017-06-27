package eu.bcvsolutions.idm.acc.event.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleEvent.IdentityRoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Identity role account management before delete
 *
 * @author Radek Tomiška
 */
@Component
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes account management before identity role is deleted.")
public class IdentityRoleDeleteAccountProcessor extends AbstractEntityEventProcessor<IdmIdentityRoleDto> {

	public static final String PROCESSOR_NAME = "identity-role-delete-account-processor";
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleDeleteAccountProcessor.class);
	private final AccAccountManagementService accountManagementService;

	@Autowired
	public IdentityRoleDeleteAccountProcessor(AccAccountManagementService accountManagementService) {
		super(IdentityRoleEventType.DELETE);
		//
		Assert.notNull(accountManagementService);
		//
		this.accountManagementService = accountManagementService;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityRoleDto> process(EntityEvent<IdmIdentityRoleDto> event) {
		IdmIdentityRoleDto identityRole = event.getContent();
		accountManagementService.deleteIdentityAccount(identityRole);
		return new DefaultEventResult<>(event, this);
	}

	@Override
	public int getOrder() {
		return -ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}
}