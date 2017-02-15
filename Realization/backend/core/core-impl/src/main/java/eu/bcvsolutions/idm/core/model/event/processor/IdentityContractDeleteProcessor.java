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
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;

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
	private final IdmIdentityRoleRepository identityRoleRepository;
	
	@Autowired
	public IdentityContractDeleteProcessor(
			IdmIdentityContractRepository repository,
			IdmIdentityRoleRepository identityRoleRepository) {
		super(IdentityContractEventType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleRepository);
		//
		this.repository = repository;
		this.identityRoleRepository = identityRoleRepository;
		
	}
	
	
	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract contract = event.getContent();
		//
		// delete referenced roles
		identityRoleRepository.deleteByIdentityContract(contract); // TODO: IdmIdentityRoleService or call provisioning separately?
		// delete identity contract
		repository.delete(contract);
		//
		return new DefaultEventResult<>(event, this);
	}

}
