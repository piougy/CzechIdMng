package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmScheduledTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.scheduler.api.service.IdmScheduledTaskService;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmScheduledTask;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmScheduledTask_;
import eu.bcvsolutions.idm.core.scheduler.exception.SchedulerException;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmScheduledTaskRepository;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

public class DefaultIdmScheduledTaskService
	extends AbstractReadWriteDtoService<IdmScheduledTaskDto, IdmScheduledTask, IdmScheduledTaskFilter>
	implements IdmScheduledTaskService {
	
	private final IdmProcessedTaskItemService itemService;
	private final IdmLongRunningTaskService lrtService;

	@Autowired
	public DefaultIdmScheduledTaskService(
			IdmScheduledTaskRepository repository,
			IdmProcessedTaskItemService itemService,
			IdmLongRunningTaskService lrtService) {
		super(repository);
		//
		Assert.notNull(itemService);
		Assert.notNull(lrtService);
		//
		this.itemService = itemService;
		this.lrtService = lrtService;
	}

	@Transactional(readOnly = true)
	@Override
	public IdmScheduledTaskDto findByQuartzTaskName(String taskName) {
		IdmScheduledTaskFilter filter = new IdmScheduledTaskFilter();
		filter.setQuartzTaskName(taskName);
		Page<IdmScheduledTaskDto> results = find(filter, new PageRequest(0, 1));
		if (results.getTotalElements() == 0) {
			return null;
		} else if (results.getTotalElements() == 1) {
			return results.iterator().next();
		}
		throw new SchedulerException(CoreResultCode.SEARCH_ERROR,
				ImmutableMap.of(
						"reason", "Multiple tasks found for " + getClass().getSimpleName() + " task."));
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmScheduledTask> root, CriteriaQuery<?> query, CriteriaBuilder builder,
			IdmScheduledTaskFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// task filter
		if (StringUtils.isNotEmpty(filter.getQuartzTaskName())) {
			predicates.add(builder.equal(root.get(IdmScheduledTask_.quartzTaskName), filter.getQuartzTaskName()));
		}
		return predicates;
	}

	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.SCHEDULER, this.getEntityClass());
	}

	@Transactional
	@Override
	public void deleteInternal(IdmScheduledTaskDto dto) {
		Assert.notNull(dto);
		//
		itemService.deleteAllByScheduledTask(this.get(dto.getId()));
		super.deleteInternal(dto);
	}

	@Override
	@Transactional(readOnly = true)
	public IdmScheduledTaskDto findByLongRunningTaskId(UUID lrtId) {
		Assert.notNull(lrtId);
		//
		IdmLongRunningTaskDto lrt = lrtService.get(lrtId);
		//
		return lrt.getScheduledTask() == null ? null : this.get(lrt.getScheduledTask());
	}
	
}
