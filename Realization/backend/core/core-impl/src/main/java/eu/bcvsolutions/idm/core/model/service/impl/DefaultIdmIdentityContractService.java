package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.domain.RecursionType;
import eu.bcvsolutions.idm.core.model.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.model.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent.IdentityContractEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityContractDeleteProcessor;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityContractSaveProcessor;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity contract administration
 * - supports {@link IdentityContractEvent}.
 * - identity contract is required for role assign
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmIdentityContractService 
		extends AbstractReadWriteDtoService<IdmIdentityContractDto, IdmIdentityContract, IdentityContractFilter> 
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
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYCONTRACT, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityContract> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityContractFilter filter) {
		List<Predicate> predicates = new ArrayList<>();
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get(AbstractEntity_.id), filter.getId()));
		}
		// quick
		if (StringUtils.isNotEmpty(filter.getText())) {
			Path<IdmTreeNode> wp = root.get(IdmIdentityContract_.workPosition);
			predicates.add(
					builder.or(
							builder.like(builder.lower(root.get(IdmIdentityContract_.position)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(wp.get(IdmTreeNode_.name)), "%" + filter.getText().toLowerCase() + "%"),
							builder.like(builder.lower(wp.get(IdmTreeNode_.code)), "%" + filter.getText().toLowerCase() + "%")
							)
					);
		}
		if (filter.getIdentity() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.identity), filter.getIdentity()));
		}
		if (filter.getValidTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmIdentityContract_.validTill), filter.getValidTill()));
		}
		if (filter.getValidFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmIdentityContract_.validFrom), filter.getValidFrom()));
		}
		if (filter.getExterne() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.externe), filter.getExterne()));
		}
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityContractDto> findAllByIdentity(UUID identityId) {
		return toDtos(repository.findAllByIdentity_Id(identityId, new Sort(IdmIdentityContract_.validFrom.getName())), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityContractDto> findAllByWorkPosition(UUID workPositionId, RecursionType recursion) {
		Assert.notNull(workPositionId);
		IdmTreeNode workPosition = treeNodeRepository.findOne(workPositionId);
		Assert.notNull(workPosition);
		//
		return toDtos(repository.findAllByWorkPosition(workPosition, recursion == null ? RecursionType.NO : recursion), false);
	}
	
	/**
	 * Publish {@link IdentityContractEvent} only.
	 * 
	 * @see {@link IdentityContractSaveProcessor}
	 */
	@Override
	@Transactional
	public IdmIdentityContractDto save(IdmIdentityContractDto entity, BasePermission... permission) {
		Assert.notNull(entity);
		Assert.notNull(entity.getIdentity());
		//
		// we need to read previous value ...
		//
		if (isNew(entity)) { // create
			LOG.debug("Saving new contract for identity [{}]", entity.getIdentity());
			return entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.CREATE, entity)).getContent();
		}
		LOG.debug("Saving contract [{}] for identity [{}]", entity.getId(), entity.getIdentity());
		return entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.UPDATE, entity)).getContent();
	}
	
	/**
	 * Publish {@link IdentityContractEvent} only.
	 * 
	 * @see {@link IdentityContractDeleteProcessor}
	 */
	@Override
	@Transactional
	public void delete(IdmIdentityContractDto entity, BasePermission... permission) {
		Assert.notNull(entity);
		Assert.notNull(entity.getIdentity());
		//
		LOG.debug("Deleting contract [{}] for identity [{}]", entity.getId(), entity.getIdentity());
		entityEventManager.process(new IdentityContractEvent(IdentityContractEventType.DELETE, entity));
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityContractDto> findExpiredContracts(LocalDate expiration, boolean disabled, Pageable pageable) {
		return toDtoPage(repository.findExpiredContracts(expiration, disabled, pageable));
	}

	@Override
	public IdmIdentityContractDto prepareDefaultContract(UUID identityId) {
		Assert.notNull(identityId);
		//
		// set identity
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identityId);
		contract.setMain(true);
		//
		// set working position
		IdmTreeType defaultTreeType = treeTypeRepository.findOneByDefaultTreeTypeIsTrue();
		if (defaultTreeType != null && defaultTreeType.getDefaultTreeNode() != null) {
			contract.setWorkPosition(defaultTreeType.getDefaultTreeNode().getId());
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
	public IdmIdentityContractDto getPrimeContract(UUID identityId) {
		Assert.notNull(identityId);
		//
		// find all identity working position
		List<IdmIdentityContract> contracts = repository.findAllByIdentity_Id(identityId, null);
		if (contracts.isEmpty()) {
			return null;
		}
		IdmIdentityContract primeContract = null;
		IdmTreeType defaultTreeType = treeTypeRepository.findOneByDefaultTreeTypeIsTrue();
		for (IdmIdentityContract contract : contracts) {
			// find main contract or contract with working position (default tree type has higher priority)
			if (contract.isMain()) {
				return toDto(contract);
			}
			if (primeContract == null) {
				primeContract = contract;
			}
			IdmTreeNode workPosition = contract.getWorkPosition();
			if (workPosition != null && defaultTreeType != null && defaultTreeType.equals(workPosition.getTreeType())) {
				return toDto(contract);
			}
		}
		return toDto(primeContract);
	}

	@Override
	public List<IdmIdentityContractDto> findAllValidForDate(UUID identityId, LocalDate date, Boolean onlyExterne) {
		return toDtos(this.repository.findAllValidContracts(identityId, date, onlyExterne), false);
	}
}

