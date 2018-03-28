package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.RoleProcessor;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Run provisioning after role was changed.
 * 
 * @author Svanda
 * @author Radek Tomiška
 */
@Component("accRoleSaveProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioning after role was changed.")
public class RoleSaveProcessor
		extends CoreEventProcessor<IdmRoleDto> 
		implements RoleProcessor {

	public static final String PROCESSOR_NAME = "role-save-processor";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleSaveProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;

	@Autowired
	public RoleSaveProcessor(ApplicationContext applicationContext) {
		super(RoleEventType.NOTIFY);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}
	
	@Override
	public boolean conditional(EntityEvent<IdmRoleDto> event) {
		// Skip provisioning
		return !this.getBooleanProperty(ProvisioningService.SKIP_PROVISIONING, event.getProperties());
	}

	@Override
	public EventResult<IdmRoleDto> process(EntityEvent<IdmRoleDto> event) {
		doProvisioning(event.getContent());
		//
		return new DefaultEventResult<>(event, this);
	}

	private void doProvisioning(IdmRoleDto role) {
		LOG.debug("Call account managment (create accounts for all systems) for role [{}]", role.getCode());
		getProvisioningService().accountManagement(role);
		LOG.debug("Call provisioning for role [{}]", role.getCode());
		getProvisioningService().doProvisioning(role);
	}

	@Override
	public int getOrder() {
		return ProvisioningEvent.DEFAULT_PROVISIONING_ORDER;
	}

	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private ProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(ProvisioningService.class);
		}
		return provisioningService;
	}

}