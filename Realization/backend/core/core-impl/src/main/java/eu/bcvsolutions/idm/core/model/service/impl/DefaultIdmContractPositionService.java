package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.RecursionType;
import eu.bcvsolutions.idm.core.api.dto.IdmContractPositionDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmContractPositionFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmContractPositionService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition;
import eu.bcvsolutions.idm.core.model.entity.IdmContractPosition_;
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
	
	private final IdmContractPositionRepository repository;
	
	@Autowired
	public DefaultIdmContractPositionService(
			IdmContractPositionRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.CONTRACTPOSITION, getEntityClass());
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmContractPositionDto> findAllByWorkPosition(UUID workPositionId, RecursionType recursion) {
		Assert.notNull(workPositionId, "Work position is required to gen related contracts.");
		//
		return toDtos(repository.findAllByWorkPosition(workPositionId, recursion == null ? RecursionType.NO : recursion), false);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmContractPosition> root, CriteriaQuery<?> query, CriteriaBuilder builder, IdmContractPositionFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		// contract id
		UUID contractId = filter.getIdentityContractId();
		if (contractId != null) {
			predicates.add(builder.equal(root.get(IdmContractPosition_.identityContract).get(AbstractEntity_.id), contractId));
		}
		// tree node id
		UUID workPosition = filter.getWorkPosition();
		if (workPosition != null) {
			predicates.add(builder.equal(root.get(IdmContractPosition_.workPosition).get(AbstractEntity_.id), workPosition));
		}
		return predicates;
	}
}
