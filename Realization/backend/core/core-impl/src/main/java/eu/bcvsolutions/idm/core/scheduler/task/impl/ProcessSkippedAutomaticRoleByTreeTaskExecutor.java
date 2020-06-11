package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Recalculate skipped automatic roles by tree structure, when tree node was moved in structure.
 *
 * @author Radek Tomiška
 * @since 10.4.0
 */
@DisallowConcurrentExecution
@Component(ProcessSkippedAutomaticRoleByTreeTaskExecutor.TASK_NAME)
public class ProcessSkippedAutomaticRoleByTreeTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmEntityStateDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ProcessSkippedAutomaticRoleByTreeTaskExecutor.class);
	public static final String TASK_NAME = "core-process-skipped-automatic-role-by-tree-long-running-task";
	//
	@Autowired private LookupService lookupService;
	@Autowired private EntityStateManager entityStateManager;
	//
	private Set<UUID> processedOwnerIds = new HashSet<>(); // distinct owners will be processed
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public boolean requireNewTransaction() {
		return true;
	}
	
	@Override
	public boolean continueOnException() {
		return true;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}
	
	@Override
	public boolean supportsQueue() {
		return false;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		processedOwnerIds = new HashSet<>();
	}

	@Override
	public Page<IdmEntityStateDto> getItemsToProcess(Pageable pageable) {
		// find all states for flag
		IdmEntityStateFilter filter = new IdmEntityStateFilter();
		filter.setStates(Lists.newArrayList(OperationState.BLOCKED));
		filter.setResultCode(CoreResultCode.AUTOMATIC_ROLE_SKIPPED.getCode());
		filter.setOwnerType(entityStateManager.getOwnerType(IdmRoleTreeNodeDto.class));
		//
		return new PageImpl<>(entityStateManager.findStates(filter, null).getContent());
	}

	@Override
	public Optional<OperationResult> processItem(IdmEntityStateDto state) {
		UUID ownerId = state.getOwnerId();
		if (processedOwnerIds.contains(ownerId)) {
			LOG.debug("Automatic roles for owner [{}] was already processed, delete state only.", ownerId);
			// 
			entityStateManager.deleteState(state);
			// Item will be deleted only - we need some result - counter / count will match.
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		//
		// process automatic role on state owner 
		LOG.debug("Process automatic roles for contract [{}].", ownerId);
		//
		IdmRoleTreeNodeDto automaticRole = lookupService.lookupDto(IdmRoleTreeNodeDto.class, ownerId);
		if (automaticRole == null) {
			getItemService().createLogItem(
					state, 
					new OperationResult
						.Builder(OperationState.NOT_EXECUTED)
						.setModel(new DefaultResultModel(CoreResultCode.CONTENT_DELETED, ImmutableMap.of(
								"ownerId", ownerId,
								"ownerType", entityStateManager.getOwnerType(IdmRoleTreeNodeDto.class))))
						.build(), 
					this.getLongRunningTaskId()
			);
		} else {	
			ProcessAutomaticRoleByTreeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByTreeTaskExecutor.class);
			automaticRoleTask.setAutomaticRoles(Lists.newArrayList(automaticRole.getId()));
			longRunningTaskManager.executeSync(automaticRoleTask); // exception is thrown and logged otherwise
			//
			getItemService().createLogItem(
					automaticRole, 
					new OperationResult.Builder(OperationState.EXECUTED).build(), 
					this.getLongRunningTaskId()
			);
		}
		//
		processedOwnerIds.add(ownerId);
		entityStateManager.deleteState(state);
		//
		// Log added manually above - log processed contract / position instead of deleted entity state.
		return null;
	}
}
