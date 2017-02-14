package eu.bcvsolutions.idm.core.model.event.processor;

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
 * Deletes identity contract - ensures referential integrity.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Deletes identity contract.")
public class IdentityContractDeleteProcessor extends CoreEventProcessor<IdmIdentityContract> {

	public static final String PROCESSOR_NAME = "identity-contract-delete-processor";
	private final IdmIdentityContractRepository repository;
	
	@Autowired
	public IdentityContractDeleteProcessor(IdmIdentityContractRepository repository) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(repository);
		//
		this.repository = repository;
		
	}
	
	
	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		// delete identity contract
		repository.delete(event.getContent());
		//
		// TODO: delete related identity roles
		//
		return new DefaultEventResult<>(event, this);
	}

}
