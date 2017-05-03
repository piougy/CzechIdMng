package eu.bcvsolutions.idm.core.model.event.processor.identity;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityRoleService;

/**
 * Persists identity contract.
 * 
 * @author Radek Tomiška
 *
 */
@Component
@Description("Persists identity contract.")
public class IdentityContractSaveProcessor extends CoreEventProcessor<IdmIdentityContractDto> {
	
	public static final String PROCESSOR_NAME = "identity-contract-save-processor";
	private final IdmIdentityContractRepository repository;
	private final IdmIdentityContractService service;
	private final IdmIdentityRoleService identityRoleService;
	
	@Autowired
	public IdentityContractSaveProcessor(
			IdmIdentityContractRepository repository,
			IdmIdentityContractService service,
			IdmIdentityRoleService identityRoleService) {
		super(IdentityContractEventType.UPDATE, IdentityContractEventType.CREATE);
		//
		Assert.notNull(repository);
		Assert.notNull(service);
		Assert.notNull(identityRoleService);
		//
		this.repository = repository;
		this.service = service;
		this.identityRoleService = identityRoleService;
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityContractDto> process(EntityEvent<IdmIdentityContractDto> event) {
		IdmIdentityContractDto contract = event.getContent();
		if (contract.isMain()) {
			this.repository.clearMain(contract.getIdentity(), contract.getId(), new DateTime());
		}
		contract = service.saveInternal(contract);
		event.setContent(contract);
		//
		if (IdentityContractEventType.UPDATE == event.getType()) {
			if (contract.isDisabled()) {
				// remove all referenced roles
				identityRoleService.findAllByContract(contract.getId()).forEach(identityRole -> {
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
