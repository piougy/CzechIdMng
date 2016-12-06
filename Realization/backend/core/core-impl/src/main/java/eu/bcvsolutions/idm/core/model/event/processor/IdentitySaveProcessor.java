package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.security.api.domain.GuardedString;

/**
 * Save identity
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Component
public class IdentitySaveProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	private final IdmIdentityRepository repository;
	private final IdentityPasswordProcessor passwordProcessor;
	
	@Autowired
	public IdentitySaveProcessor(
			IdmIdentityRepository repository,
			IdentityPasswordProcessor passwordProcessor) {
		super(IdentityOperationType.SAVE);
		//
		Assert.notNull(repository);
		Assert.notNull(passwordProcessor);
		//
		this.repository = repository;
		this.passwordProcessor = passwordProcessor;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
		Assert.notNull(context.getContent());
		//
		IdmIdentity identity = context.getContent();
		GuardedString password = identity.getPassword();

		identity = repository.save(identity);
		// save password to confidential storage
		if (password != null) {
			passwordProcessor.savePassword(identity, password);
		}
		context.setContent(identity);
		return context;
	}
}