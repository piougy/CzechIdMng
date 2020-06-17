package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.dto.IdmDelegationDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmDelegationFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmDelegationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegationDefinition_;
import eu.bcvsolutions.idm.core.model.entity.IdmDelegation_;
import eu.bcvsolutions.idm.core.model.repository.IdmDelegationRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * CRUD service for delegations.
 *
 * @author Vít Švanda
 * @since 10.4.0
 *
 */
@Service("delegationService")
public class DefaultIdmDelegationService extends
		AbstractEventableDtoService<IdmDelegationDto, IdmDelegation, IdmDelegationFilter> implements IdmDelegationService {

	@Autowired
	LookupService lookupService;

	@Autowired
	public DefaultIdmDelegationService(IdmDelegationRepository repository, EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.DELEGATIONDEFINITION, getEntityClass());
	}

	@Override
	public boolean supportsToDtoWithFilter() {
		return true;
	}

	@Override
	protected IdmDelegationDto toDto(IdmDelegation entity, IdmDelegationDto dto, IdmDelegationFilter filter) {
		dto = super.toDto(entity, dto, filter);

		if (filter != null && filter.isIncludeOwner() && dto != null && dto.getOwnerType() != null && dto.getOwnerId() != null) {
			BaseDto ownerDto = lookupService.lookupDto(dto.getOwnerType(), dto.getOwnerId());
			dto.setOwnerDto(ownerDto);
		}

		return dto;
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmDelegation> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmDelegationFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);

		UUID delegateId = filter.getDelegateId();
		if (delegateId != null) {
			predicates.add(builder.equal(root.get(IdmDelegation_.definition).get(IdmDelegationDefinition_.delegate).get(AbstractEntity_.id), delegateId));
		}

		UUID delegatorId = filter.getDelegatorId();
		if (delegatorId != null) {
			predicates.add(builder.equal(root.get(IdmDelegation_.definition).get(IdmDelegationDefinition_.delegator).get(AbstractEntity_.id), delegatorId));
		}

		UUID delegatorContractId = filter.getDelegatorContractId();
		if (delegatorContractId != null) {
			predicates.add(builder.equal(
					root.get(IdmDelegation_.definition).get(IdmDelegationDefinition_.delegatorContract).get(AbstractEntity_.id),
					delegatorContractId));
		}

		String ownerType = filter.getOwnerType();
		if (ownerType != null) {
			predicates.add(builder.equal(root.get(IdmDelegation_.ownerType), ownerType));
		}

		UUID ownerId = filter.getOwnerId();
		if (ownerId != null) {
			predicates.add(builder.equal(root.get(IdmDelegation_.ownerId), ownerId));
		}

		UUID definitionId = filter.getDelegationDefinitionId();
		if (definitionId != null) {
			predicates.add(builder.equal(root.get(IdmDelegation_.definition).get(AbstractEntity_.id), definitionId));
		}

		OperationState operationState = filter.getOperationState();
		if (operationState != null) {
			predicates.add(builder.equal(root.get(IdmDelegation_.ownerState).get(OperationResult_.state), operationState));
		}

		return predicates;
	}
}
