package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

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
import eu.bcvsolutions.idm.core.model.domain.RecursionType;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityContractDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.IdentityContractSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
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
public class DefaultIdmIdentityContractService 
		extends AbstractReadWriteEntityService<IdmIdentityContract, IdentityContractFilter> 
		implements IdmIdentityContractService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmIdentityContractService.class);
	private final IdmIdentityContractRepository repository;
	private final EntityEventManager entityEventManager;
	private final IdmTreeTypeRepository treeTypeRepository;
	private final IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	public DefaultIdmIdentityContractService(
			IdmIdentityContractRepository repository,
			EntityEventManager entityEventManager,
			IdmTreeTypeRepository treeTypeRepository,
			IdmTreeNodeRepository treeNodeRepository) {
		super(repository);
		//
		Assert.notNull(entityEventManager);
		Assert.notNull(treeTypeRepository);
		Assert.notNull(treeNodeRepository);
		//
		this.repository = repository;
		this.entityEventManager = entityEventManager;
		this.treeTypeRepository = treeTypeRepository;
		this.treeNodeRepository = treeNodeRepository;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityContract> getContracts(IdmIdentity identity) {
		return repository.findAllByIdentity(identity, new Sort("validFrom"));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityContract> getContractsByWorkPosition(UUID workPositionId, RecursionType recursion) {
		Assert.notNull(workPositionId);
		IdmTreeNode workPosition = treeNodeRepository.findOne(workPositionId);
		Assert.notNull(workPosition);
		//
		return repository.findAllByWorkPosition(workPosition, recursion == null ? RecursionType.NO : recursion);
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
		// we need to read previous value ...
		//
		if (isNew(entity)) { // create
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
	public Page<IdmIdentityContract> findExpiredContracts(LocalDate expiration, boolean disabled, Pageable pageable) {
		return repository.findExpiredContracts(expiration, disabled, pageable);
	}

	@Override
	public IdmIdentityContract prepareDefaultContract(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		// set identity
		IdmIdentityContract contract = new IdmIdentityContract();
		contract.setIdentity(identity);
		contract.setMain(true);
		//
		// set working position
		IdmTreeType defaultTreeType = treeTypeRepository.findOneByDefaultTreeTypeIsTrue();
		if (defaultTreeType != null && defaultTreeType.getDefaultTreeNode() != null) {
			contract.setWorkingPosition(defaultTreeType.getDefaultTreeNode());
		} else {
			contract.setPosition(DEFAULT_POSITION_NAME);
		}
		return contract;
	}
	
	/**
	 * Returns given identity's prime contract.
	 * If no main contract is defined, then returns the first contract with working position defined (default tree type has higher priority).
	 * 
	 * @param identity
	 * @return
	 */
	@Override
	@Transactional(readOnly = true)
	public IdmIdentityContract getPrimeContract(IdmIdentity identity) {
		Assert.notNull(identity);
		//
		// find all identity working position
		List<IdmIdentityContract> contracts = repository.findAllByIdentity(identity, null);
		if (contracts.isEmpty()) {
			return null;
		}
		IdmIdentityContract primeContract = null;
		IdmTreeType defaultTreeType = treeTypeRepository.findOneByDefaultTreeTypeIsTrue();
		for (IdmIdentityContract contract : contracts) {
			// find main contract or contract with working position (default tree type has higher priority)
			if (contract.isMain()) {
				return contract;
			}
			IdmTreeNode workingPosition = contract.getWorkingPosition();
			if (primeContract == null || (workingPosition != null && defaultTreeType != null && defaultTreeType.equals(workingPosition.getTreeType()))) {
				primeContract = contract;
			}
		}
		return primeContract;
	}
}

