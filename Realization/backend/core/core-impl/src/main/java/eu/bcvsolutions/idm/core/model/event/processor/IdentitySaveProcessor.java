package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Save identity, catch event UPDATE and CREATE
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Component
public class IdentitySaveProcessor extends CoreEventProcessor<IdmIdentity> {

	private final IdmIdentityRepository repository;
	private final IdentityPasswordProcessor passwordProcessor;
	
	@Autowired
	public IdentitySaveProcessor(
			IdmIdentityRepository repository,
			IdentityPasswordProcessor passwordProcessor) {
		super(IdentityEventType.UPDATE, IdentityEventType.CREATE);
		//
		Assert.notNull(repository);
		Assert.notNull(passwordProcessor);
		//
		this.repository = repository;
		this.passwordProcessor = passwordProcessor;
	}

	@Override
	public EventResult<IdmIdentity> process(EntityEvent<IdmIdentity> event) {
		IdmIdentity identity = event.getContent();
		GuardedString password = identity.getPassword();
		
		identity = repository.save(identity);
		// save password to confidential storage
		if (password != null) {
			passwordProcessor.savePassword(identity, password);
		}
		// TODO: clone identity - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}
}