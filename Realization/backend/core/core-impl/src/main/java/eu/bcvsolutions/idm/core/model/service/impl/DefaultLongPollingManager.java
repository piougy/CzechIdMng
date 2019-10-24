package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.context.request.async.DeferredResult;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.dto.OperationResultDto;
import eu.bcvsolutions.idm.core.api.dto.filter.ModifiedFromFilter;
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.ReadDtoService;
import eu.bcvsolutions.idm.core.model.service.api.CheckLongPollingResult;
import eu.bcvsolutions.idm.core.model.service.api.LongPollingManager;
import eu.bcvsolutions.idm.core.rest.DeferredResultWrapper;
import eu.bcvsolutions.idm.core.rest.LongPollingSubscriber;

/**
 *
 * Default implementation of long polling manager
 *
 * @author Vít Švanda
 *
 */
@Service("longPollingManager")
public class DefaultLongPollingManager implements LongPollingManager{

	private static final Logger LOG = LoggerFactory.getLogger(DefaultLongPollingManager.class);

	/**
	 * Queue of deferred results
	 */
	private final Queue<DeferredResultWrapper> suspendedRequests = new ConcurrentLinkedQueue<DeferredResultWrapper>();
	/**
	 * Map of subscribers - subscriber contains metadata as last time stamp after deferred result is ended.
	 */
	private final Map<UUID, LongPollingSubscriber> registredSubscribers = new ConcurrentHashMap<UUID, LongPollingSubscriber>();

	@Autowired
	@Lazy
	private ConfigurationService configurationService;


	@Override
	public void checkDeferredRequests(Class<? extends AbstractDto> type) {
		Assert.notNull(type, "Class type cannot be null!");

		this.suspendedRequests.stream()
			.filter(request -> request.getResult().isSetOrExpired())
			.forEach(request -> {
				this.suspendedRequests.remove(request);
			 }
		);

		this.suspendedRequests.stream() //
				.filter(request -> type.equals(request.getType())) //
				.forEach(request -> { //
					LongPollingSubscriber subscriber = null;
					if (this.registredSubscribers.containsKey(request.getEntityId())) {
						subscriber = this.registredSubscribers.get(request.getEntityId());
					} else {
						subscriber = new LongPollingSubscriber(request.getEntityId(), type);
					}
					CheckLongPollingResult checkResultCallback = request.getCheckResultCallback();
					if (checkResultCallback != null) {
						subscriber.setLastUsingSubscriber(ZonedDateTime.now());
						checkResultCallback.checkDeferredResult(request.getResult(), subscriber);
					}
				});
	}


