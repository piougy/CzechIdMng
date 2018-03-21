package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent_;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityState_;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityStateRepository;

/**
 * CRUD for entity states
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmEntityStateService 
		extends AbstractEventableDtoService<IdmEntityStateDto, IdmEntityState, IdmEntityStateFilter> 
		implements IdmEntityStateService {

	@Autowired
	public DefaultIdmEntityStateService(
			IdmEntityStateRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmEntityState> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmEntityStateFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmEntityState_.ownerType)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmEntityState_.ownerId).as(String.class)), "%" + filter.getText().toLowerCase() + "%"))
					);
		}
		// owner type
		if (StringUtils.isNotEmpty(filter.getOwnerType())) {
			predicates.add(builder.equal(root.get(IdmEntityState_.ownerType), filter.getOwnerType()));
		}
		// owner id
		if (filter.getOwnerId() != null) {
			predicates.add(builder.equal(root.get(IdmEntityState_.ownerId), filter.getOwnerId()));
		}
		// change id
		if (filter.getEventId() != null) {
			predicates.add(builder.equal(root.get(IdmEntityState_.event).get(IdmEntityEvent_.id), filter.getEventId()));
		}
		if (filter.getCreatedFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmEntityState_.created), filter.getCreatedFrom()));
		}
		if (filter.getCreatedTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmEntityState_.created), filter.getCreatedTill()));
		}
		//
		return predicates;
	}

}
