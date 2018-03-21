package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.ResultModel;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.model.entity.IdmRoleTreeNode_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Long running task for remove automatic roles from identities.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service
@Description("Remove automatic role from IdmRoleTreeNode.")
public class RemoveAutomaticRoleTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityRoleDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveAutomaticRoleTaskExecutor.class);
	//
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	//
	private boolean deleteEntity = true; // At the end of the task remove whole entity (this isn't possible set via FE parameters)
	private UUID automaticRoleId = null;
	private AbstractIdmAutomaticRoleDto automaticRole = null;
	
	/**
	 * Automatic role removal can be start, if previously LRT ended.
	 */
	@Override
	public void validate(IdmLongRunningTaskDto task) {
		super.validate(task);
		//
		AbstractIdmAutomaticRoleDto automaticRole = roleTreeNodeService.get(getAutomaticRoleId());
		if (automaticRole == null) {
			// get from automatic role attribute service
			automaticRole = automaticRoleAttributeService.get(getAutomaticRoleId());
		}
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(this.getClass().getCanonicalName());
		filter.setRunning(Boolean.TRUE);
		//
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getTaskProperties().get(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE).equals(automaticRole.getId())) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_RUN_CONCURRENTLY,
						ImmutableMap.of(
								"roleTreeNode", automaticRole.getId().toString(),
								"taskId", longRunningTask.getId().toString()));
			}
		}
		//
		filter.setTaskType(AddNewAutomaticRoleTaskExecutor.class.getCanonicalName());
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getTaskProperties().get(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE).equals(automaticRole.getId())) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_ADD_RUNNING,
						ImmutableMap.of(
								"roleTreeNode", automaticRole.getId().toString(),
								"taskId", longRunningTask.getId().toString()));
			}
		}
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		this.automaticRole = null;
		this.setAutomaticRoleId(getParameterConverter().toUuid(properties, AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE));
	}
	
	@Override
	protected boolean start() {
		IdmRoleDto role = DtoUtils.getEmbedded(getAutomaticRole(), IdmRoleTreeNode_.role, IdmRoleDto.class);
		LOG.debug("Remove role [{}] by automatic role [{}]", role.getCode(), getAutomaticRole().getId());
		//
		return super.start();
	}
	
	@Override
	public Page<IdmIdentityRoleDto> getItemsToProcess(Pageable pageable) {
		List<IdmIdentityRoleDto> identityRoles = identityRoleService.findByAutomaticRole(getAutomaticRoleId(), null).getContent();
		//
		return new PageImpl<>(identityRoles);
	}
	
	@Override
	public Optional<OperationResult> processItem(IdmIdentityRoleDto identityRole) {	
		try {
			automaticRoleAttributeService.removeAutomaticRoles(identityRole);
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch(Exception ex) {
			IdmIdentityContractDto identityContract = identityContractService.get(identityRole.getIdentityContract());
			IdmIdentityDto identity = DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity, IdmIdentityDto.class);
			IdmRoleDto role = DtoUtils.getEmbedded(getAutomaticRole(), IdmRoleTreeNode_.role, IdmRoleDto.class);
			//
			LOG.error("Remove role [{}] by automatic role [{}] failed", role.getCode(), getAutomaticRole().getId(), ex);
			//
			return Optional.of(new OperationResult
					.Builder(OperationState.EXCEPTION)
					.setModel(new DefaultResultModel(
							CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_NOT_COMPLETE,
							ImmutableMap.of(
									"role", role.getCode(),
									"roleTreeNode", getAutomaticRole().getId(),
									"identity", identity.getUsername())))
					.setCause(ex)
					.build());
		}
	}
	
	@Override
	protected Boolean end(Boolean result, Exception ex) {
		Boolean ended = super.end(result, ex);
		//
		if (BooleanUtils.isTrue(ended)) {
			IdmRoleDto role = DtoUtils.getEmbedded(getAutomaticRole(), IdmRoleTreeNode_.role, IdmRoleDto.class);
			LOG.debug("Remove role [{}] by automatic role [{}]", role.getCode(), getAutomaticRole().getId());
			//
			try {
				//
				// Find all concepts and remove relation on role tree
				IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
				conceptRequestFilter.setAutomaticRole(getAutomaticRoleId());
				//
				List<IdmConceptRoleRequestDto> concepts = conceptRequestService.find(conceptRequestFilter, null).getContent();
				for (IdmConceptRoleRequestDto concept : concepts) {
					IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
					String message = null;
					if (concept.getState().isTerminatedState()) {
						message = MessageFormat.format(
								"Role tree node [{0}] (reqested in concept [{1}]) was deleted (not from this role request)!",
								getAutomaticRoleId(), concept.getId());
					} else {
						message = MessageFormat.format(
								"Request change in concept [{0}], was not executed, because requested RoleTreeNode [{1}] was deleted (not from this role request)!",
								concept.getId(), getAutomaticRoleId());
						concept.setState(RoleRequestState.CANCELED);
					}
					roleRequestService.addToLog(request, message);
					conceptRequestService.addToLog(concept, message);
					concept.setAutomaticRole(null);
					
					roleRequestService.save(request);
					conceptRequestService.save(concept);
				}
				//
				// by default is this allowed
				if (this.isDeleteEntity()) {
					// delete entity
					if (getAutomaticRole() instanceof IdmRoleTreeNodeDto) {
						roleTreeNodeService.deleteInternalById(getAutomaticRole().getId());
					} else {
						// remove all rules
						automaticRoleAttributeRuleService.deleteAllByAttribute(getAutomaticRole().getId());
						automaticRoleAttributeService.deleteInternalById(getAutomaticRole().getId());
					}
				}
				//
				LOG.debug("End: Remove role [{}] by automatic role [{}].", role.getCode(), getAutomaticRole().getId());
				//
			} catch (Exception O_o) {
				LOG.debug("Remove role [{}] by automatic role [{}] failed", role.getCode(), getAutomaticRole().getId(), O_o);
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
	
	private void saveResult(ResultModel resultModel, OperationState state, Exception ex) {
		IdmLongRunningTaskDto task = longRunningTaskService.get(getLongRunningTaskId());
		task.setResult(new OperationResult.Builder(state).setModel(resultModel).setCause(ex).build());
		//
		// TODO: skips event about task ends with exception
		getLongRunningTaskService().save(task);
	}

	public boolean isDeleteEntity() {
		return deleteEntity;
	}

	public void setDeleteEntity(boolean deleteEntity) {
		this.deleteEntity = deleteEntity;
	}
	
	public void setAutomaticRoleId(UUID automaticRoleId) {
		this.automaticRoleId = automaticRoleId;
	}
	
	public UUID getAutomaticRoleId() {
		return this.automaticRoleId;
	}
	
	@Deprecated
	public void setRoleTreeNodeId(UUID roleTreeNodeId) {
		this.setAutomaticRoleId(roleTreeNodeId);
	}
	
	@Deprecated
	protected UUID getRoleTreeNodeId() {
		return this.getAutomaticRoleId();
	}
	
	private AbstractIdmAutomaticRoleDto getAutomaticRole() {
		if (automaticRole == null) {
			automaticRole = roleTreeNodeService.get(getAutomaticRoleId());
			if (automaticRole == null) {
				automaticRole = automaticRoleAttributeService.get(getAutomaticRoleId());
			}
			if (automaticRole == null) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
			}
		}
		return automaticRole;
	}
	
	@Override
	public Map<String, Object> getProperties() {
		Map<String, Object> properties =  super.getProperties();
		properties.put(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE, getAutomaticRoleId());
		return properties;
	}
	
	@Override
	public List<String> getPropertyNames() {
		List<String> propertyNames = super.getPropertyNames();
		propertyNames.add(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE);
		return propertyNames;
	}
}