	@Override
	public synchronized void addSuspendedResult(DeferredResultWrapper result) {
		Assert.notNull(result, "Result cannot be null!");
		Assert.notNull(result.getType(), "Type of result must be defined!");
		Assert.notNull(result.getResult(), "Deffered result must be defined!");
		UUID entityId = result.getEntityId();
		Assert.notNull(entityId, "Entity ID cannot be null!");

		LOG.debug("Add deferred-result [{}]", result);

		this.suspendedRequests.add(result);
		if (!this.registredSubscribers.containsKey(entityId)) {
			this.registredSubscribers.put(entityId, new LongPollingSubscriber(result.getEntityId(), result.getType()));
		}

		result.getResult().onCompletion(new Runnable() {
			@Override
			public void run() {
				suspendedRequests.remove(result);
			}
		});
	}


	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void baseCheckDeferredResult(DeferredResult<OperationResultDto> deferredResult,
			LongPollingSubscriber subscriber, ModifiedFromFilter filter,
			ReadDtoService service, boolean checkCount) {
		Assert.notNull(deferredResult, "Deferred result is required to check.");
		Assert.notNull(subscriber.getEntityId(), "Subscriber is required to check deferred result.");

		LOG.debug("Start baseCheckDeferredResult for deferred-result [{}] and subscriber [{}]", deferredResult, subscriber);

		if (checkCount) {
			long countOfentities = service.count(filter);
			Long lastNumberOfEntities = subscriber.getLastNumberOfEntities();
			subscriber.setLastNumberOfEntities(countOfentities);
			if (lastNumberOfEntities != null && countOfentities != lastNumberOfEntities) {
				// Notify FE -> Some of an entities were changed (refresh must be executed)
				deferredResult.setResult(new OperationResultDto(OperationState.RUNNING));
				return;
			}
		}

		ZonedDateTime timeStamp = subscriber.getLastTimeStamp();
		if (timeStamp == null) {
			List<AbstractDto> entities = service
					.find(filter, PageRequest.of(0, 1, new Sort(Direction.DESC, AbstractEntity_.created.getName(),
							AbstractEntity_.modified.getName())))
					.getContent();

			if (entities.isEmpty()) {
				subscriber.setLastTimeStamp(ZonedDateTime.now());
				return;
			}

			ZonedDateTime lastModified = this.getLastTimeStamp(entities.get(0));
			subscriber.setLastTimeStamp(lastModified);
			return;

		}
		// Try to find, if some from not finished entities were changed
		filter.setModifiedFrom(timeStamp);
		List<AbstractDto> changedRequestsFromLastChecks = service
				.find(filter, PageRequest.of(0, 1000,
				Sort.by(Direction.DESC, AbstractEntity_.created.getName(), AbstractEntity_.modified.getName())))
				.getContent();

		if (!changedRequestsFromLastChecks.isEmpty()) {
			AbstractDto changedRequestsFromLastCheck = changedRequestsFromLastChecks.get(0);
			ZonedDateTime lastModified = this.getLastTimeStamp(changedRequestsFromLastCheck);
			subscriber.setLastTimeStamp(lastModified);
			// Notify FE -> Some of the role-request was changed (refresh must be executed)
			deferredResult.setResult(new OperationResultDto(OperationState.RUNNING));
			return;
		}
		// Nothing was changed
	}

	@Scheduled(fixedDelay = 7200000) // Every two hours
	public void clearUnUseSubscribers() {
		LOG.info("Start scheduled clearUnUseSubscribers ...");
		this.clearUnUseSubscribers(null);
	}

	@Override
	public void clearUnUseSubscribers(ZonedDateTime clearBeforIt) {
		if (clearBeforIt == null) {
			clearBeforIt = ZonedDateTime.now().minusHours(1);
		}
		ZonedDateTime timeStamp = clearBeforIt;
		LOG.debug("Start clearUnUseSubscribers [{}] ...", timeStamp);

		this.registredSubscribers.values().stream() //
				.filter(subscriber -> subscriber.getLastUsingSubscriber() != null
						&& subscriber.getLastUsingSubscriber().isBefore(timeStamp))
				.forEach(subscriber -> { //
					if (subscriber.getEntityId() != null) {
						LOG.debug("Remove expired subscriber [{}].", subscriber);
						this.registredSubscribers.remove(subscriber.getEntityId());
					}
				});
	}

	@Override
	public boolean isLongPollingEnabled() {
		return configurationService.getBooleanValue(LONG_POLLING_ENABLED_KEY, true);
	}

	@Override
	public ZonedDateTime getLastTimeStamp(AbstractDto dto) {
		ZonedDateTime lastModified = dto.getModified();
		if (lastModified == null) {
			lastModified = dto.getCreated();
		}

		if (lastModified == null) {
			return null;
		}

		if (dto.getCreated() != null && lastModified.isBefore(dto.getCreated())) {
			lastModified = dto.getCreated();
		}

		return lastModified.plus(2, ChronoUnit.MILLIS);
	}

	/**
	 * Get deferred results. For testing purpose.
	 *
	 */
	public Queue<DeferredResultWrapper> getSuspendedRequests() {
		return suspendedRequests;
	}


	/**
	 * Get registered subscribers. For testing purpose.
	 *
	 * @return
	 */
	public Map<UUID, LongPollingSubscriber> getRegistredSubscribers() {
		return registredSubscribers;
	}

}
