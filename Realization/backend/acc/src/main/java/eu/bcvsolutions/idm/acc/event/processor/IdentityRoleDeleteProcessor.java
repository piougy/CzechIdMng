package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.event.ProvisioningEvent;
import eu.bcvsolutions.idm.acc.service.api.AccAccountManagementService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityRoleOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;

/**
 * Identity role account management after delete
 * 
 * @author Radek Tomiška
 *
 */
@Order(ProvisioningEvent.DEFAULT_PROVISIONING_ORDER)
@Component("accIdentityRoleDeleteProcessor")
public class IdentityRoleDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentityRole> {

	private AccAccountManagementService accountManagementService;
	private final ApplicationContext applicationContext;

	@Autowired
	public IdentityRoleDeleteProcessor(ApplicationContext applicationContext) {
		super(IdentityRoleOperationType.DELETE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public EntityEvent<IdmIdentityRole> process(EntityEvent<IdmIdentityRole> context) {
		Assert.notNull(context.getContent());
		//
		getAccountManagementService().deleteIdentityAccount(context.getContent());
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
}