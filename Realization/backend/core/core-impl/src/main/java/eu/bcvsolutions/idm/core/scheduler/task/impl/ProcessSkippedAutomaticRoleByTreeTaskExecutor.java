package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmEntityStateDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmEntityStateFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.EntityStateManager;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
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
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	@Autowired private EntityEventManager entityEventManager;
	//
	private Set<UUID> processedOwnerIds = new HashSet<>(); // distinct owners will be processed
	private List<UUID> processedAutomaticRoles = new ArrayList<>(); // distinct processed automatic roles
	private Set<UUID> processedRoleRequests = new HashSet<>(); // all processed identity roles - invalid role removal is solved, after all automatic roles are assigned (prevent drop and create target account)
	
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
		filter.setOwnerType(entityEventManager.getOwnerType(IdmRoleTreeNodeDto.class));
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
								"ownerType", entityEventManager.getOwnerType(IdmRoleTreeNodeDto.class))))
						.build(), 
					this.getLongRunningTaskId()
			);
			// delete state only
			entityStateManager.deleteState(state);
		} else {	
			ProcessAutomaticRoleByTreeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByTreeTaskExecutor.class);
			automaticRoleTask.setAutomaticRoles(Lists.newArrayList(automaticRole.getId()));
			automaticRoleTask.setRemoveNotProcessedIdentityRoles(false); // invalid roles will be removed, after LRT end (prevent drop and create target account)
			automaticRoleTask.setRequireNewTransaction(true);
			longRunningTaskManager.executeSync(automaticRoleTask); // exception is thrown and logged otherwise
			processedRoleRequests.addAll(automaticRoleTask.getProcessedRoleRequests());
			processedAutomaticRoles.add(ownerId);
			//
			getItemService().createLogItem(
					automaticRole, 
					new OperationResult.Builder(OperationState.EXECUTED).build(), 
					this.getLongRunningTaskId()
			);
		}
		//
		processedOwnerIds.add(ownerId);
		//
		// Log added manually above - log processed contract / position instead of deleted entity state.
		return Optional.empty();
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		if (BooleanUtils.isTrue(result) && ex == null) {
			// remove previously assigned role, which was not processed by any automatic role
			ProcessAutomaticRoleByTreeTaskExecutor automaticRoleTask = AutowireHelper.createBean(ProcessAutomaticRoleByTreeTaskExecutor.class);
			automaticRoleTask.setAutomaticRoles(processedAutomaticRoles);
			automaticRoleTask.setProcessedRoleRequests(processedRoleRequests);
			automaticRoleTask.setRemoveNotProcessedIdentityRoles(true);
			automaticRoleTask.setRequireNewTransaction(true);
			automaticRoleTask.setLongRunningTaskId(getLongRunningTaskId()); // support to cancel task, when roles are removed
			// remove previously assigned role, which was not processed by any automatic role
			processedAutomaticRoles.forEach(automaticRole -> {
				Set<UUID> processedIdentityRoles = new HashSet<>(processedRoleRequests);
				processedRoleRequests.forEach(requestId -> {
					processedIdentityRoles.addAll(
							conceptRoleRequestService
								.findAllByRoleRequest(requestId)
								.stream()
								.map(concept -> {
									UUID identityRole = concept.getIdentityRole();
									Assert.notNull(identityRole, 
											String.format(
													"Concept is not executed [%s], identity role identifier is empty.", 
													concept.getState()
											)
									);
									return identityRole;
								})
								.collect(Collectors.toSet()
							)
					);
				});
				// new transaction is wrapped inside
				automaticRoleTask.processIdentityRoles(processedIdentityRoles, automaticRole);
			});
		}
		//
		return super.end(result, ex);
	}
}
