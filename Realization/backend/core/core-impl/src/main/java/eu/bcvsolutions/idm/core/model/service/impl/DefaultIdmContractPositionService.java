package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
import eu.bcvsolutions.idm.core.model.entity.IdmForestIndexEntity_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentity_;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode;
import eu.bcvsolutions.idm.core.model.entity.IdmTreeNode_;
import eu.bcvsolutions.idm.core.model.repository.IdmContractPositionRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Identity's contract other position.
 * 
 * @author Radek Tomiška
 * @since 9.1.0
 */
public class DefaultIdmContractPositionService 
		extends AbstractEventableDtoService<IdmContractPositionDto, IdmContractPosition, IdmContractPositionFilter> 
		implements IdmContractPositionService {
	
	@Autowired
	public DefaultIdmContractPositionService(
			IdmContractPositionRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CONTRACTPOSITION, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmContractPositionDto> findAllByWorkPosition(UUID workPositionId, RecursionType recursionType) {
		Assert.notNull(workPositionId, "Work position is required to get related contracts.");
		//
		IdmContractPositionFilter filter = new IdmContractPositionFilter();
		filter.setWorkPosition(workPositionId);
		filter.setRecursionType(recursionType);
		//
		return find(filter, null).getContent();
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmContractPosition> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmContractPositionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// contract id
		UUID contractId = filter.getIdentityContractId();
		if (contractId != null) {
			predicates.add(builder.equal(root.get(IdmContractPosition_.identityContract).get(IdmIdentityContract_.id), contractId));
		}
		// tree node id
		UUID workPosition = filter.getWorkPosition();
		if (workPosition != null) {
			RecursionType recursionType = filter.getRecursionType();
			if (recursionType == RecursionType.NO) {
				// NO recursion => equals on work position only.
				predicates.add(builder.equal(root.get(IdmContractPosition_.workPosition).get(IdmTreeNode_.id), workPosition));
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
									builder.equal(root.get(IdmContractPosition_.workPosition).get(IdmTreeNode_.treeType), subqueryTreeNodeRoot.get(IdmTreeNode_.treeType)),
									builder.between(
											root.get(IdmContractPosition_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
		                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
		                    				subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt)
		                    		)
							));
				} else { // UP
					subqueryTreeNode.where(
							builder.and(
									builder.equal(subqueryTreeNodeRoot.get(IdmTreeNode_.id), workPosition),
									builder.equal(root.get(IdmContractPosition_.workPosition).get(IdmTreeNode_.treeType), subqueryTreeNodeRoot.get(IdmTreeNode_.treeType)),
									builder.between(
											subqueryTreeNodeRoot.get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft), 
											root.get(IdmContractPosition_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.lft),
											root.get(IdmContractPosition_.workPosition).get(IdmTreeNode_.forestIndex).get(IdmForestIndexEntity_.rgt)
		                    		)
							));
				}
				//
				predicates.add(builder.exists(subqueryTreeNode));
			}
		}
		UUID identity = filter.getIdentity();
		if (identity != null) {
			predicates.add(builder.equal(
					root
						.get(IdmContractPosition_.identityContract)
						.get(IdmIdentityContract_.identity)
						.get(IdmIdentity_.id), 
					identity)
			);
		}
		Boolean validNowOrInFuture = filter.getValidNowOrInFuture();
		if (validNowOrInFuture != null) {
			Path<IdmIdentityContract> pathContract = root.get(IdmContractPosition_.identityContract);
			//
			if (validNowOrInFuture) {
				predicates.add(
						builder.and(
								builder.or(
										builder.greaterThanOrEqualTo(pathContract.get(IdmIdentityContract_.validTill), LocalDate.now()),
										builder.isNull(pathContract.get(IdmIdentityContract_.validTill))
										),
								builder.equal(pathContract.get(IdmIdentityContract_.disabled), Boolean.FALSE)
							));
			} else {
				predicates.add(builder.lessThan(pathContract.get(IdmIdentityContract_.validTill), LocalDate.now()));
			}
		}
		//
		return predicates;
	}
}
