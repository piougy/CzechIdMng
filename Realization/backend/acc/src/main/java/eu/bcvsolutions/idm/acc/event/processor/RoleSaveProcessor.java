package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.AccModuleDescriptor;
import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.ProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmRole;
import eu.bcvsolutions.idm.core.model.event.RoleEvent.RoleEventType;
import eu.bcvsolutions.idm.core.security.api.domain.Enabled;

/**
 * Run provisioning after role was saved.
 * 
 * @author Svanda
 *
 */
@Component("accRoleSaveProcessor")
@Enabled(AccModuleDescriptor.MODULE_ID)
@Description("Executes provisioing after role is saved.")
public class RoleSaveProcessor extends AbstractEntityEventProcessor<IdmRole> {

	public static final String PROCESSOR_NAME = "role-save-processor";
	public static final String SKIP_PROVISIONING = "skip_provisioning";
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RoleSaveProcessor.class);
	private ProvisioningService provisioningService;
	private final ApplicationContext applicationContext;

	@Autowired
	public RoleSaveProcessor(ApplicationContext applicationContext) {
		super(RoleEventType.CREATE, RoleEventType.UPDATE, CoreEventType.EAV_SAVE);
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
	public EventResult<IdmRole> process(EntityEvent<IdmRole> event) {
		Object breakProvisioning = event.getProperties().get(SKIP_PROVISIONING);
		
		if(breakProvisioning != null && breakProvisioning instanceof Boolean && (Boolean)breakProvisioning){
			return new DefaultEventResult<>(event, this);
		}
		doProvisioning(event.getContent());
		return new DefaultEventResult<>(event, this);
	}

	private void doProvisioning(IdmRole node) {
		LOG.debug("Call account managment (create accounts for all systems) for role [{}]", node.getName());
		getProvisioningService().createAccountsForAllSystems(node);
		LOG.debug("Call provisioning for role [{}]", node.getName());
		getProvisioningService().doProvisioning(node);
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