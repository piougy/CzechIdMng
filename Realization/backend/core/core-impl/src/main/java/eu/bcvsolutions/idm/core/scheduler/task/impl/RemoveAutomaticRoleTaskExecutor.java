package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleTreeNodeDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmConceptRoleRequestFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeRuleService;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleTreeNodeService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityContract_;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;

/**
 * Long running task for remove automatic roles from identities.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 *
 */
@Service
@Description("Remove automatic role from IdmRoleTreeNode.")
public class RemoveAutomaticRoleTaskExecutor extends AbstractAutomaticRoleTaskExecutor {
	
	/*
	 * At the end of the task remove whole entity (this isn't possible set via FE parameters)
	 */
	private boolean deleteEntity = true;
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RemoveAutomaticRoleTaskExecutor.class);
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleTreeNodeService roleTreeNodeService;
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmConceptRoleRequestService conceptRequestService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmRoleService roleService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmAutomaticRoleAttributeRuleService automaticRoleAttributeRuleService;
	
	/**
	 * Automatic role removal can be start, if previously LRT ended.
	 */
//	@Override
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
			if (longRunningTask.getTaskProperties().get(PARAMETER_ROLE_TREE_NODE).equals(automaticRole.getId())) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_RUN_CONCURRENTLY,
						ImmutableMap.of(
								"roleTreeNode", automaticRole.getId().toString(),
								"taskId", longRunningTask.getId().toString()));
			}
		}
		//
		filter.setTaskType(AddNewAutomaticRoleTaskExecutor.class.getCanonicalName());
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getTaskProperties().get(PARAMETER_ROLE_TREE_NODE).equals(automaticRole.getId())) {
				throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_ADD_RUNNING,
						ImmutableMap.of(
								"roleTreeNode", automaticRole.getId().toString(),
								"taskId", longRunningTask.getId().toString()));
			}
		}
	}
	
	@Override
	public Boolean process() {
		AbstractIdmAutomaticRoleDto automaticRole = roleTreeNodeService.get(getAutomaticRoleId());
		if (automaticRole == null) {
			automaticRole = automaticRoleAttributeService.get(getAutomaticRoleId());
		}
		if (automaticRole == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
		}
		//
		// TODO: pageable?
		List<IdmIdentityRoleDto> list = identityRoleService.findByAutomaticRole(automaticRole.getId(), null).getContent();
		//
		counter = 0L;
		count = Long.valueOf(list.size());
		//
		IdmRoleDto role = roleService.get(automaticRole.getRole());
		LOG.debug("[RemoveAutomaticRoleTaskExecutor] Remove role [{}] by automatic role [{}]. Count: [{}]", role.getCode(), automaticRole.getId(), count);		
		//
		List<String> failedIdentities = new ArrayList<>();
		boolean canContinue = true;
		for (IdmIdentityRoleDto identityRole : list) {
			IdmRoleRequestDto roleRequest = automaticRoleAttributeService.prepareRemoveAutomaticRoles(identityRole, Sets.newHashSet(automaticRole));
			roleRequest = roleRequestService.startRequest(roleRequest.getId(), false);
			if (roleRequest.getState() != RoleRequestState.EXCEPTION) {
				counter++;
			} else {
				IdmIdentityContractDto identityContract = identityContractService.get(identityRole.getIdentityContract());
				IdmIdentityDto identity = DtoUtils.getEmbedded(identityContract, IdmIdentityContract_.identity, IdmIdentityDto.class);
				failedIdentities.add(identity.getUsername());
			}
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		if (!failedIdentities.isEmpty()) {
			LOG.debug("End: Remove role [{}] by automatic role [{}]. Count: [{}/{}]", role.getCode(), automaticRole.getId(), counter, count);
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_REMOVE_TASK_NOT_COMPLETE, 
					ImmutableMap.of(
							"role", role.getCode(),
							"roleTreeNode", automaticRole.getId(),
							"identities", StringUtils.join(failedIdentities, ",")));
		}
		if (!canContinue) {
			// LRT was canceled
			return Boolean.FALSE;
		}
		// Find all concepts and remove relation on role tree
		IdmConceptRoleRequestFilter conceptRequestFilter = new IdmConceptRoleRequestFilter();
		conceptRequestFilter.setAutomaticRole(automaticRole.getId());
		//
		List<IdmConceptRoleRequestDto> concepts = conceptRequestService.find(conceptRequestFilter, null).getContent();
		for (IdmConceptRoleRequestDto concept : concepts) {
			IdmRoleRequestDto request = roleRequestService.get(concept.getRoleRequest());
			String message = null;
			if (concept.getState().isTerminatedState()) {
				message = MessageFormat.format(
						"Role tree node [{0}] (reqested in concept [{1}]) was deleted (not from this role request)!",
						automaticRole.getId(), concept.getId());
			} else {
				message = MessageFormat.format(
						"Request change in concept [{0}], was not executed, because requested RoleTreeNode [{1}] was deleted (not from this role request)!",
						concept.getId(), automaticRole.getId());
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
			if (automaticRole instanceof IdmRoleTreeNodeDto) {
				roleTreeNodeService.deleteInternalById(automaticRole.getId());
			} else {
				// remove all rules
				automaticRoleAttributeRuleService.deleteAllByAttribute(automaticRole.getId());
				automaticRoleAttributeService.deleteInternalById(automaticRole.getId());
			}
		}
		//
		LOG.debug("End: Remove role [{}] by automatic role [{}]. Count: [{}/{}]", role.getCode(), automaticRole.getId(), counter, count);
		//
		return Boolean.TRUE;
	}

	public boolean isDeleteEntity() {
		return deleteEntity;
	}

	public void setDeleteEntity(boolean deleteEntity) {
		this.deleteEntity = deleteEntity;
	}
}
