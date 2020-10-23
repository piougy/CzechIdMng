package eu.bcvsolutions.idm.core.model.service.impl;

import eu.bcvsolutions.idm.core.api.event.EntityEventLock;
import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.config.cache.domain.ValueWrapper;
import eu.bcvsolutions.idm.core.api.config.domain.EventConfiguration;
import eu.bcvsolutions.idm.core.api.domain.Auditable;
import eu.bcvsolutions.idm.core.api.domain.ConfigurationMap;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.Identifiable;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.PriorityType;
import eu.bcvsolutions.idm.core.api.domain.TransactionContext;
import eu.bcvsolutions.idm.core.api.domain.TransactionContextHolder;
import eu.bcvsolutions.idm.core.api.domain.comparator.CreatedComparator;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.BaseDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.EntityEventProcessorDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityEventDto;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.EntityEventProcessorFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityEventFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity;
import eu.bcvsolutions.idm.core.api.event.AsyncEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEvent.CoreEventType;
import eu.bcvsolutions.idm.core.api.event.DefaultEventContext;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EmptyEntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EntityEventEvent.EntityEventType;
import eu.bcvsolutions.idm.core.api.event.EntityEventProcessor;
import eu.bcvsolutions.idm.core.api.event.EventContext;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.EventType;
import eu.bcvsolutions.idm.core.api.exception.EventContentDeletedException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmCacheManager;
import eu.bcvsolutions.idm.core.api.service.IdmEntityEventService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.scheduler.api.config.SchedulerConfiguration;
import eu.bcvsolutions.idm.core.scheduler.api.service.LongRunningTaskExecutor;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;
import eu.bcvsolutions.idm.core.security.api.service.ExceptionProcessable;
import eu.bcvsolutions.idm.core.security.api.service.SecurityService;
import eu.bcvsolutions.idm.core.security.api.utils.IdmAuthorityUtils;

