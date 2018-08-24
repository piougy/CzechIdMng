package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleCompositionDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmRoleCompositionFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleCompositionService;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Long running task for remove assigned roles by given composition from identities.
 *
 * @author Radek Tomiška
 * @since 9.0.0
 */
@Service
@Description("Long running task for remove assigned roles by given composition from identities.")
public class RemoveRoleCompositionTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityRoleDto> {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveRoleCompositionTaskExecutor.class);
	public static final String PARAMETER_ROLE_COMPOSITION_ID = "role-composition-id";
	//
	@Autowired private IdmRoleCompositionService roleCompositionService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	//
	private UUID roleCompositionId = null;

	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.setRoleCompositionId(getParameterConverter().toUuid(properties, PARAMETER_ROLE_COMPOSITION_ID));
	}
	
	/**
	 * Automatic role removal can be start, if previously LRT ended.
	 */
	@Override
	public void validate(IdmLongRunningTaskDto task) {
		super.validate(task);
		//
		IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
		Assert.notNull(roleComposition);
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(this.getClass().getCanonicalName());
		filter.setRunning(Boolean.TRUE);
		//
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getTaskProperties().get(PARAMETER_ROLE_COMPOSITION_ID).equals(roleCompositionId)) {
				throw new ResultCodeException(CoreResultCode.ROLE_COMPOSITION_REMOVE_TASK_RUN_CONCURRENTLY,
						ImmutableMap.of(
								"roleCompositionId", roleCompositionId.toString(),
								"taskId", longRunningTask.getId().toString()));
			}
		}
		//
		filter.setTaskType(AddNewRoleCompositionTaskExecutor.class.getCanonicalName());
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getTaskProperties().get(PARAMETER_ROLE_COMPOSITION_ID).equals(roleCompositionId)) {
				throw new ResultCodeException(CoreResultCode.ROLE_COMPOSITION_REMOVE_TASK_ADD_RUNNING,
						ImmutableMap.of(
								"roleCompositionId", roleCompositionId.toString(),
								"taskId", longRunningTask.getId().toString()));
			}
		}
	}	
	
	/**
	 * Returns superior roles, which should be processed
	 */
	@Override
	public Page<IdmIdentityRoleDto> getItemsToProcess(Pageable pageable) {
		IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
		Assert.notNull(roleComposition);
		//
		//
		IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
		filter.setRoleCompositionId(roleComposition.getId());
		//
		return identityRoleService.find(filter, null);
	}
	
	@Override
	public Optional<OperationResult> processItem(IdmIdentityRoleDto identityRole) {
		try {
			removeAssignedRoles(new ArrayList<>(), identityRole);
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			return Optional.of(new OperationResult
					.Builder(OperationState.EXCEPTION)
					.setModel(new DefaultResultModel(
							CoreResultCode.ROLE_COMPOSITION_ASSIGNED_ROLE_REMOVAL_FAILED,
							ImmutableMap.of(
									"identityRole", identityRole.getId().toString())))
					.setCause(ex)
					.build());
		}
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		Boolean ended = super.end(result, ex);
		//
		if (BooleanUtils.isTrue(ended)) {
			IdmRoleCompositionDto roleComposition = roleCompositionService.get(roleCompositionId);
			Assert.notNull(roleComposition);
			//
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setRoleCompositionId(roleComposition.getId());
			//
			long assignedRoles = identityRoleService.find(filter, new PageRequest(0, 1)).getTotalElements();
			if (assignedRoles != 0) {
				LOG.debug("Remove role composition [{}] is not complete, some identity roles [{}] remains assigned to identities.", 
						roleCompositionId, assignedRoles);
				return ended;
			}
			//
			LOG.debug("Remove role composition [{}]", roleCompositionId);
			try {
				roleCompositionService.deleteInternal(roleComposition);
				//
				LOG.debug("End: Remove role composition [{}].", roleCompositionId);
				//
			} catch (Exception O_o) {
				LOG.debug("Remove role composition [{}] failed", roleCompositionId, O_o);
				//
				IdmLongRunningTaskDto task = longRunningTaskService.get(getLongRunningTaskId());
				ResultModel resultModel = new DefaultResultModel(CoreResultCode.LONG_RUNNING_TASK_FAILED, 
						ImmutableMap.of(
								"taskId", getLongRunningTaskId(), 
								"taskType", task.getTaskType(),
								"instanceId", task.getInstanceId()));
				saveResult(resultModel, OperationState.EXCEPTION, O_o);
			}
		}
		//
		return ended;
	}
	
	public void setRoleCompositionId(UUID roleCompositionId) {
		this.roleCompositionId = roleCompositionId;
	}
	
	public UUID getRoleCompositionId() {
		return this.roleCompositionId;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(PARAMETER_ROLE_COMPOSITION_ID, roleCompositionId);
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(PARAMETER_ROLE_COMPOSITION_ID);
		return propertyNames;
	}
	
	private void removeAssignedRoles(List<UUID> processedIdentityRoles, IdmIdentityRoleDto identityRole) {
		IdmRoleCompositionFilter compositionFilter = new IdmRoleCompositionFilter();
		compositionFilter.setSuperiorId(identityRole.getRole());
		processedIdentityRoles.add(identityRole.getId()); // prevent cycles ...
		roleCompositionService.find(compositionFilter, null)
			.forEach(composition -> {
				IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
				filter.setRoleId(composition.getSub());
				filter.setDirectRoleId(identityRole.getDirectRole());
				identityRoleService
					.find(filter, null)
					.forEach(subIdentityRole -> {
						// remove all sub
						if (!processedIdentityRoles.contains(subIdentityRole.getId())) {
							removeAssignedRoles(processedIdentityRoles, subIdentityRole);
						}
					});
			});
		//
		// remove superior at last
		identityRoleService.delete(identityRole);
	}
	
	private void saveResult(ResultModel resultModel, OperationState state, Exception ex) {
		IdmLongRunningTaskDto task = longRunningTaskService.get(getLongRunningTaskId());
		task.setResult(new OperationResult.Builder(state).setModel(resultModel).setCause(ex).build());
		//
		// TODO: skips event about task ends with exception
		getLongRunningTaskService().save(task);
	}
}
