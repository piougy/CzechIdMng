package eu.bcvsolutions.idm.core.model.event.processor;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;

/**
 * Persists identity contract.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity contract.")
public class IdentityContractSaveProcessor extends CoreEventProcessor<IdmIdentityContract> {
	
	public static final String PROCESSOR_NAME = "identity-contract-save-processor";
	private final IdmIdentityContractRepository repository;
	
	@Autowired
	public IdentityContractSaveProcessor(IdmIdentityContractRepository repository) {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.CREATE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract entity = event.getContent();
		if (entity.isMain()) {
			this.repository.clearMain(entity.getIdentity(), entity.getId(), new DateTime());
		}
		repository.save(entity);
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}

}