/**
 * Entity (dto) processing based on event publishing.
 * 
 * TODO: remove duplicate events operation though whole queue (e.q. stopProcessing -> synchronize all entities -> deduplicate -> startProcessing).
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultEntityEventManager implements EntityEventManager {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultEntityEventManager.class);
	private static final ConcurrentHashMap<UUID, UUID> runningOwnerEvents = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<UUID, List<LongRunningTaskExecutor<?>>> lrts = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<UUID, Boolean> notifiedLrts = new ConcurrentHashMap<>();
	//
	@Autowired private ApplicationContext context;
	@Autowired private ApplicationEventPublisher publisher;
	@Autowired private ModelMapper modelMapper;
	@Autowired private ObjectMapper mapper;
	@Autowired private IdmCacheManager cacheManager;
	@Autowired private EntityEventLock lock;
	//
	@Autowired @Lazy private EnabledEvaluator enabledEvaluator;
	@Autowired @Lazy private IdmEntityEventService entityEventService;
	@Autowired @Lazy private EntityStateManager entityStateManager;
	@Autowired @Lazy private SecurityService securityService;
	@Autowired @Lazy private EventConfiguration eventConfiguration;
	@Autowired @Lazy private LookupService lookupService;
	@Autowired @Lazy private ConfigurationService configurationService;
	
	/**
	 * Cancel all previously ran events
	 */
	@Override
	public void init() {
		LOG.info("Cancel unprocessed events - event was interrupt during instance restart");
		//
		String instanceId = configurationService.getInstanceId();
		entityEventService.findByState(instanceId, OperationState.RUNNING).forEach(event -> {
			LOG.info("Cancel unprocessed event [{}] - event was interrupt during instance [{}] restart", event.getId(), instanceId);
			//
			// cancel event
			ResultModel resultModel = new DefaultResultModel(
					CoreResultCode.EVENT_CANCELED_BY_RESTART, 
					ImmutableMap.of(
							"eventId", event.getId(),
							"eventType", event.getEventType(),
							"ownerId", String.valueOf(event.getOwnerId()),
							"instanceId", event.getInstanceId()));
			OperationResultDto result = new OperationResultDto.Builder(OperationState.CANCELED).setModel(resultModel).build();
			event.setResult(result);
			entityEventService.saveInternal(event);
			//
			// cancel event states		
			IdmEntityStateFilter filter = new IdmEntityStateFilter();
			filter.setEventId(event.getId());
			List<IdmEntityStateDto> states = entityStateManager.findStates(filter, null).getContent();
			states
				.stream()
				.filter(state -> {
					return OperationState.RUNNING == state.getResult().getState();
				})
				.forEach(state -> {		
					state.setResult(result);
					entityStateManager.saveState(null, state);
				});
		});
		//
		cacheManager.evictCache(TRANSACTION_EVENT_CACHE_NAME);
	}

	@Override
	@Transactional
	public <E extends Serializable> EventContext<E> process(EntityEvent<E> event) {
		return process(event, null);
	}

	@Override
	@Transactional
	@SuppressWarnings("unchecked")
	public <E extends Serializable> EventContext<E> process(EntityEvent<E> event, EntityEvent<?> parentEvent) {
		Assert.notNull(event, "Event is required for processing.");
		Serializable content = event.getContent();
		//
		LOG.info("Publishing event [{}]", event);
		//
		// continue suspended event
		event.getContext().setSuspended(false);
		//
		if (parentEvent != null) {
			event.setParentId(parentEvent.getId());
			event.setRootId(parentEvent.getRootId() == null ? parentEvent.getId() : parentEvent.getRootId());
			if (parentEvent.getPriority() != null 
					&& (event.getPriority() == null || event.getPriority().getPriority() < parentEvent.getPriority().getPriority())) {
				// parent has higher priority ... execute with the same priority as parent
				event.setPriority(parentEvent.getPriority());
			}
			// parent event type can be preset manually
			if (StringUtils.isEmpty(event.getParentType())) {
				event.setParentType(parentEvent.getType().name());
			}
			// propagate properties from parent to child event. 
			// properties need for internal event processing are ignored (see {@link EntityEvent} properties)
			propagateProperties(event, parentEvent);
		}
		//
		// read previous (original) dto source - usable in "check modification" processors
		if (event.getOriginalSource() == null && (content instanceof AbstractDto)) { // original source could be set externally
			AbstractDto contentDto = (AbstractDto) content;
			// works only for dto modification
			if (contentDto.getId() != null && lookupService.getDtoLookup(contentDto.getClass()) != null) {
				event.setOriginalSource((E) lookupService.lookupDto(contentDto.getClass(), contentDto.getId()));
			}
		}
		//
		// persist event if needed
		// event is persisted automatically, when parent event is persisted
		try {
			if (content instanceof BaseDto && event.getId() == null && event.getParentId() != null) {
				BaseDto dto = (BaseDto) content;
				if (dto.getId() == null) {
					// prepare id for new content - event is persisted before entity is persisted.
					dto.setId(UUID.randomUUID());
				}
				//
				IdmEntityEventDto preparedEvent = toDto(dto, (EntityEvent<AbstractDto>) event);
				preparedEvent.setResult(new OperationResultDto.Builder(OperationState.RUNNING).build()); // RUNNING => prevent to start by async task
				preparedEvent.setRootId(event.getRootId() == null ? event.getParentId() : event.getRootId());
				preparedEvent = entityEventService.save(preparedEvent);
				event.setId(preparedEvent.getId());
				//
				// prepared event is be executed
				CoreEvent<IdmEntityEventDto> executeEvent = new CoreEvent<>(EntityEventType.EXECUTE, preparedEvent);
				publisher.publishEvent(executeEvent);
				//
				// fill original event result
				E processedContent = (E) preparedEvent.getContent();
				if (processedContent != null) {
					event.setContent(processedContent);
				}
				event.getContext().addResult(new DefaultEventResult<E>(event, new EmptyEntityEventProcessor<E>()));
				//
				return completeEvent(event);
			} else {
				publisher.publishEvent(event); 
				//
				return completeEvent(event);
			}
		} catch (Exception ex) {
			completeEvent(event);
			//
			throw ex;
		}
	}
	
	@Override
	public boolean registerAsynchronousTask(LongRunningTaskExecutor<?> executor) {
		lock.lock();
		try {
			if (!isAsynchronous()) {
				return false;
			}
			//
			UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
			if (lrts.putIfAbsent(transactionId, new ArrayList<>()) == null) {
				notifiedLrts.remove(executor.getLongRunningTaskId());
				cacheManager.evictValue(TRANSACTION_EVENT_CACHE_NAME, transactionId);
			}
			lrts.get(transactionId).add(executor);
			//
			return true;
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public boolean deregisterAsynchronousTask(LongRunningTaskExecutor<?> executor) {
		if (!isAsynchronous()) {
			return true;
		}
		//
		if (notifiedLrts.containsKey(executor.getLongRunningTaskId())) {
			notifiedLrts.remove(executor.getLongRunningTaskId());
			return false;
		}
		//
		UUID transactionId = TransactionContextHolder.getContext().getTransactionId();
		ValueWrapper value = cacheManager.getValue(TRANSACTION_EVENT_CACHE_NAME, transactionId);
		if (value == null) {
			LOG.debug("Transaction id [{}] was processed already (synchronously or complete).", transactionId);
			lrts.remove(transactionId);
			return true;
		}
		//
		return false;
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<EntityEventProcessorDto> find(EntityEventProcessorFilter filter) {
		List<EntityEventProcessorDto> dtos = new ArrayList<>();
		Map<String, EntityEventProcessor> processors = context.getBeansOfType(EntityEventProcessor.class);
		for (Entry<String, EntityEventProcessor> entry : processors.entrySet()) {
			EntityEventProcessor<?> processor = entry.getValue();
			// entity event processor depends on module - we could not call any processor method
			// TODO: all processor should be returned - disabled controlled by filter
			if (!enabledEvaluator.isEnabled(processor)) {
				continue;
			}
			EntityEventProcessorDto dto = toDto(processor);
			//
			if (passFilter(dto, filter)) {
				dtos.add(dto);
			}
		}
		// sort by order
		Collections.sort(dtos, new Comparator<EntityEventProcessorDto>() {

			@Override
			public int compare(EntityEventProcessorDto one, EntityEventProcessorDto two) {
				return Integer.compare(one.getOrder(),two.getOrder());
			}
			
		});
		//
		LOG.debug("Returning [{}] registered entity event processors", dtos.size());
		//
		return dtos;
	}
	
	@Override
	public EntityEventProcessorDto get(String processorId) {
		EntityEventProcessor<?> processor = getProcessor(processorId);
		if (processor == null) {
			return null;
		}
		return toDto(processor);
	}
	
	@Override
	public EntityEventProcessor<?> getProcessor(String processorId) {
		Assert.notNull(processorId, "Procesor identifier is required.");
		//
		return (EntityEventProcessor<?>) context.getBean(processorId);
	}

	@Override
	public void publishEvent(Object event) {
		publisher.publishEvent(event);
	}
	
	@Override
	public <E extends Identifiable> void changedEntity(E owner) {
		changedEntity(owner, null);
	}
	
	@Override
	public <E extends Identifiable> void changedEntity(E owner, EntityEvent<? extends Identifiable> originalEvent) {
		Assert.notNull(owner, "Owner is needed for publish is changed event.");
		//
		IdmEntityEventDto notifyEvent = prepareEvent(owner, originalEvent);
		notifyEvent.setEventType(CoreEventType.NOTIFY.name());
		//
		publishNotify(notifyEvent, originalEvent);
	}
	
	@Override
	public void changedEntity(Class<? extends Identifiable> ownerType, UUID ownerId) {
		changedEntity(ownerType, ownerId, null);
	}
	
	@Override
	public void changedEntity(
			Class<? extends Identifiable> ownerType, 
			UUID ownerId, 
			EntityEvent<? extends Identifiable> originalEvent) {
		IdmEntityEventDto notifyEvent = prepareEvent(ownerType, ownerId, originalEvent);
		notifyEvent.setEventType(CoreEventType.NOTIFY.name());
		//
		publishNotify(notifyEvent, originalEvent);
	}
	
	/**
	 * Try put notify event into queue - event is put into queue, only if it's not executed synchronously.
	 * If event is executed synchronously, then processed notify event properties (if some processor was executed) are propagated into original event. 
	 * 
	 * @param notifyEvent
	 * @param originalEvent
	 */
	private void publishNotify(IdmEntityEventDto notifyEvent, EntityEvent<? extends Identifiable> originalEvent) {
		EventContext<?> processedContext = putToQueue(notifyEvent);
		if (originalEvent == null) {
			// original was not set - just notify event was published
			return;
		}
		if (processedContext == null) {
			// asynchronous
			return;
		}
		if (processedContext.getLastResult() == null) {
			// no processor listens
			return;
		}
		//
		// propagate properties of processed event into parent
		propagateProperties(originalEvent, processedContext.getLastResult().getEvent());
	}
	
	/**
	 * Spring schedule new task after previous task ended (don't run concurrently)
	 */
	@Scheduled(fixedDelayString = "${" + SchedulerConfiguration.PROPERTY_EVENT_QUEUE_PROCESS + ":" + SchedulerConfiguration.DEFAULT_EVENT_QUEUE_PROCESS + "}")
	public void scheduleProcessCreated() {
		if (!eventConfiguration.isAsynchronous()) {
			// asynchronous processing is disabled
			// prevent to debug some messages into log - usable for devs
			return;
		}
		// check running events queue is full already
		if (runningOwnerEvents.size() > eventConfiguration.getBatchSize()) {
			LOG.trace("Asynchronous running events queue is full, waiting for complete running events.");
			return;
		}
		//
		processCreated();
	}
	
	/**
	 * Process created events from event queue
	 * 
	 * @return
	 */
	protected int processCreated() {
		// calculate events to process
		String instanceId = configurationService.getInstanceId();
		List<IdmEntityEventDto> events = getCreatedEvents(instanceId);
		LOG.trace("Events to process [{}] on instance [{}].", events.size(), instanceId);
		for (IdmEntityEventDto event : events) {
			// adds @Transactional
			context.getBean(this.getClass()).executeEvent(event);
		}
		return events.size();
	}
	
	@Override
	public IdmEntityEventDto getEvent(EntityEvent<? extends Serializable> event) {
		Assert.notNull(event, "Event is required to be trasformed to DTO.");
		//
		UUID eventId = getEventId(event);
		if (eventId == null) {
			// event doesn't contain entity change - event is not based on entity change
			return null;
		}
		return getEvent(eventId);
	}
	
	@Override
	public IdmEntityEventDto getEvent(UUID eventId) {
		return entityEventService.get(eventId);
	}
	
	@Override
	public UUID getEventId(EntityEvent<? extends Serializable> event) {
		Assert.notNull(event, "Event is required for get their id.");
		//
		return event.getId();
	}
	
	@Override
	public boolean isRunnable(UUID eventId) {
		Assert.notNull(eventId, "Event identifier is required.");
		IdmEntityEventDto event = getEvent(eventId);
		//
		return isRunnable(event);
	}
	
	@Override
	public boolean isRunnable(IdmEntityEventDto event) {
		if (event == null) {
			return false;
		}
		//
		return event.getResult().getState().isRunnable();
	}
	
	@Override
	public String getOwnerType(Identifiable owner) {
		return lookupService.getOwnerType(owner);
	}
	
	@Override
	public String getOwnerType(Class<? extends Identifiable> ownerType) {
		return lookupService.getOwnerType(ownerType);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public AbstractDto findOwner(IdmEntityEventDto change) {
		try {
			Class<?> ownerType = Class.forName(change.getOwnerType());
			if (!AbstractEntity.class.isAssignableFrom(ownerType)) {
				throw new IllegalArgumentException(String.format("Owner type [%s] has to generalize [AbstractEntity]", ownerType));
			}
			//
			return (AbstractDto) lookupService.lookupDto((Class<? extends AbstractEntity>) ownerType, change.getOwnerId());
		} catch (ClassNotFoundException ex) {
			LOG.error("Class [{}] for entity change [{}] not found, module or type was uninstalled, returning null",
					change.getOwnerType(), change.getId());
			return null;
		}
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public AbstractDto findOwner(String ownerType, Serializable ownerId) {
		try {
			Class<?> ownerTypeClass = Class.forName(ownerType);
			if (!AbstractEntity.class.isAssignableFrom(ownerTypeClass)) {
				throw new IllegalArgumentException(String.format("Owner type [%s] has to generalize [AbstractEntity]", ownerType));
			}
			//
			return (AbstractDto) lookupService.lookupDto((Class<? extends AbstractEntity>) ownerTypeClass, ownerId);
		} catch (ClassNotFoundException ex) {
			LOG.error("Class [{}] for entity change [{}] not found, module or type was uninstalled, returning null",
					ownerType, ownerId);
			return null;
		}
	}
	
	@Override
	@Transactional
	public void executeEvent(IdmEntityEventDto event) {
		Assert.notNull(event, "Event is reqired to be event executed.");
		Assert.notNull(event.getOwnerId(), "Event owner identifier is reqired to be event executed.");
		if (!eventConfiguration.isAsynchronous()) {
			// synchronous processing
			// we don't persist events and their states
			process(new CoreEvent<>(EntityEventType.EXECUTE, event));
			return;
		}
		if (event.getPriority() == PriorityType.IMMEDIATE) {
			// synchronous processing
			// we don't persist events and their states
			// TODO: what about running event with the same owner? And events in queue for the same owner => no locking now, last wins 
			process(new CoreEvent<>(EntityEventType.EXECUTE, event));
			return;
		}
		//
		if (runningOwnerEvents.putIfAbsent(event.getOwnerId(), event.getId()) != null) {
			LOG.debug("Previous event [{}] for owner with id [{}] is currently processed.", 
					runningOwnerEvents.get(event.getOwnerId()), event.getOwnerId());
			// event will be processed in another scheduling			
			return;
		}
		// check super owner is not processed
		UUID superOwnerId = event.getSuperOwnerId();
		if (superOwnerId != null && !superOwnerId.equals(event.getOwnerId())) {			
			if (runningOwnerEvents.putIfAbsent(superOwnerId, event.getId()) != null) {
				LOG.debug("Previous event [{}] for super owner with id [{}] is currently processed.", 
						runningOwnerEvents.get(superOwnerId), superOwnerId);
				runningOwnerEvents.remove(event.getOwnerId());
				// event will be processed in another scheduling		
				return;
			}
		}
		// execute event in new thread asynchronously
		try {
			eventConfiguration.getExecutor().execute(new Runnable() {
				
				@Override
				@SuppressWarnings("unchecked")
				public void run() {
					// run as event creator
					securityService.setAuthentication(new IdmJwtAuthentication(
							new IdmIdentityDto(event.getCreatorId(), event.getCreator()),
							new IdmIdentityDto(event.getOriginalCreatorId(), event.getOriginalCreator()),
							null,
							ZonedDateTime.now(),
							Lists.newArrayList(IdmAuthorityUtils.getAdminAuthority()),
							null));
					// run under original transaction id - asynchronous processing continue the "user" transaction
					TransactionContextHolder.setContext(new TransactionContext(event.getTransactionId()));
					//
					LOG.debug("Executing event under user [{}] (admin authorities) and transaction [{}]", 
							securityService.getUsername(),
							TransactionContextHolder.getContext().getTransactionId());
					//
					try {
						process(new CoreEvent<>(EntityEventType.EXECUTE, event));
					} catch (Exception ex) {
						// exception handling only ... all processor should persist their own entity state (see AbstractEntityEventProcessor)
						ResultModel resultModel;
						if (ex instanceof ResultCodeException) {
							resultModel = ((ResultCodeException) ex).getError().getError();
						} else {
							resultModel = new DefaultResultModel(
									CoreResultCode.EVENT_EXECUTE_FAILED, 
									ImmutableMap.of(
											"eventId", event.getId(), 
											"eventType", String.valueOf(event.getEventType()),
											"ownerId", String.valueOf(event.getOwnerId()),
											"instanceId", String.valueOf(event.getInstanceId())));
						}		
						saveResult(event.getId(), new OperationResultDto
										.Builder(OperationState.EXCEPTION)
										.setCause(ex)
										.setModel(resultModel)
										.build());
						
						LOG.error(resultModel.toString(), ex);
						//
						// Sometimes should be the exception processed within owner service (for audit purpose in some request).
						// We check if owner service supports this feature (implements ExceptionProcessable).
						try {
							Class<?> ownerClass = Class.forName(event.getOwnerType());
							ReadDtoService<?, ?> dtoService = lookupService.getDtoService((Class<? extends Identifiable>) ownerClass);
							if (dtoService instanceof ExceptionProcessable) {
								ExceptionProcessable<?> exceptionProcessable = (ExceptionProcessable<?>) dtoService;
								// Propagate the exception
								exceptionProcessable.processException(event.getOwnerId(), ex);
							}
						} catch (ClassNotFoundException e) {
							// Only to the log
							LOG.error(e.getLocalizedMessage(), e);
						}
						
					} finally {
						LOG.trace("Event [{}] ends for owner with id [{}].", event.getId(), event.getOwnerId());
						removeRunningEvent(event);
					}
				}
			});
			//
			LOG.trace("Running event [{}] for owner with id [{}].", event.getId(), event.getOwnerId());
		} catch (RejectedExecutionException ex) {
			// thread pool queue is full - wait for another try
			removeRunningEvent(event);
		}
	}
	
	@Override
	public void processOnBackground(EntityEvent<? extends Identifiable> event) {
		Assert.notNull(event, "Event is required to be executed.");
		//
		putToQueue(prepareEvent(event.getContent(), event));
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmEntityEventDto saveEvent(IdmEntityEventDto entityEvent) {
		return entityEventService.save(entityEvent);
	}
	
	@Override
	public void deleteEvent(IdmEntityEventDto entityEvent) {
		entityEventService.delete(entityEvent);
		if (entityEvent.getResult().getState() == OperationState.RUNNING) {
			removeRunningEvent(entityEvent);
		}
	}
	
	@Override
	public synchronized void deleteAllEvents() {
		entityEventService.deleteAll();
		runningOwnerEvents.clear();
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmEntityEventDto saveResult(UUID eventId, OperationResultDto result) {
		Assert.notNull(eventId, "Event has to be persisted before result is persisted.");
		Assert.notNull(result, "Event result to persist is required.");
		IdmEntityEventDto entityEvent = entityEventService.get(eventId);
		Assert.notNull(entityEvent, "Event DTO has to be persisted before result is persisted.");
		//
		entityEvent.setResult(result);
		//
		return entityEventService.save(entityEvent);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public List<IdmEntityStateDto> saveStates(
			EntityEvent<?> event, 
			List<IdmEntityStateDto> previousStates,
			EventResult<?> result) {
		IdmEntityEventDto entityEvent = getEvent(event);
		List<IdmEntityStateDto> results = new ArrayList<>();
		if (entityEvent == null) {
			return results;
		}
		// simple drop - we don't need to find and update results, we'll create new ones
		if (previousStates != null && !previousStates.isEmpty()) {
			previousStates.forEach(state -> {
				entityStateManager.deleteState(state);
			});
		}
		//
		if (result == null) {
			IdmEntityStateDto state = new IdmEntityStateDto(entityEvent);
			// default result without model
			state.setResult(new OperationResultDto
					.Builder(OperationState.EXECUTED)
					.build());
			results.add(entityStateManager.saveState(null, state));
			return results;
		}
		if (result.getResults().isEmpty()) {
			results.add(entityStateManager.saveState(null, createState(entityEvent, result, new OperationResultDto.Builder(OperationState.EXECUTED).build())));
			return results;
		}
		result.getResults().forEach(opeartionResult -> {
			results.add(entityStateManager.saveState(null, createState(entityEvent, result, opeartionResult.toDto())));
		});
		//
		return results;
	}
	
	@Override
	public EntityEvent<? extends Identifiable> toEvent(IdmEntityEventDto entityEvent) {
		Identifiable content = null;
		// try to use persisted event content
		// only if type and id is the same as owner can be used
		if (entityEvent.getContent() != null 
				&& Objects.equal(getOwnerType(entityEvent.getContent().getClass()), entityEvent.getOwnerType())
				&& Objects.equal(entityEvent.getContent().getId(), entityEvent.getOwnerId())) {
			content = entityEvent.getContent();
		}
		if (content == null) {
			// content is not persisted - try to find actual entity
			content = findOwner(entityEvent);
		}
		if (content == null) {
			throw new EventContentDeletedException(entityEvent);
		}
		//
		Map<String, Serializable> eventProperties = entityEvent.getProperties().toMap();
		eventProperties.put(EntityEvent.EVENT_PROPERTY_EVENT_ID, entityEvent.getId());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_PRIORITY, entityEvent.getPriority());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_EXECUTE_DATE, entityEvent.getExecuteDate());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_PARENT_EVENT_TYPE, entityEvent.getParentEventType());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_PARENT_EVENT_ID, entityEvent.getParent());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_ROOT_EVENT_ID, entityEvent.getRootId());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_SUPER_OWNER_ID, entityEvent.getSuperOwnerId());
		eventProperties.put(EntityEvent.EVENT_PROPERTY_TRANSACTION_ID, entityEvent.getTransactionId());
		final String type = entityEvent.getEventType();
		DefaultEventContext<Identifiable> initContext = new DefaultEventContext<>();
		initContext.setProcessedOrder(entityEvent.getProcessedOrder());
		EventType eventType = (EventType) () -> type;
		EntityEvent<Identifiable> resurectedEvent = new CoreEvent<>(eventType, content, eventProperties, initContext);
		resurectedEvent.setOriginalSource(entityEvent.getOriginalSource());
		//
		return resurectedEvent;
	}
	
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<EntityEventProcessor> getEnabledProcessors(EntityEvent<?> event) {
		return context
				.getBeansOfType(EntityEventProcessor.class)
				.values()
				.stream()
				.filter(enabledEvaluator::isEnabled)
				.filter(processor -> !processor.isDisabled())
				.filter(processor -> processor.supports(event))
				.sorted(new AnnotationAwareOrderComparator())
				.collect(Collectors.toList());
	}
	
	/**
	 * Propagate properties from parent to child event.
	 * Properties need for internal event processing are ignored (see {@link EntityEvent} properties). 
	 * 
	 * @param event
	 * @param parentEvent
	 */
	@Override
	public void propagateProperties(EntityEvent<?> event, EntityEvent<?> parentEvent) {
		Assert.notNull(event, "Event is required.");
		Assert.notNull(parentEvent, "Parent event is required.");
		//
		// clone event properties from parent - only if absent
		getProperties(parentEvent.getProperties())
			.entrySet()
			.forEach(entry -> {
				event.getProperties().putIfAbsent(entry.getKey(), (Serializable) entry.getValue());
			});
	}
	
	@Override
	public IdmEntityEventDto saveEvent(EntityEvent<? extends Identifiable> event, OperationResultDto result) {
		Assert.notNull(event, "Event is required.");
		Identifiable content = event.getContent();
		Assert.notNull(content, "Event content is required.");
		//
		IdmEntityEventDto savedEvent = toDto(event);
		savedEvent.setOwnerId(lookupService.getOwnerId(content));
		savedEvent.setOwnerType(getOwnerType(content));
		savedEvent.setResult(result);
		//
		if (savedEvent.getPriority() == null) {
			savedEvent.setPriority(PriorityType.NORMAL);
		}
		//
		savedEvent = entityEventService.save(savedEvent);
		//
		event.setId(savedEvent.getId());
		event.setPriority(savedEvent.getPriority());
		//
		return savedEvent;
	}

	@Override
	public IdmEntityEventDto prepareEvent(Identifiable owner, EntityEvent<? extends Identifiable> originalEvent) {
		Assert.notNull(owner, "Owner is required.");
		Assert.notNull(owner.getId(), "Change can be published after entity id is assigned at least.");
		//
		IdmEntityEventDto event = prepareEvent(owner.getClass(), lookupService.getOwnerId(owner), originalEvent);
		event.setContent(owner);
		//
		return event;
	}
	
	@Override
	public void enable(String processorId) {
		setEnabled(processorId, true);
	}

	@Override
	public void disable(String processorId) {
		setEnabled(processorId, false);
	}

	@Override
	public void setEnabled(String processorId, boolean enabled) {
		setEnabled(getProcessor(processorId), enabled);
	}
	
	@Override
	public boolean isAsynchronous() {
		return eventConfiguration.isAsynchronous();
	}
	
	/**
	 * Returns true if some event for given owner currently running.
	 * 
	 * @param ownerId
	 * @return
	 */
	protected boolean isRunningOwner(UUID ownerId) {
		return runningOwnerEvents.containsKey(ownerId);
	}
	
	/**
	 * Evaluate event priority by registered processors
	 * 
	 * @param event
	 * @param registeredProcessors
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	protected PriorityType evaluatePriority(EntityEvent<?> event, List<EntityEventProcessor> registeredProcessors) {
		PriorityType priority = null;
		for (EntityEventProcessor processor : registeredProcessors) {
			if (!(processor instanceof AsyncEntityEventProcessor)) {
				continue;
			}
			AsyncEntityEventProcessor asyncProcessor = (AsyncEntityEventProcessor) processor; 
			PriorityType processorPriority = asyncProcessor.getPriority(event);
			if (processorPriority == null) {
				// processor doesn't vote about priority - preserve original event priority. 
				continue;
			}
			if (priority == null || processorPriority.getPriority() < priority.getPriority()) {
				priority = processorPriority;
			}
			if (priority == PriorityType.IMMEDIATE) {
				// nothing is higher
				break;
			}
		}
		//
		return priority;
	}
	
	/**
	 * Called from scheduler - concurrency is prevented.
	 * Returns events to process sorted by priority 7 / 3 (high / normal). 
	 * Immediate priority is executed synchronously.
	 * Cancel duplicate events (same type, owner and props) - last event is returned
	 * 
	 * @param instanceId
	 * @return
	 */
	protected List<IdmEntityEventDto> getCreatedEvents(String instanceId) {
		Assert.notNull(instanceId, "Server instance identifier is required.");
		//
		// already running owners are excluded (super owner is excluded too)
		List<UUID> exceptOwnerIds = Lists.newArrayList(runningOwnerEvents.keySet());
		exceptOwnerIds = exceptOwnerIds.subList(0, exceptOwnerIds.size() > 500 ? 500 : exceptOwnerIds.size()); // prevent sql queue size is exceeded
		//
		// load created events - high priority
		ZonedDateTime executeDate = ZonedDateTime.now();
		Page<IdmEntityEventDto> highEvents = entityEventService.findToExecute(
				instanceId,
				executeDate,
				PriorityType.HIGH,
				exceptOwnerIds,
				PageRequest.of(0, eventConfiguration.getBatchSize(), new Sort(Direction.ASC, Auditable.PROPERTY_CREATED)));
		// load created events - low priority
		Page<IdmEntityEventDto> normalEvents = entityEventService.findToExecute(
				instanceId,
				executeDate,
				PriorityType.NORMAL,
				exceptOwnerIds,
				PageRequest.of(0, eventConfiguration.getBatchSize(), new Sort(Direction.ASC, Auditable.PROPERTY_CREATED)));
		// merge events
		List<IdmEntityEventDto> events = new ArrayList<>();
		events.addAll(highEvents.getContent());
		events.addAll(normalEvents.getContent());
		// sort by created date
		events.sort(new CreatedComparator());
		//
		// cancel duplicates - by owner => properties has to be the same
		// execute the first event for each owner only - preserve events order
		Map<UUID, IdmEntityEventDto> distinctEvents = new LinkedHashMap<>();	
		events.forEach(event -> {
			if (!distinctEvents.containsKey(event.getOwnerId())) {
				// the first event
				distinctEvents.put(event.getOwnerId(), event);
			} else {
				// cancel duplicate older event 
				IdmEntityEventDto olderEvent = distinctEvents.get(event.getOwnerId());
				if (isDuplicate(olderEvent, event)) {
					// try to set higher priority
					if (olderEvent.getPriority() == PriorityType.HIGH) {
						event.setPriority(PriorityType.HIGH);
					}
					distinctEvents.put(event.getOwnerId(), event);
					//
					LOG.debug(new DefaultResultModel(
							CoreResultCode.EVENT_DUPLICATE_CANCELED, 
							ImmutableMap.of(
									"eventId", olderEvent.getId(), 
									"eventType", String.valueOf(olderEvent.getEventType()),
									"ownerId", String.valueOf(olderEvent.getOwnerId()),
									"instanceId", String.valueOf(olderEvent.getInstanceId()),
									"neverEventId", event.getId())).toString());
					//
					IdmEntityEventFilter eventFilter = new IdmEntityEventFilter();
					eventFilter.setParentId(olderEvent.getId());
					if (entityEventService.find(eventFilter, PageRequest.of(0, 1)).getTotalElements() == 0) {
						entityEventService.delete(olderEvent);
					}
				}
			}
		});
		// 
		// sort by priority
		events = distinctEvents
				.values()
				.stream()
				.sorted((o1, o2) -> {
					return Integer.compare(o1.getPriority().getPriority(), o2.getPriority().getPriority());
				})
				.collect(Collectors.toList());
		int normalCount = events.stream().filter(e -> e.getPriority() == PriorityType.NORMAL).collect(Collectors.toList()).size();
		int maxNormalCount = eventConfiguration.getBatchSize() / 3;
		
		int highMaximum = normalCount > maxNormalCount
				? (eventConfiguration.getBatchSize() - maxNormalCount)
				: (eventConfiguration.getBatchSize() - normalCount);
		// evaluate priority => high 70 / low 30
		int highCounter = 0;
		List<IdmEntityEventDto> prioritizedEvents = new ArrayList<>();
		for (IdmEntityEventDto event : events) {
			if (event.getPriority() == PriorityType.HIGH) {
				if (highCounter < highMaximum) {
					prioritizedEvents.add(event);
					highCounter++;
				}
			} else {
				// normal priority remains only
				if (prioritizedEvents.size() >= eventConfiguration.getBatchSize()) {
					break;
				}
				prioritizedEvents.add(event);
			}
		}
		//
		return prioritizedEvents;
	}
	
	/**
	 * Returns true, when events are duplicates
	 * - event type, parent event type, properties and original source is compared => all properties, which can be used in processors. 
	 * 
	 * @param olderEvent
	 * @param event
	 * @return
	 */
	protected boolean isDuplicate(IdmEntityEventDto olderEvent, IdmEntityEventDto event) {
		Assert.notNull(olderEvent, "Older event is required.");
		Assert.notNull(event, "Event is required.");
		//
		boolean result = Objects.equal(olderEvent.getEventType(), event.getEventType())
				&& Objects.equal(olderEvent.getParentEventType(), event.getParentEventType())
				&& Objects.equal(getProperties(olderEvent), getProperties(event));
		if (!result) {
			// we can end - events are different
			return false;
		}
		if (olderEvent.getOriginalSource() == null && event.getOriginalSource() == null) {
			return true;
		}
		// If all event properties match, we need to check original sources => some processors can use 
		// previous content to processing (e.g. contract processors).
		if (!Objects.equal(olderEvent.getOriginalSource(), event.getOriginalSource())) {
			return false;
		}
		if (!(olderEvent.getOriginalSource() instanceof AbstractDto)) {
			// Evaluated already by 'equal' method above.
			return false;
		}
		// If both original sources are DTOs, then we are comparing original sources (DTOs) as JSON without embedded.
		AbstractDto olderOriginalSource = (AbstractDto) olderEvent.getOriginalSource();
		AbstractDto originalSource = (AbstractDto) event.getOriginalSource();
		try {
			// Prevent to change event setting => defensive copy by mapper.
			AbstractDto olderOriginalSourceCopy = olderOriginalSource.getClass().getDeclaredConstructor().newInstance();
			modelMapper.map(olderOriginalSource, olderOriginalSourceCopy);
			AbstractDto originalSourceCopy = originalSource.getClass().getDeclaredConstructor().newInstance();
			modelMapper.map(originalSource, originalSourceCopy);
			// Embedded is ignored.
			olderOriginalSourceCopy.setEmbedded(null);
			originalSourceCopy.setEmbedded(null);
			// Audit fields are ignored.
			DtoUtils.clearAuditFields(olderOriginalSourceCopy);
			DtoUtils.clearAuditFields(originalSourceCopy);
			//
			return mapper.writeValueAsString(olderOriginalSourceCopy)
					.equals(mapper.writeValueAsString(originalSourceCopy));
		} catch (JsonProcessingException | ReflectiveOperationException ex) {
			LOG.warn("Comparing json for checking duplicate events failed - both events [{}]-[{}] will be executed!", 
					olderEvent, event, ex);
			//
			return false;
		}
	}
	
	private void removeRunningEvent(IdmEntityEventDto event) {
		runningOwnerEvents.remove(event.getOwnerId());
		UUID superOwnerId = event.getSuperOwnerId();
		if (superOwnerId != null) {
			runningOwnerEvents.remove(superOwnerId);
		}
	}
	
	private void setEnabled(EntityEventProcessor<?> processor, boolean enabled) {
		String enabledPropertyName = processor.getConfigurationPropertyName(ConfigurationService.PROPERTY_ENABLED);
		//
		configurationService.setBooleanValue(enabledPropertyName, enabled);
	}
	
	/**
	 * Convert processor to dto.
	 * 
	 * @param processor
	 * @return
	 */
	private EntityEventProcessorDto toDto(EntityEventProcessor<?> processor) {
		EntityEventProcessorDto dto = new EntityEventProcessorDto();
		dto.setId(processor.getId());
		dto.setName(processor.getName());
		dto.setModule(processor.getModule());
		dto.setContentClass(processor.getEntityClass());
		dto.setEntityType(processor.getEntityClass().getSimpleName());
		dto.setEventTypes(Lists.newArrayList(processor.getEventTypes()));
		dto.setClosable(processor.isClosable());
		dto.setDisabled(processor.isDisabled());
		dto.setDisableable(processor.isDisableable());
		dto.setOrder(processor.getOrder());
		// resolve documentation
		dto.setDescription(processor.getDescription());
		dto.setConfigurationProperties(processor.getConfigurationMap());
		//
		return dto;
	}
	
	/**
	 * Try put event to queue - event is put into queue, only if it's not executed synchronously.
	 * If event is executed synchronously, then {@link EventContext} is returned, {@code null} is returned otherwise. 
	 * 
	 * @param entityEvent
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private EventContext<?> putToQueue(IdmEntityEventDto entityEvent) {
		if (entityEvent.getPriority() == PriorityType.IMMEDIATE) {
			LOG.trace("Event type [{}] for owner with id [{}] will be executed synchronously.", 
					entityEvent.getEventType(), entityEvent.getOwnerId());	
			// synchronous processing
			// we don't persist events and their states
			return process(new CoreEvent<>(EntityEventType.EXECUTE, entityEvent));
		}
		if (!eventConfiguration.isAsynchronous()) {
			LOG.trace("Event type [{}] for owner with id [{}] will be executed synchronously, asynchronous event processing [{}] is disabled.", 
					entityEvent.getEventType(), entityEvent.getOwnerId(), EventConfiguration.PROPERTY_EVENT_ASYNCHRONOUS_ENABLED);	
			// synchronous processing
			return process(new CoreEvent<>(EntityEventType.EXECUTE, entityEvent));
		}
		//
		// get enabled processors, which listen given event (conditional is evaluated)
		final EntityEvent<?> event = toEvent(entityEvent);
		List<EntityEventProcessor> listenProcessors = getEnabledProcessors(event)
				.stream()
				.filter(processor -> processor.conditional(event))
				.collect(Collectors.toList());
		if (listenProcessors.isEmpty()) {
			LOG.debug("Event type [{}] for owner with id [{}] will not be executed, no enabled processor is registered.", 
					entityEvent.getEventType(), entityEvent.getOwnerId());	
			// return empty context - nothing is processed
			return new DefaultEventContext<>();
		}
		//
		// evaluate event priority by registered processors
		PriorityType priority = evaluatePriority(event, listenProcessors);
		if (priority != null && priority.getPriority() < entityEvent.getPriority().getPriority()) {
			entityEvent.setPriority(priority);
		}
		//
		// registered processors voted about event will be processed synchronously
		if (entityEvent.getPriority() == PriorityType.IMMEDIATE) {
			LOG.trace("Event type [{}] for owner with id [{}] will be executed synchronously.", 
					entityEvent.getEventType(), entityEvent.getOwnerId());	
			// synchronous processing
			// we don't persist events and their states
			process(new CoreEvent<>(EntityEventType.EXECUTE, entityEvent));
		}
		//
		// TODO: send notification only when event fails
		// notification - info about registered (asynchronous) processors
//		Map<String, Object> parameters = new LinkedHashMap<>();
//		parameters.put("eventType", entityEvent.getEventType());
//		parameters.put("ownerId", entityEvent.getOwnerId());
//		parameters.put("instanceId", entityEvent.getInstanceId());
//		parameters.put("processors", registeredProcessors
//				.stream()
//				.map(DefaultEntityEventManager.this::toDto)
//				.collect(Collectors.toList()));
//		notificationManager.send(
//				CoreModuleDescriptor.TOPIC_EVENT, 
//				new IdmMessageDto
//					.Builder()
//					.setLevel(NotificationLevel.INFO)
//					.setModel(new DefaultResultModel(CoreResultCode.EVENT_ACCEPTED, parameters))
//					.build());
		//
		// persist event - asynchronous processing
		entityEvent = entityEventService.save(entityEvent);
		addEventCache(entityEvent.getId(), entityEvent.getTransactionId());
		// not processed - persisted into queue
		return null;
	}
	
	/**
	 * Remove internal event properties needed for processing
	 * 
	 * @param event
	 * @return
	 */
	private ConfigurationMap getProperties(IdmEntityEventDto event) {
		return getProperties(event.getProperties().toMap());
	}
	
	private ConfigurationMap getProperties(Map<String, Serializable> eventProperties) {
		ConfigurationMap copiedProperies = new ConfigurationMap(eventProperties);
		//
		// Remove internal event properties needed for processing - will be computed as new.
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_EVENT_ID);
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_EXECUTE_DATE);
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_PRIORITY);
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_PARENT_EVENT_ID);
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_PARENT_EVENT_TYPE);
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_ROOT_EVENT_ID);
		copiedProperies.remove(EntityEvent.EVENT_PROPERTY_PERMISSION);
		//
		return copiedProperies;
	}
	
	private <E extends Serializable> IdmEntityStateDto createState(
			IdmEntityEventDto entityEvent, 
			EventResult<E> eventResult, 
			OperationResultDto operationResult) {
		IdmEntityStateDto state = new IdmEntityStateDto(entityEvent);
		//
		state.setClosed(eventResult.isClosed());
		state.setSuspended(eventResult.isSuspended());
		state.setProcessedOrder(eventResult.getProcessedOrder());
		state.setProcessorId(eventResult.getProcessor().getId());
		state.setProcessorModule(eventResult.getProcessor().getModule());
		state.setProcessorName(eventResult.getProcessor().getName());
		state.setResult(operationResult);
		//
		return state;
	}
	
	/**
	 * Returns true, when given processor pass given filter
	 * 
	 * @param processor
	 * @param filter
	 * @return
	 */
	private boolean passFilter(EntityEventProcessorDto processor, EntityEventProcessorFilter filter) {
		if (filter == null) {
			// empty filter
			return true;
		}
		// id - not supported
		if (filter.getId() != null) {
			throw new UnsupportedOperationException("Filtering event processors by [id] is not supported.");
		}
		// text - lowercase like in name, description, content class - canonical name
		if (StringUtils.isNotEmpty(filter.getText())) {
			if (!processor.getName().toLowerCase().contains(filter.getText().toLowerCase())
					&& (processor.getDescription() == null || !processor.getDescription().toLowerCase().contains(filter.getText().toLowerCase()))
					&& !processor.getContentClass().getCanonicalName().toLowerCase().contains(filter.getText().toLowerCase())) {
				return false;
			}
		}
		// processors name
		if (StringUtils.isNotEmpty(filter.getName()) && !processor.getName().equals(filter.getName())) {
			return false; 
		}
		// content ~ entity type - dto type
		if (filter.getContentClass() != null && !filter.getContentClass().isAssignableFrom(processor.getContentClass())) {
			return false;
		}
		// module id
		if (StringUtils.isNotEmpty(filter.getModule()) && !filter.getModule().equals(processor.getModule())) {
			return false;
		}
		// description - like
		if (StringUtils.isNotEmpty(filter.getDescription()) 
				&& StringUtils.isNotEmpty(processor.getDescription()) 
				&& !processor.getDescription().contains(filter.getDescription())) {
			return false;
		}
		// entity ~ content type - simple name
		if (StringUtils.isNotEmpty(filter.getEntityType()) && !processor.getEntityType().equals(filter.getEntityType())) {
			return false;
		}
		// event types
		if (!filter.getEventTypes().isEmpty() && !processor.getEventTypes().containsAll(filter.getEventTypes())) {
			return false;
		}
		//
		return true;
	}
	
	private IdmEntityEventDto prepareEvent(Class<? extends Identifiable> ownerType, UUID ownerId, EntityEvent<? extends Identifiable> originalEvent) {
		Assert.notNull(ownerType, "Owner type is required.");
		Assert.notNull(ownerId, "Change can be published after entity id is assigned at least.");
		//
		IdmEntityEventDto savedEvent = toDto(originalEvent);
		savedEvent.setId(null);
		savedEvent.setOwnerId(ownerId);
		savedEvent.setOwnerType(getOwnerType(ownerType));
		//
		if (originalEvent != null) {
			savedEvent.setParent(originalEvent.getId());
			savedEvent.setRootId(originalEvent.getRootId() == null ? originalEvent.getId() : originalEvent.getRootId());
			savedEvent.setParentEventType(originalEvent.getType().name());
		} else {
			// notify as default event type
			savedEvent.setEventType(CoreEventType.NOTIFY.name());
		}
		//
		return savedEvent;
	}
	
	/**
	 * Usable for newly created events
	 * 
	 * @param owner
	 * @param event
	 * @return
	 */
	private IdmEntityEventDto toDto(Identifiable owner, EntityEvent<? extends Identifiable> event) {
		IdmEntityEventDto entityEvent = toDto(event);
		if (owner != null) {
			entityEvent.setOwnerId(lookupService.getOwnerId(owner));
			entityEvent.setOwnerType(getOwnerType(owner.getClass()));
		}
		//
		return entityEvent;
	}
	
	private IdmEntityEventDto toDto(EntityEvent<? extends Identifiable> event) {
		IdmEntityEventDto entityEvent = new IdmEntityEventDto();
		//
		entityEvent.setResult(new OperationResultDto.Builder(OperationState.CREATED).build());
		entityEvent.setInstanceId(eventConfiguration.getAsynchronousInstanceId());
		if (event == null) {
			entityEvent.setPriority(PriorityType.NORMAL);
			return entityEvent;
		}
		entityEvent.setId(event.getId());
		entityEvent.setSuperOwnerId(event.getSuperOwnerId());
		entityEvent.setEventType(event.getType().name());
		entityEvent.getProperties().putAll(event.getProperties());
		entityEvent.setParent(event.getParentId());
		entityEvent.setRootId(event.getRootId());
		entityEvent.setParentEventType(event.getParentType());
		entityEvent.setExecuteDate(event.getExecuteDate()); // look out - it's the wish - when asynchronous event should be executed...
		entityEvent.setPriority(event.getPriority() != null ? event.getPriority() : PriorityType.NORMAL);
		entityEvent.setContent(event.getContent());
		entityEvent.setOriginalSource(event.getOriginalSource());
		entityEvent.setClosed(event.isClosed());
		if (entityEvent.isClosed()) {
			entityEvent.setResult(new OperationResultDto
					.Builder(OperationState.EXECUTED)
					.setModel(new DefaultResultModel(CoreResultCode.EVENT_ALREADY_CLOSED))
					.build());
		}
		entityEvent.setSuspended(event.isSuspended());
		//
		return entityEvent;
	}
	
	/**
	 * Complete event - check all event in the same transaction is completely processed.
	 * 
	 * @param <E>
	 * @param event
	 * @return
	 */
	private <E extends Serializable> EventContext<E> completeEvent(EntityEvent<E> event) {
		lock.lock();
		try {
			UUID eventId = event.getId();
			if (eventId == null) {
				// synchronous event without id
				return event.getContext();
			}
			//
			// transaction cache
			UUID transactionId = event.getTransactionId();
			if (removeEventCache(eventId, transactionId)) {				
				LOG.info("Event [{}] with transaction id [{}] is processed completely", event, transactionId);
				if (lrts.containsKey(transactionId) 
						&& lrts.get(transactionId).stream().allMatch(lrt -> lrt.getResult() != null)) {
					List<LongRunningTaskExecutor<?>> longRunningTaskExecutors = lrts.remove(transactionId);
					//
					longRunningTaskExecutors.forEach(lrt -> {
						notifiedLrts.put(lrt.getLongRunningTaskId(), Boolean.TRUE);
						lrt.notifyEnd();
					});
				}
			}
			//
			return event.getContext();
		} finally {
			lock.unlock();
		}
	}
	
	/**
	 * Include event in transaction processing.
	 * 
	 * @param eventId
	 * @param transactionId
	 */
	@SuppressWarnings("unchecked")
	private void addEventCache(UUID eventId, UUID transactionId) {
		Assert.notNull(eventId, "Event has to be asynchronous (~persisted).");
		//
		lock.lock();
		try {			
			if (transactionId == null) {
				return;
			}
			//
			ValueWrapper value = cacheManager.getValue(TRANSACTION_EVENT_CACHE_NAME, transactionId);
			//
			Set<UUID> events = null;
			if (value == null) {
				events = new HashSet<>();
			} else {
				events = (Set<UUID>) value.get();
			}
			events.add(eventId);
			cacheManager.cacheValue(TRANSACTION_EVENT_CACHE_NAME, transactionId, events);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * ~ Complete event in transaction processing.
	 * 
	 * @param eventId
	 * @param transactionId
	 * @return true => all events in transaction are processed
	 */
	@SuppressWarnings("unchecked")
	private boolean removeEventCache(UUID eventId, UUID transactionId) {
		lock.lock();
		try {
			if (transactionId == null) {
				return false;
			}
			//
			ValueWrapper value = cacheManager.getValue(TRANSACTION_EVENT_CACHE_NAME, transactionId);
			if (value == null) {
				// transaction was not registered (~ synchronous processing)
				return false;
			}
			Set<UUID> events = (Set<UUID>) value.get();
			events.remove(eventId);
			if (!events.isEmpty()) {
				cacheManager.cacheValue(TRANSACTION_EVENT_CACHE_NAME, transactionId, events);
				return false;
			}
			//
			cacheManager.evictValue(TRANSACTION_EVENT_CACHE_NAME, transactionId);
			return true; // => asynchronous transaction is processed completely
		} finally {
			lock.unlock();
		}
	}
}
