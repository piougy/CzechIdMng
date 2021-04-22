package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.config.domain.TreeConfiguration;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTreeTypeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractSliceFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityContractFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityContractProcessor;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractSliceService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.eav.api.service.AbstractFormableService;
import eu.bcvsolutions.idm.core.eav.api.service.FormService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.event.IdentityContractEvent;
import eu.bcvsolutions.idm.core.model.repository.IdmIdentityContractRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity contract administration:
 * - Supports {@link IdentityContractEvent},
 * - identity contract is required for role assign.
 * 
 * @author Radek Tomiška
 * @see IdentityContractEvent
 * @see IdentityContractProcessor
 */
public class DefaultIdmIdentityContractService 
		extends AbstractFormableService<IdmIdentityContractDto, IdmIdentityContract, IdmIdentityContractFilter>
		implements IdmIdentityContractService {

	private final IdmIdentityContractRepository repository;
	//
	@Autowired private TreeConfiguration treeConfiguration;
	@Autowired private IdmContractSliceService contractSliceService;
	
	@Autowired
	public DefaultIdmIdentityContractService(
			IdmIdentityContractRepository repository,
			FormService formService,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager, formService);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.IDENTITYCONTRACT, getEntityClass());
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * @since 11.0.0
	 */
	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}
	
	@Override
	protected IdmIdentityContract toEntity(IdmIdentityContractDto dto, IdmIdentityContract entity) {
		IdmIdentityContract contract = super.toEntity(dto, entity);
		if (contract != null && dto != null) {
			contract.setDisabled(dto.isDisabled()); // redundant attribute for queries
		}
		return contract;
	}
	
	@Override
	protected IdmIdentityContractDto toDto(IdmIdentityContract entity, IdmIdentityContractDto dto, IdmIdentityContractFilter context) {
		IdmIdentityContractDto resultDto = super.toDto(entity, dto, context);
		// Set attribute if that contract was created by slices
		if (resultDto != null && resultDto.getId() != null && !resultDto.isTrimmed()) {
			IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
			sliceFilter.setParentContract(resultDto.getId());
			resultDto.setControlledBySlices(contractSliceService.count(sliceFilter) > 0);
		}
		return resultDto;
	}
	
	@Override
	protected IdmIdentityContractDto applyContext(IdmIdentityContractDto dto, IdmIdentityContractFilter context,
			BasePermission... permission) {
		dto = super.applyContext(dto, context, permission);
		if (dto == null || context == null) {
			return dto;
		}
		if (dto.getControlledBySlices() != null || !context.isAddControlledBySlices()) {
			// flag already initialized in toDto method
			return dto;
		}
		//
		IdmContractSliceFilter sliceFilter = new IdmContractSliceFilter();
		sliceFilter.setParentContract(dto.getId());
		dto.setControlledBySlices(contractSliceService.count(sliceFilter) > 0);
		//
		return dto;
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmIdentityContract> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmIdentityContractFilter filter) {
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
		if (filter.getMain() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.main), filter.getMain()));
		}
		if (filter.getValid() != null) {
			final LocalDate today = LocalDate.now();
			//
			if (filter.getValid()) {
				// EXCLUDED contracts remain valid ...
				predicates.add(
						builder.and(
								RepositoryUtils.getValidPredicate(root, builder, today),
								builder.equal(root.get(IdmIdentityContract_.disabled), Boolean.FALSE)
								)								
						);
			} else {
				predicates.add(
						builder.or(
								builder.lessThan(root.get(IdmIdentityContract_.validTill), today),
								builder.greaterThan(root.get(IdmIdentityContract_.validFrom), today),
								builder.equal(root.get(IdmIdentityContract_.disabled), Boolean.TRUE)
								)
						);
			}
		}
		Boolean validNowOrInFuture = filter.getValidNowOrInFuture();
		if (validNowOrInFuture != null) {
			if (validNowOrInFuture) {
				predicates.add(
						builder.and(
								builder.or(
										builder.greaterThanOrEqualTo(root.get(IdmIdentityContract_.validTill), LocalDate.now()),
										builder.isNull(root.get(IdmIdentityContract_.validTill))
										),
								builder.equal(root.get(IdmIdentityContract_.disabled), Boolean.FALSE)
							));
			} else {
				predicates.add(builder.lessThan(root.get(IdmIdentityContract_.validTill), LocalDate.now()));
			}
		}
		if (filter.getState() != null) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.state), filter.getState()));
		}
		if (StringUtils.isNotEmpty(filter.getPosition())) {
			predicates.add(builder.equal(root.get(IdmIdentityContract_.position), filter.getPosition()));
		}
		UUID workPosition = filter.getWorkPosition();
		if (workPosition != null) {
			RecursionType recursionType = filter.getRecursionType();
			if (recursionType == RecursionType.NO) {
				// NO recursion => equals on work position only.
				predicates.add(builder.equal(root.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.id), filter.getWorkPosition()));
			} else {
				// prepare subquery for tree nodes and index
				Subquery<IdmTreeNode> subqueryTreeNode = query.subquery(IdmTreeNode.class);
				Root<IdmTreeNode> subqueryTreeNodeRoot = subqueryTreeNode.from(IdmTreeNode.class);
				subqueryTreeNode.select(subqueryTreeNodeRoot);
				//
				if (recursionType == RecursionType.DOWN) {
					subqueryTreeNode.where(
							builder.and(
									builder.equal(subqueryTreeNodeRoot.get(IdmTreeNode_.id), workPosition),
									builder.equal(root.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType), subqueryTreeNodeRoot.get(IdmTreeNode_.treeType)),
									builder.between(
											root.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
		                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
		                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt)
		                    		)
							));
				} else { // UP
					subqueryTreeNode.where(
							builder.and(
									builder.equal(subqueryTreeNodeRoot.get(IdmTreeNode_.id), workPosition),
									builder.equal(root.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.treeType), subqueryTreeNodeRoot.get(IdmTreeNode_.treeType)),
									builder.between(
											subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
											root.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
											root.get(IdmIdentityContract_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt)
		                    		)
							));
				}
				//
				predicates.add(builder.exists(subqueryTreeNode));
			}
		}
		Boolean excluded = filter.getExcluded();
		if (excluded != null) {
			Predicate excludedPredicate = builder.equal(root.get(IdmIdentityContract_.state), ContractState.EXCLUDED);
			if (excluded) {
				predicates.add(excludedPredicate);
			} else {
				predicates.add(
						builder.or(
								builder.not(excludedPredicate),
								builder.isNull(root.get(IdmIdentityContract_.state))
								)
						);
			}
		}
		
		UUID roleId = filter.getRoleId();
		if (roleId != null) {
			Subquery<IdmIdentityRole> identityRoleSubquery = query.subquery(IdmIdentityRole.class);
			Root<IdmIdentityRole> subRootIdentityRole = identityRoleSubquery.from(IdmIdentityRole.class);
			identityRoleSubquery.select(subRootIdentityRole);
			
			identityRoleSubquery.where(
                    builder.and(
                    		builder.equal(subRootIdentityRole.get(IdmIdentityRole_.identityContract), root),
                    		builder.equal(subRootIdentityRole.get(IdmIdentityRole_.role).get(AbstractEntity_.id), roleId)
                    		));
			predicates.add(builder.exists(identityRoleSubquery));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityContractDto> findAllByIdentity(UUID identityId) {
		return toDtos(repository.findAllByIdentity_Id(identityId, Sort.by(IdmIdentityContract_.validFrom.getName())), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmIdentityContractDto> findAllByWorkPosition(UUID workPositionId, RecursionType recursionType) {
		Assert.notNull(workPositionId, "Work position is required to get related contracts.");
		//
		IdmIdentityContractFilter filter = new IdmIdentityContractFilter();
		filter.setWorkPosition(workPositionId);
		filter.setRecursionType(recursionType);
		//
		return find(filter, null).getContent();
	}

	@Override
	@Transactional(readOnly = true)
	public Page<IdmIdentityContractDto> findExpiredContracts(LocalDate expiration, Pageable pageable) {
		return toDtoPage(repository.findExpiredContracts(expiration, pageable));
	}

	@Override
	public IdmIdentityContractDto prepareMainContract(UUID identityId) {
		Assert.notNull(identityId, "Identity identifier is required for preparing main contract");
		//
		// set identity
		IdmIdentityContractDto contract = new IdmIdentityContractDto();
		contract.setIdentity(identityId);
		contract.setMain(true);
		//
		// set default contract name
		// TODO: add boolean attribute
		contract.setPosition(DEFAULT_POSITION_NAME);
		//
		// set working position
		IdmTreeNodeDto defaultTreeNode = treeConfiguration.getDefaultNode();
		if (defaultTreeNode != null) {
			contract.setWorkPosition(defaultTreeNode.getId());
		}
		return contract;
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentityContractDto getPrimeContract(UUID identityId) {
		Assert.notNull(identityId, "Identity identifier is required for get prime contract");
		//
		// find all identity working position
		List<IdmIdentityContract> contracts = repository.findAllByIdentity_Id(identityId, null);
		if (contracts.isEmpty()) {
			return null;
		}
		List<IdmIdentityContractDto> contractDtos = toDtos(contracts, true);
		sortByPrimeContract(contractDtos);
		// return contract with the highest priority + not trimmed
		IdmIdentityContractDto primeContractDto = contractDtos.get(0);
		return toDto(contracts
				.stream()
				.filter(c -> c.getId().equals(primeContractDto.getId()))
				.findFirst()
				.get());
	}
	
	@Override
	@Transactional(readOnly = true)
	public IdmIdentityContractDto getPrimeValidContract(UUID identityId) {
		Assert.notNull(identityId, "Identity identifier is required for get prime valid contract");
		//
		// find valid all identity working position
		List<IdmIdentityContract> contracts = repository.findAllValidContracts(identityId, LocalDate.now(), null);
		if (contracts.isEmpty()) {
			return null;
		}
		List<IdmIdentityContractDto> contractDtos = toDtos(contracts, true);
		sortByPrimeContract(contractDtos);
		// return contract with the highest priority + not trimmed
		IdmIdentityContractDto primeContractDto = contractDtos.get(0);
		return toDto(contracts
				.stream()
				.filter(c -> c.getId().equals(primeContractDto.getId()))
				.findFirst()
				.get());
	}

	@Override
	public List<IdmIdentityContractDto> findAllValidForDate(UUID identityId, LocalDate date, Boolean isExterne) {
		return toDtos(this.repository.findAllValidContracts(identityId, date, isExterne), false);
	}

	@Override
	public IdmIdentityContractDto findLastExpiredContract(UUID identityId, LocalDate expiration) {
		Assert.notNull(identityId, "Identity identifier is required for get last expired contract");
		//
		List<IdmIdentityContract> contracts = repository
				.findExpiredContractsByIdentity(
						identityId,
						expiration == null ? LocalDate.now() : expiration,
						PageRequest.of(0, 1, new Sort(Sort.Direction.DESC, IdmIdentityContract_.validTill.getName()))		
				)
				.getContent();
		//
		return contracts.isEmpty() ? null : toDto(contracts.get(0));
	}
	
	@Override
	public void sortByPrimeContract(List<IdmIdentityContractDto> contracts) {
		if (CollectionUtils.isEmpty(contracts)) {
			return;
		}
		//
		Collections.sort(contracts, new PrimeIdentityContractComparator(treeConfiguration.getDefaultType()));
		// lookout - reversed comparator doesn't work.
		Collections.reverse(contracts);
	}
	
	/**
	 * Returns contracts sorted by priority:
	 * - 1. main
	 * - 2. valid (validable and not disabled)
	 * - 3. with working position with default tree type
	 * - 4. with working position with any tree type
	 * - 5. with undefined valid from
	 * - 6. other with lowest valid from
	 * - 7. by created date
	 * 
	 * @author Radek Tomiška
	 *
	 */
	private static class PrimeIdentityContractComparator implements Comparator<IdmIdentityContractDto> {

		private final IdmTreeTypeDto defaultTreeType;
		
		public PrimeIdentityContractComparator(IdmTreeTypeDto defaultTreeType) {
			this.defaultTreeType = defaultTreeType;
		}
		
		@Override
		public int compare(IdmIdentityContractDto o1, IdmIdentityContractDto o2) {
			CompareToBuilder builder = new CompareToBuilder();
			// main
			builder.append(o1.isMain(), o2.isMain());
			// not disabled
			builder.append(!o1.isDisabled(), !o2.isDisabled());
			// valid
			builder.append(o1.isValid(), o2.isValid());
			// with default tree position
			if (defaultTreeType != null) {
				UUID treeType1 = null;
				if (o1.getWorkPosition() != null) {
					IdmTreeNodeDto node = DtoUtils.getEmbedded(o1, IdmIdentityContractDto.PROPERTY_WORK_POSITION);
					treeType1 = node.getTreeType();
				}
				UUID treeType2 = null;
				if (o2.getWorkPosition() != null) {
					IdmTreeNodeDto node = DtoUtils.getEmbedded(o2, IdmIdentityContractDto.PROPERTY_WORK_POSITION);
					treeType2 = node.getTreeType();
				}
				
				builder.append(treeType1 != null && treeType1.equals(defaultTreeType.getId()), 
						treeType2 != null && treeType2.equals(defaultTreeType.getId()));
			}			
			// with any tree position
			builder.append(o1.getWorkPosition() != null, o2.getWorkPosition() != null);
			// by the less valid from (or undefined)
			builder.append(o2.getValidFrom(), o1.getValidFrom());
			// by the less created
			builder.append(o2.getCreated(), o1.getCreated());
			//
			return builder.toComparison();
		}
	}
}

