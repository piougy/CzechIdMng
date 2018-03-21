package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.exception.EventDeleteFailedHasChildrenException;
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent_;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityEventRepository;

/**
 * CRUD for entity changes
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public class DefaultIdmEntityEventService 
		extends AbstractEventableDtoService<IdmEntityEventDto, IdmEntityEvent, IdmEntityEventFilter> 
		implements IdmEntityEventService {

	private IdmEntityEventRepository repository;
	//
	// @Autowired private ConfidentialStorage confidentialStorage;
	@Autowired private IdmEntityStateService entityStateService;
	
	@Autowired
	public DefaultIdmEntityEventService(
			IdmEntityEventRepository repository,
			EntityEventManager entityEventManager) {
		super(repository, entityEventManager);
		//
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public List<IdmEntityEventDto> findByState(String instanceId, OperationState state) {
		return toDtos(repository.findByInstanceIdAndResult_StateOrderByCreatedAsc(instanceId, state), false);
	}
	
	@Override
	@Transactional(readOnly = true)
	public Page<IdmEntityEventDto> findToExecute(
			String instanceId,
			DateTime executeDate,
			PriorityType priority,
			Pageable pageable) {
		return toDtoPage(repository.findToExecute(instanceId, OperationState.CREATED, executeDate, priority, pageable));
	}
	
	@Override
	@Transactional
	public IdmEntityEventDto saveInternal(IdmEntityEventDto dto) {
//		ConfigurationMap eventProperties = dto.getEventProperties();
		dto = super.saveInternal(dto);
		// TODO ...
//		if (eventProperties.isEmpty()) {
//			confidentialStorage.save(dto.getId(), getEntityClass(), "core:properties", password.asString());
//		}
		//		
		return dto;
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmEntityEventDto dto) {
		if (repository.countByParentId(dto.getId()) > 0) {
			throw new EventDeleteFailedHasChildrenException(dto);
		}		
		// delete states
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setEventId(dto.getId());
		entityStateService.find(filter, null).forEach(state -> {
			entityStateService.delete(state);
		});
		//
		// TODO: delete confidential properties
		//
		super.deleteInternal(dto);
		// delete parent if children count is one (=> removed dto only)
		if (dto.getParent() != null) {
			if(repository.countByParentId(dto.getParent()) == 0) {
				// delete parent 
				deleteById(dto.getParent());
			}
		}
	}
	
	/**
	 * Returns fully loaded event properties (with confidential properties)
	 * @param changeId
	 * @return
	 */
	public ConfigurationMap getEventProperties(IdmEntityEventDto change) {
		Assert.notNull(change);
		//
		// TODO: return confidentialStorage.getGuardedString(requestId, getEntityClass(), PROPERTY_PASSWORD);
		return change.getProperties();
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmEntityEvent> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmEntityEventFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmEntityEvent_.ownerType)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmEntityEvent_.ownerId).as(String.class)), "%" + filter.getText().toLowerCase() + "%"))
					);
		}
		// owner type
		if (StringUtils.isNotEmpty(filter.getOwnerType())) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.ownerType), filter.getOwnerType()));
		}
		// owner id
		if (filter.getOwnerId() != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.ownerId), filter.getOwnerId()));
		}
		if (filter.getCreatedFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmEntityEvent_.created), filter.getCreatedFrom()));
		}
		if (filter.getCreatedTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmEntityEvent_.created), filter.getCreatedTill()));
		}
		if (!filter.getStates().isEmpty()) {
			predicates.add(root.get(IdmEntityEvent_.result).get(OperationResult_.state).in(filter.getStates()));
		}
		if (filter.getParentId() != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.parent).get(IdmEntityEvent_.id), filter.getParentId()));
		}
		if (filter.getPriority() != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.priority), filter.getPriority()));
		}
		//
		return predicates;
	}
}
