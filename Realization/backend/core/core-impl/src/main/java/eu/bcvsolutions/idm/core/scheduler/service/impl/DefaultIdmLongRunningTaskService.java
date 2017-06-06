package eu.bcvsolutions.idm.core.scheduler.service.impl;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.entity.OperationResult_;
import eu.bcvsolutions.idm.core.api.service.AbstractReadWriteDtoService;
import eu.bcvsolutions.idm.core.model.domain.CoreGroupPermission;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.dto.filter.LongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask;
import eu.bcvsolutions.idm.core.scheduler.entity.IdmLongRunningTask_;
import eu.bcvsolutions.idm.core.scheduler.repository.IdmLongRunningTaskRepository;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmLongRunningTaskService;
import eu.bcvsolutions.idm.core.scheduler.service.api.IdmProcessedTaskItemService;
import eu.bcvsolutions.idm.core.security.api.domain.BasePermission;
import eu.bcvsolutions.idm.core.security.api.dto.AuthorizableType;

/**
 * Persists long running tasks
 * 
 * Look out: 
 * @Transactional(propagation = Propagation.REQUIRES_NEW) is needed. 
 * LRT has to be persist and read separately from outer transaction => we want to see LRT progress all time. And result with exception.
 * 
 * 
 * @author Radek Tomiška
 *
 */
public class DefaultIdmLongRunningTaskService
	extends AbstractReadWriteDtoService<IdmLongRunningTaskDto, IdmLongRunningTask, LongRunningTaskFilter>
	implements IdmLongRunningTaskService {
	
	private final IdmLongRunningTaskRepository repository;
	private final IdmProcessedTaskItemService itemService;
	
	@Autowired
	public DefaultIdmLongRunningTaskService(
			IdmLongRunningTaskRepository repository,
			IdmProcessedTaskItemService itemService) {
		super(repository);
		//
		Assert.notNull(itemService);
		//
		this.repository = repository;
		this.itemService = itemService;
	}
	
	@Override
	public AuthorizableType getAuthorizableType() {
		return new AuthorizableType(CoreGroupPermission.SCHEDULER, getEntityClass());
	}
	
	@Override
	protected List<Predicate> toPredicates(Root<IdmLongRunningTask> root, CriteriaQuery<?> query,
			CriteriaBuilder builder, LongRunningTaskFilter filter) {
		List<Predicate> predicates = super.toPredicates(root, query, builder, filter);
		//
		// quick - "fulltext"
		if (StringUtils.isNotEmpty(filter.getText())) {
			predicates.add(builder.or(
					builder.like(builder.lower(root.get(IdmLongRunningTask_.taskType)), "%" + filter.getText().toLowerCase() + "%"),
					builder.like(builder.lower(root.get(IdmLongRunningTask_.taskDescription)), "%" + filter.getText().toLowerCase() + "%")					
					));
		}
		if (StringUtils.isNotEmpty(filter.getTaskType())) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.taskType), filter.getTaskType()));
		}
		if (filter.getFrom() != null) {
			predicates.add(builder.greaterThanOrEqualTo(root.get(IdmLongRunningTask_.created), filter.getFrom()));
		}
		if (filter.getTill() != null) {
			predicates.add(builder.lessThanOrEqualTo(root.get(IdmLongRunningTask_.created), filter.getTill().plusDays(1)));
		}
		if (filter.getOperationState() != null) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.result).get(OperationResult_.state), filter.getOperationState()));
		}
		if (filter.getRunning() != null) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.running), filter.getRunning()));
		}
		if (filter.getStateful() != null) {
			predicates.add(builder.equal(root.get(IdmLongRunningTask_.stateful), filter.getStateful()));
		}
		//
		return predicates;
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<IdmLongRunningTaskDto> findAllByInstance(String instanceId, OperationState state) {
		return toDtos(repository.findAllByInstanceIdAndResult_State(instanceId, state), false);
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void updateState(UUID id, Long count, Long counter) {
		repository.updateState(id, count, counter, new DateTime());
	}
	
	@Override
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public IdmLongRunningTaskDto save(IdmLongRunningTaskDto dto, BasePermission... permission) {
		return super.save(dto, permission);
	}
	
	@Override
	@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
	public IdmLongRunningTaskDto get(Serializable id, BasePermission... permission) {
		return super.get(id, permission);
	}
	
	@Override
	@Transactional
	public void deleteInternal(IdmLongRunningTaskDto dto) {
		Assert.notNull(dto);
		//
		itemService.deleteAllByLongRunningTask(get(dto.getId()));
		super.deleteInternal(dto);
	}
}
