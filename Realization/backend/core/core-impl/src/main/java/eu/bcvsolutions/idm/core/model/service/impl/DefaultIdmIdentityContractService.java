package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdentityContractFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.eav.service.api.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeType;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeNodeRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmTreeTypeRepository;
import eu.bcvsolutions.idm.core.model.service.api.IdmIdentityContractService;
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
		extends AbstractEventableDtoService<IdmIdentityContractDto, IdmIdentityContract, IdentityContractFilter>
		implements IdmIdentityContractService {

	private final IdmIdentityContractRepository repository;
	private final FormService formService;
	private final IdmTreeTypeRepository treeTypeRepository;
	private final IdmTreeNodeRepository treeNodeRepository;
	
	@Autowired
	public DefaultIdmIdentityContractService(
			IdmIdentityContractRepository repository,
			FormService formService,
			EntityEventManager entityEventManager,
			IdmTreeTypeRepository treeTypeRepository,
			IdmTreeNodeRepository treeNodeRepository) {
		super(repository, entityEventManager);
		//
		Assert.notNull(formService);
		Assert.notNull(treeTypeRepository);
		Assert.notNull(treeNodeRepository);
		//
		this.repository = repository;
		this.formService = formService;
		this.treeTypeRepository = treeTypeRepository;
		this.treeNodeRepository = treeNodeRepository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYCONTRACT, getEntityClass());
	}
	
	@Override
	public void deleteInternal(IdmIdentityContractDto dto) {
		// TODO: eav dto
		formService.deleteValues(getRepository().findOne(dto.getId()));
		//
		super.deleteInternal(dto);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityContract> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdentityContractFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
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
			predicates.add(builder.equal(root.get(IdmIdentityContract_.identity).get(AbstractEntity_.id), filter.getIdentity()));
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
		if (filter.getDisabled() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.disabled), filter.getDisabled()));
		}
		if (filter.getMain() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.main), filter.getMain()));
		}
		if (filter.getValid() != null && filter.getValid()) {
			final LocalDate today = LocalDate.now();
			predicates.add(
					builder.and(
							builder.or(
									builder.lessThanOrEqualTo(root.get(IdmIdentityContract_.validFrom), today),
									builder.isNull(root.get(IdmIdentityContract_.validFrom))
									),
							builder.or(
									builder.greaterThanOrEqualTo(root.get(IdmIdentityContract_.validTill), today),
									builder.isNull(root.get(IdmIdentityContract_.validTill))
									)
							)
					);
		}
		if (filter.getValid() != null && !filter.getValid()) {
			final LocalDate today = LocalDate.now();
			predicates.add(
					builder.or(
							builder.lessThan(root.get(IdmIdentityContract_.validTill), today),
							builder.greaterThan(root.get(IdmIdentityContract_.validFrom), today)
							)
					);
		}
		if (filter.getValidNowOrInFuture() != null) {
			if (filter.getValidNowOrInFuture()) {
				predicates.add(
							builder.or(
									builder.greaterThanOrEqualTo(root.get(IdmIdentityContract_.validTill), LocalDate.now()),
									builder.isNull(root.get(IdmIdentityContract_.validTill))
									)
						);
			} else {
				predicates.add(builder.lessThan(root.get(IdmIdentityContract_.validTill), LocalDate.now()));
			}
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
		// TODO: use uuid only - rewrite to subquery
		IdmTreeNode workPosition = treeNodeRepository.findOne(workPositionId);
		Assert.notNull(workPosition);
		//
		return toDtos(repository.findAllByWorkPosition(workPosition, recursion == null ? RecursionType.NO : recursion), false);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityContractDto> findExpiredContracts(LocalDate expiration, Pageable pageable) {
		return toDtoPage(repository.findExpiredContracts(expiration, pageable));
	}
	
	
	@Override
	@Deprecated
	public IdmIdentityContractDto prepareDefaultContract(UUID identityId) {
		return prepareMainContract(identityId);
	}

	@Override
	public IdmIdentityContractDto prepareMainContract(UUID identityId) {
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
	 * Returns given identity's prime contract, by contract's priority:
	 * - 1. main
	 * - 2. valid (validable and not disabled)
	 * - 3. with working position with default tree type
	 * - 4. with working position with any tree type
	 * - 5. other
	 * 
	 * @param identityId
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
		Collections.sort(contracts, new PrimeIdentityContractComparator(treeTypeRepository.findOneByDefaultTreeTypeIsTrue()));
		// return contract with the highest priority
		return toDto(contracts.get(contracts.size() - 1));
	}

	@Override
	public List<IdmIdentityContractDto> findAllValidForDate(UUID identityId, LocalDate date, Boolean onlyExterne) {
		return toDtos(this.repository.findAllValidContracts(identityId, date, onlyExterne), false);
	}
	
	/**
	 * Returns contracts sorted by priority:
	 * - 1. main
	 * - 2. valid (validable and not disabled)
	 * - 3. with working position with default tree type
	 * - 4. with working position with any tree type
	 * - 5. other with lowest valid from
	 * 
	 * @author Radek Tomiška
	 *
	 */
	private static class PrimeIdentityContractComparator implements Comparator<IdmIdentityContract> {

		private final IdmTreeType defaultTreeType;
		
		public PrimeIdentityContractComparator(IdmTreeType defaultTreeType) {
			this.defaultTreeType = defaultTreeType;
		}
		
		@Override
		public int compare(IdmIdentityContract o1, IdmIdentityContract o2) {
			CompareToBuilder builder = new CompareToBuilder();
			// main
			builder.append(o1.isMain(), o2.isMain());
			// valid 
			builder.append(!o1.isDisabled(), !o2.isDisabled());
			// not disabled
			builder.append(o1.isValid(), o2.isValid());
			// with default tree position
			if (defaultTreeType != null) {
				builder.append(o1.getWorkPosition() != null && o1.getWorkPosition().getTreeType().equals(defaultTreeType), 
						o2.getWorkPosition() != null && o2.getWorkPosition().getTreeType().equals(defaultTreeType));
			}			
			// with any tree position
			builder.append(o1.getWorkPosition() != null, o2.getWorkPosition() != null);
			// by valid from
			builder.append(o1.getValidFrom(), o2.getValidFrom());
			//
			return builder.toComparison();
		}
		
	}
}

