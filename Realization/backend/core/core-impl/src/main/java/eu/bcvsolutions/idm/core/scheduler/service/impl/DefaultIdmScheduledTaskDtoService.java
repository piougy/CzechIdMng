package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.util.ArrayList;
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
import eu.bcvsolutions.idm.core.api.entity.AbstractEntity_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmScheduledTaskDto;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.IdmScheduledTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmScheduledTask;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmScheduledTask_;
import eu.bcvsolutions.idm.core.scheduler.exception.SchedulerException;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmScheduledTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskDtoService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemDtoService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmScheduledTaskDtoService;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

public class DefaultIdmScheduledTaskDtoService
	extends AbstractReadWriteDtoService<IdmScheduledTaskDto, IdmScheduledTask, IdmScheduledTaskFilter>
	implements IdmScheduledTaskDtoService {
	
	private final IdmProcessedTaskItemDtoService itemService;
	private final IdmLongRunningTaskDtoService lrtService;

	@Autowired
	public DefaultIdmScheduledTaskDtoService(
			IdmScheduledTaskRepository repository,
			IdmProcessedTaskItemDtoService itemService,
			IdmLongRunningTaskDtoService lrtService) {
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
		List<Predicate> predicates = new ArrayList<>();
		// id
		if (filter.getId() != null) {
			predicates.add(builder.equal(root.get(AbstractEntity_.id), filter.getId()));
		}
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

	@Transactional(readOnly = true)
	@Override
	public IdmScheduledTaskDto findByLongRunningTaskId(UUID lrtId) {
		Assert.notNull(lrtId);
		//
		IdmLongRunningTaskDto lrt = lrtService.get(lrtId);
		//
		return lrt.getScheduledTask() == null ? null : this.get(lrt.getScheduledTask());
	}
	
}
