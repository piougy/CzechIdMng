package eu.bcvsolutions.idm.acc.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.acc.dto.IdentityAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccIdentityAccountService;
import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;

/**
 * Before identity delete - deletes all identity accounts
 * 
 * @author Radek Tomiška
 *
 */
@Component("accIdentityDeleteProcessor")
public class IdentityDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentity> {
	
	private final AccIdentityAccountService identityAccountService;
	
	@Autowired
	public IdentityDeleteProcessor(AccIdentityAccountService identityAccountService) {
		super(IdentityEventType.DELETE);
		//
		Assert.notNull(identityAccountService);
		//
		this.identityAccountService = identityAccountService;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdentityAccountFilter filter = new IdentityAccountFilter();
		filter.setIdentityId(event.getContent().getId());
		identityAccountService.find(filter, null).forEach(identityAccount -> {
			identityAccountService.delete(identityAccount);
		});
		return new DefaultEventResult<>(event, this);
	}
	
	@Override
	public int getOrder() {
		// right now before identity delete
		return CoreEvent.DEFAULT_ORDER - 1;
	}
}