package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.time.ZonedDateTime;
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
import eu.bcvsolutions.idm.core.api.service.AbstractEventableDtoService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.IdmEntityStateService;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent;
import eu.bcvsolutions.idm.core.model.entity.IdmEntityEvent_;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityEventRepository;
import eu.bcvsolutions.idm.core.model.repository.IdmEntityStateRepository;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;

/**
 * CRUD for entity changes.
 * 
 * TODO: add support for save confidential properties.
 * 
 * @author Radek Tomiška
 * @since 8.0.0
 */
public class DefaultIdmEntityEventService 
		extends AbstractEventableDtoService<IdmEntityEventDto, IdmEntityEvent, IdmEntityEventFilter> 
		implements IdmEntityEventService {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultIdmEntityEventService.class);
	private final IdmEntityEventRepository repository;
	//
	// @Autowired private ConfidentialStorage confidentialStorage;
	@Autowired private IdmEntityStateService entityStateService;
	@Autowired private IdmEntityStateRepository entityStateRepository;
	@Autowired private SecurityService securityService;
	
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
			ZonedDateTime executeDate, 
			PriorityType priority,
			List<UUID> exceptOwnerIds, 
			Pageable pageable) {
		if (CollectionUtils.isEmpty(exceptOwnerIds)) {
			return toDtoPage(repository.findToExecute(instanceId, OperationState.CREATED, executeDate, priority, pageable));
		}
		if (exceptOwnerIds.size() > 500) {
			throw new IllegalArgumentException(String.format("Except owners size exceeded, given [%s], max [%s]", exceptOwnerIds.size(), 500));
		}
		return toDtoPage(repository.findToExecute(instanceId, OperationState.CREATED, executeDate, priority, exceptOwnerIds, pageable));
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
		// delete child events - by parent
		IdmEntityEventFilter filter = new IdmEntityEventFilter();
		filter.setParentId(dto.getId());
		find(filter, null).forEach(childEvent -> {
			deleteInternal(childEvent);
		});
		//
		// delete states
		IdmEntityStateFilter stateFilter = new IdmEntityStateFilter();
		stateFilter.setEventId(dto.getId());
		entityStateService.find(stateFilter, null).forEach(state -> {
			entityStateService.delete(state);
		});
		//
		// TODO: delete confidential properties
		//
		super.deleteInternal(dto);
	}
	
	/**
	 * Returns fully loaded event properties (with confidential properties)
	 * @param event
	 * @return
	 */
	public ConfigurationMap getEventProperties(IdmEntityEventDto event) {
		Assert.notNull(event, "Event is required to get properties");
		//
		// TODO: return confidentialStorage.getGuardedString(requestId, getEntityClass(), PROPERTY_PASSWORD);
		return event.getProperties();
	}
	
	@Override
	@Transactional
	public void deleteAll() {
		LOG.warn("Entity events were truncated by identity [{}].", securityService.getCurrentId());
		//
		entityStateRepository.deleteByEventIsNotNull();
		repository.deleteAll();
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmEntityEvent> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmEntityEventFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmEntityEvent_.ownerType)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmEntityEvent_.ownerId).as(String.class)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmEntityEvent_.id).as(String.class)), "%" + filter.getText().toLowerCase() + "%"))
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
		UUID superOwnerId = filter.getSuperOwnerId();
		if (superOwnerId != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.superOwnerId), superOwnerId));
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
		UUID rootId = filter.getRootId();
		if (rootId != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.rootId), rootId));
		}
		UUID parentId = filter.getParentId();
		if (parentId != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.parent).get(IdmEntityEvent_.id), parentId));
		}
		if (filter.getPriority() != null) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.priority), filter.getPriority()));
		}
		String resultCode = filter.getResultCode();
		if (StringUtils.isNotEmpty(resultCode)) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.result).get(OperationResult_.code), resultCode));
		}
		String eventType = filter.getEventType();
		if (StringUtils.isNotEmpty(eventType)) {
			predicates.add(builder.equal(root.get(IdmEntityEvent_.eventType), eventType));
		}
		//
		return predicates;
	}
}
