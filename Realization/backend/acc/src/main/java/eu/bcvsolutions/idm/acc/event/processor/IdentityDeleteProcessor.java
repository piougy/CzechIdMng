package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;

/**
 * Before identity delete - deletes all identity accounts
 * 
 * @author Radek Tomiška
 *
 */
@Order(-1)
@Component("accIdentityDeleteProcessor")
public class IdentityDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentity> {
	
	private AccIdentityAccountService identityAccountService;
	private final ApplicationContext applicationContext;
	
	@Autowired
	public IdentityDeleteProcessor(ApplicationContext applicationContext) {
		super(IdentityOperationType.DELETE);
		//
		Assert.notNull(applicationContext);
		//
		this.applicationContext = applicationContext;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		Assert.notNull(event.getContent());
		//
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(event.getContent().getId());
		getIdentityAccountService().find(filter, null).forEach(identityAccount -> {
			getIdentityAccountService().delete(identityAccount);
		});
		return event;
	}
	
	/**
	 * identityAccountService has dependency everywhere - so we need lazy init ...
	 * 
	 * @return
	 */
	private AccIdentityAccountService getIdentityAccountService() {
		if (identityAccountService == null) {
			identityAccountService = applicationContext.getBean(AccIdentityAccountService.class);
		}
		return identityAccountService;
	}
}