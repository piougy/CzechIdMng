package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteEntityService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityContractDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityContractSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
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
public class DefaultIdmIdentityContractService extends AbstractReadWriteEntityService<IdmIdentityContract, IdentityContractFilter> implements IdmIdentityContractService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityContractService.class);
	private final IdmIdentityContractRepository repository;
	private final EntityEventManager entityEventManager;
	private final IdmTreeTypeRepository treeTypeRepository;
	
	@Autowired
	public DefaultIdmIdentityContractService(
			IdmIdentityContractRepository repository,
			EntityEventManager entityEventManager,
			IdmTreeTypeRepository treeTypeRepository) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(treeTypeRepository);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
		this.treeTypeRepository = treeTypeRepository;
	}
	
	public List<IdmIdentityContract> getContracts(IdmIdentity identity) {
		return repository.findAllByIdentity(identity, new Sort("validFrom"));
	}
	
	/**
	 * Publish {@link IdentityContractEvent} only.
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
	
	/**
	 * Publish {@link IdentityContractEvent} only.
	 * 
	 * @see {@link IdentityContractDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmIdentityContract entity) {
		Assert.notNull(entity);
		Assert.notNull(entity.getIdentity());
		//
		LOG.debug("Deleting contract [{}] for identity [{}]", entity.getId(), entity.getIdentity().getUsername());
		entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.DELETE, entity));
	}

	@Override
	@Transactional
	public int clearGuarantee(IdmIdentity identity) {
		return repository.clearGuarantee(identity, new DateTime());
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityContract> findExpiredContracts(LocalDate expiration, Pageable pageable) {
		return repository.findExpiredContracts(expiration, pageable);
	}

	@Override
	public IdmIdentityContract prepareDefaultContract(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		// set identity
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		//
		// set working position
		IdmTreeType defaultTreeType = treeTypeRepository.findOneByDefaultTreeTypeIsTrue();
		if (defaultTreeType != null && defaultTreeType.getDefaultTreeNode() != null) {
			contract.setWorkingPosition(defaultTreeType.getDefaultTreeNode());
		} else {
			contract.setPosition(DEFAULT_POSITION_NAME); // TODO: from configuration manager
		}
		return contract;
	}
}
