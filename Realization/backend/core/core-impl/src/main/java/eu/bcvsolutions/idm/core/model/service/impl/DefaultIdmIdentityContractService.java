package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.filter.EmptyFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;

/**
 * Identity contract administration
 * - supports {@link IdentityContractEvent}.
 * - identity contract is required for role assign
 * 
 * @author Radek Tomiška
 *
 */
@Service
public class DefaultIdmIdentityContractService extends AbstractReadWriteEntityService<IdmIdentityContract, EmptyFilter> implements IdmIdentityContractService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityContractService.class);
	private final IdmIdentityContractRepository repository;
	private final EntityEventManager entityEventManager;
	
	@Autowired
	public DefaultIdmIdentityContractService(
			IdmIdentityContractRepository repository,
			EntityEventManager entityEventManager) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
	}
	
	public List<IdmIdentityContract> getContracts(IdmIdentity identity) {
		return repository.findAllByIdentity(identity, null);
	}
	
	/**
	 * Publish {@IdentityContractEvent} only.
	 * 
	 * @see {@link IdentityContractSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmIdentityContract save(IdmIdentityContract entity) {
		Assert.notNull(entity);
		Assert.notNull(entity.getIdentity());
		//
		if (entity.getId() == null) { // create
			LOG.debug("Saving new contract for identity [{}]", entity.getIdentity().getUsername());
			return entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.CREATE, entity)).getContent();
		} 
		LOG.debug("Saving contract [{}] for identity [{}]", entity.getId(), entity.getIdentity().getUsername());
		return entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.UPDATE, entity)).getContent();
	}
	
	@Override
	@Transactional
	public void delete(IdmIdentityContract entity) {
		Assert.notNull(entity);
		Assert.notNull(entity.getIdentity());
		//
		LOG.debug("Deleting contract [{}] for identity [{}]", entity.getId(), entity.getIdentity().getUsername());
		entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.DELETE, entity));
	}
}
