package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
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
 * Delete identity - deletes all identity accounts
 * 
 * @author Radek Tomiška
 *
 */
@Order(2)
@Component("accIdentityDeleteProcessor")
public class IdentityDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentity> {
	
	private final AccIdentityAccountService identityAccountService;
	
	@Autowired
	public IdentityDeleteProcessor(AccIdentityAccountService identityAccountService) {
		super(IdentityOperationType.DELETE);
		//
		Assert.notNull(identityAccountService);
		//
		this.identityAccountService = identityAccountService;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		Assert.notNull(event.getContent());
		//
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(event.getContent().getId());
		identityAccountService.find(filter, null).forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});
		return event;
	}
}