package eu.bcvsolutions.idm.core.model.event.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.event.AbstractEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.IdentityOperationType;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityRoleRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmRoleGuaranteeRepository;
import eu.bcvsolutions.idm.eav.service.api.FormService;

/**
 * Delete identity
 * 
 * @author Radek Tomiška
 *
 */
@Order(0)
@Component
public class IdentityDeleteProcessor extends AbstractEntityEventProcessor<IdmIdentity> {

	private final IdmIdentityRepository repository;
	private final FormService formService;
	private final IdentityPasswordProcessor passwordProcessor;
	private final IdmRoleGuaranteeRepository roleGuaranteeRepository;
	private final IdmIdentityRoleRepository identityRoleRepository;
	private final IdmIdentityContractRepository identityContractRepository;
	
	@Autowired
	public IdentityDeleteProcessor(
			IdmIdentityRepository repository,
			FormService formService,
			IdentityPasswordProcessor passwordProcessor,
			IdmRoleGuaranteeRepository roleGuaranteeRepository,
			IdmIdentityRoleRepository identityRoleRepository,
			IdmIdentityContractRepository identityContractRepository) {
		super(IdentityOperationType.DELETE);
		//
		Assert.notNull(repository);
		Assert.notNull(formService);
		Assert.notNull(passwordProcessor);
		Assert.notNull(roleGuaranteeRepository);
		Assert.notNull(identityRoleRepository);
		Assert.notNull(identityContractRepository);
		//
		this.repository = repository;
		this.formService = formService;
		this.passwordProcessor = passwordProcessor;
		this.roleGuaranteeRepository = roleGuaranteeRepository;
		this.identityRoleRepository = identityRoleRepository;
		this.identityContractRepository = identityContractRepository;
	}

	@Override
	public EntityEvent<IdmIdentity> process(EntityEvent<IdmIdentity> context) {
		Assert.notNull(context.getContent());
		IdmIdentity identity = context.getContent();
		//
		// clear referenced roles
		identityRoleRepository.deleteByIdentity(identity);
		// contracts
		identityContractRepository.deleteByIdentity(identity);
		// contract guaratee - set to null
		identityContractRepository.clearGuarantee(identity);
		// remove role guarantee
		roleGuaranteeRepository.deleteByGuarantee(identity);
		// remove password from confidential storage
		passwordProcessor.deletePassword(identity);
		// delete eav attrs
		formService.deleteValues(identity);
		// deletes identity
		repository.delete(identity);
		return context;
	}
}