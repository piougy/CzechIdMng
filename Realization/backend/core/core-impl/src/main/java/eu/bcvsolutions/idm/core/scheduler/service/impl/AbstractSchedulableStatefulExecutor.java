package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.Assert;

import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmProcessedTaskItemDto;
import eu.bcvsolutions.idm.core.scheduler.api.service.SchedulableStatefulExecutor;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.IdmProcessedTaskItemFilter;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemDtoService;

/**
 * Abstract base class for HR process tasks, which handles common
 * HR process flow for context-less and stateful processes (the ones
 * with inner memory.
 * 
 * All stateful HR processes work with entity IDs (of type UUID) as
 * references to already processed items. 
 * 
 * @author Jan Helbich
 *
 * @param <T> process DTO type, 
 */
public abstract class AbstractSchedulableStatefulExecutor<T extends AbstractDto>
	extends AbstractSchedulableTaskExecutor<Boolean>
	implements SchedulableStatefulExecutor<T, Boolean> {
	
	private static final Logger LOG = LoggerFactory.getLogger(AbstractSchedulableStatefulExecutor.class);
	private static final int PAGE_SIZE = 100;
	
	@Autowired
	protected IdmProcessedTaskItemDtoService itemService;

	@Override
	public Boolean process() {
		try {
			this.counter = 0L;
			executeProcess();
		} catch (Exception e) {
			LOG.error("An error occurred in task.", e);
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public IdmProcessedTaskItemDto logItemProcessed(T dto, OperationResult opResult) {
		Assert.notNull(dto);
		Assert.notNull(opResult);
		//
		return itemService.createLogItem(dto, opResult, longRunningTaskService.get(this.getLongRunningTaskId()));
	}

	@Override
	public IdmProcessedTaskItemDto addToProcessedQueue(T dto, OperationResult opResult) {
		Assert.notNull(dto);
		Assert.notNull(opResult);
		//
		if (this.getScheduledTaskId() == null) {
			// manually executed task -> ignore stateful queue
			LOG.warn("Running stateful tasks outside scheduler is not recommended.");
			return null;
		}
		return itemService.createQueueItem(dto, opResult, scheduledTaskService.get(this.getScheduledTaskId()));
	}
	
	@Override
	public Collection<UUID> getProcessedItemRefsFromQueue() {
		if (this.getScheduledTaskId() == null) {
			LOG.warn("Running stateful tasks outside scheduler is not recommended.");
			return new ArrayList<>();
		}
		return itemService.findAllRefEntityIdsInQueueByScheduledTask(scheduledTaskService.get(this.getScheduledTaskId()));
	}
	
	@Override
	public boolean isInProcessedQueue(T dto) {
		Assert.notNull(dto);
		//
		Page<IdmProcessedTaskItemDto> p = getItemFromQueue(dto.getId());
		return p.getTotalElements() > 0;
	}

	@Override
	public void removeFromProcessedQueue(UUID entityRef) {
		Assert.notNull(entityRef);
		//
		Page<IdmProcessedTaskItemDto> p = getItemFromQueue(entityRef);
		//
		Assert.isTrue(p.getTotalElements() == 1);
		itemService.deleteInternal(p.iterator().next());
	}

	@Override
	public void removeFromProcessedQueue(T dto) {
		Assert.notNull(dto);
		//
		removeFromProcessedQueue(dto.getId());
	}

	private void executeProcess() {
		Set<UUID> retrievedRefs = new HashSet<>();
		//
		int page = 0;
		boolean hasNextPage = false;
		//
		do {
			Page<T> candidates = this.getItemsToProcess(new PageRequest(page, PAGE_SIZE));
			hasNextPage = candidates.hasContent();
			if (count == null) {
				count = candidates.getTotalElements();
			}
			//
			for (Iterator<T> i = candidates.iterator(); i.hasNext() && hasNextPage;) {
				T candidate = i.next();
				Assert.notNull(candidate);
				Assert.notNull(candidate.getId());
				//
				retrievedRefs.add(candidate.getId());
				processCandidate(candidate);
				++counter;
				hasNextPage &= updateState();
			}
			++page;
			//
		} while (hasNextPage);
		//
		List<UUID> queueEntityRefs = Lists.newArrayList(this.getProcessedItemRefsFromQueue());
		queueEntityRefs.removeAll(retrievedRefs);
		queueEntityRefs.forEach(entityRef -> this.removeFromProcessedQueue(entityRef));
	}

	private void processCandidate(T candidate) {
		if (isInProcessedQueue(candidate)) {
			return;
		}
		Optional<OperationResult> result = this.processItem(candidate);
		if (result.isPresent()) {
			OperationResult opResult = result.get();
			this.logItemProcessed(candidate, opResult);
			if (OperationState.isSuccessful(opResult.getState())) {
				this.addToProcessedQueue(candidate, opResult);
			}
			LOG.debug("HR process [{}] intermediate result: [{}], count: [{}/{}]",
					getClass().getSimpleName(), opResult, count, counter);
		} else {
			LOG.debug("HR process [{}] processed item [{}] without result.",
					getClass().getSimpleName(), candidate);
		}
			
	}
	
	private Page<IdmProcessedTaskItemDto> getItemFromQueue(UUID entityRef) {
		IdmProcessedTaskItemFilter filter = new IdmProcessedTaskItemFilter();
		filter.setReferencedEntityId(entityRef);
		filter.setScheduledTaskId(this.getScheduledTaskId());
		Page<IdmProcessedTaskItemDto> p = itemService.find(filter, new PageRequest(0, 1));
		if (p.getTotalElements() > 1) {
			LOG.warn("Multiple same item references found in [{}] process queue.", this.getClass());
		}
		return p;
	}
	
}
