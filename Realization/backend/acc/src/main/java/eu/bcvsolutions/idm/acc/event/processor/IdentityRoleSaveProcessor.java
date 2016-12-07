package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.acc.service.api.SysProvisioningService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityRoleOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Identity role account management after save
 * 
 * @author Radek Tomiška
 *
 */
@Order(ProvisioningEvent.DEFAULT_PROVISIONING_ORDER)
@Component("accIdentityRoleSaveProcessor")
public class IdentityRoleSaveProcessor extends AbstractEntityEventProcessor<IdmIdentityRole> {

	private AccAccountManagementService accountManagementService;
	private final ApplicationContext applicationContext;
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleSaveProcessor.class);
	private SysProvisioningService provisioningService;

	@Autowired
	public IdentityRoleSaveProcessor(ApplicationContext applicationContext) {
		super(IdentityRoleOperationType.SAVE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}


	@Override
	public EntityEvent<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> context) {
		Assert.notNull(context.getContent());
		//
		LOG.debug("Call account management for idnetity [{}]", context.getContent().getIdentity().getUsername());
		boolean provisioningRequired = getAccountManagementService().resolveIdentityAccounts(context.getContent().getIdentity());
		if(provisioningRequired){
			LOG.debug("Call provisioning for idnetity [{}]", context.getContent().getIdentity().getUsername());
			getProvisioningService().doProvisioning(context.getContent().getIdentity());
		}
		//
		return context;
	}
	
	/**
	 * accountManagementService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private AccAccountManagementService getAccountManagementService() {
		if (accountManagementService == null) {
			accountManagementService = applicationContext.getBean(AccAccountManagementService.class);
		}
		return accountManagementService;
	}
	
	/**
	 * provisioningService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private SysProvisioningService getProvisioningService() {
		if (provisioningService == null) {
			provisioningService = applicationContext.getBean(SysProvisioningService.class);
		}
		return provisioningService;
	}
}