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
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

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
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public IdentityContractSaveProcessor(
			IdmIdentityContractRepository repository,
			IdmIdentityRoleService identityRoleService) {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.CREATE);
		//
		Assert.notNull(repository);
		Assert.notNull(identityRoleService);
		//
		this.repository = repository;
		this.identityRoleService = identityRoleService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContract> process(EntityEvent<IdmIdentityContract> event) {
		IdmIdentityContract contract = event.getContent();
		if (contract.isMain()) {
			this.repository.clearMain(contract.getIdentity(), contract.getId(), new DateTime());
		}
		repository.save(contract);
		//
		if (IdentityContractEventType.UPDATE == event.getType()) {
			if (contract.isDisabled()) {
				// remove all referenced roles
				identityRoleService.getRoles(contract).forEach(identityRole -> {
					identityRoleService.delete(identityRole);
				});
			} else {
				// automatic roles was added by IdentityContractUpdateAutomaticRoleProcessor
			}
		}
		//
		// TODO: clone content - mutable previous event content :/
		return new DefaultEventResult<>(event, this);
	}

}
