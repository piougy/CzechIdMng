package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.internal.util.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.ConfidentialStorage;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.api.utils.RepositoryUtils;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent_;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState_;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityStateRepository;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * CRUD for entity states.
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmEntityStateService 
		extends AbstractEventableDtoService<IdmEntityStateDto, IdmEntityState, IdmEntityStateFilter> 
		implements IdmEntityStateService {

	@Autowired private ConfidentialStorage confidentialStorage;
	
	@Autowired
	public DefaultIdmEntityStateService(
			IdmEntityStateRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.ENTITYSTATE, getEntityClass());
	}

	@Override
	@Transactional
	public void deleteInternal(IdmEntityStateDto dto) {
		Assert.notNull(dto.getId(), "ID cannot be null!");

		// Referential integrity - delete all confidential storage values for this entity state.
		confidentialStorage.deleteAll(dto);
		
		super.deleteInternal(dto);
	}

	@Override
	protected List<Predicate> toPredicates(Root<IdmEntityState> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmEntityStateFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// "fulltext"
		String text = filter.getText();
		if (StringUtils.isNotEmpty(text)) {
			List<Predicate> textPredicates = new ArrayList<>(5);
			//
			text = text.toLowerCase();
			textPredicates.add(builder.like(builder.lower(root.get(IdmEntityState_.ownerType)), "%" + text + "%"));
			textPredicates.add(builder.like(builder.lower(root.get(IdmEntityState_.ownerId).as(String.class)), "%" + text + "%"));
			textPredicates.add(builder.like(builder.lower(root.get(IdmEntityState_.result).get(OperationResult_.code)), "%" + text + "%"));
			RepositoryUtils.appendUuidPredicate(textPredicates, root.get(IdmEntityState_.ownerId), builder, text);
			//
			predicates.add(builder.or(textPredicates.toArray(new Predicate[textPredicates.size()])));
		}
		//
		// owner type
		if (StringUtils.isNotEmpty(filter.getOwnerType())) {
			predicates.add(builder.equal(root.get(IdmEntityState_.ownerType), filter.getOwnerType()));
		}
		// owner id
		if (filter.getOwnerId() != null) {
			predicates.add(builder.equal(root.get(IdmEntityState_.ownerId), filter.getOwnerId()));
		}
		UUID superOwnerId = filter.getSuperOwnerId();
		if (superOwnerId != null) {
			predicates.add(builder.equal(root.get(IdmEntityState_.superOwnerId), superOwnerId));
		}
		// change id
		if (filter.getEventId() != null) {
			predicates.add(builder.equal(root.get(IdmEntityState_.event).get(IdmEntityEvent_.id), filter.getEventId()));
		}
		String resultCode = filter.getResultCode();
		if (StringUtils.isNotEmpty(resultCode)) {
			predicates.add(builder.equal(root.get(IdmEntityState_.result).get(OperationResult_.code), resultCode));
		}
		List<OperationState> states = filter.getStates();
		if (!states.isEmpty()) {
			predicates.add(root.get(IdmEntityState_.result).get(OperationResult_.state).in(states));
		}
		//
		return predicates;
	}

}
