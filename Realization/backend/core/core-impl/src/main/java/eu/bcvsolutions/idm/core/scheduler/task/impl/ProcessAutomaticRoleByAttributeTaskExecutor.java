package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.exception.AcceptedException;
import eu.bcvsolutions.idm.core.api.exception.EntityNotFoundException;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.utils.AutowireHelper;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.IdmLongRunningTaskDto;
import eu.bcvsolutions.idm.core.scheduler.api.dto.filter.IdmLongRunningTaskFilter;

/**
 * Process all contracts that passed and not passed given automatic role.
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
@Component(ProcessAutomaticRoleByAttributeTaskExecutor.TASK_NAME)
public class ProcessAutomaticRoleByAttributeTaskExecutor extends AbstractAutomaticRoleTaskExecutor {

	public static final String TASK_NAME = "core-process-automatic-role-attribute-long-running-task";
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ProcessAutomaticRoleByAttributeTaskExecutor.class);
	
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmIdentityContractService identityContractService;
	
	private boolean async = true; // FIXME: make AbstractAutomaticRoleTaskExecutor stateful - use requires new + continue on exception
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		this.setAutomaticRoleId(getParameterConverter().toUuid(properties, PARAMETER_ROLE_TREE_NODE));
		super.init(properties);
	}
	
	/**
	 * Automatic role removal can be start, if previously LRT ended.
	 */
	@Override
	public void validate(IdmLongRunningTaskDto task) {
		super.validate(task);
		//
		UUID automaticRoleId = getAutomaticRoleId();
		AbstractIdmAutomaticRoleDto automaticRole = automaticRoleAttributeService.get(automaticRoleId);
		if (automaticRole == null) {
			throw new EntityNotFoundException(AbstractIdmAutomaticRoleDto.class, automaticRoleId);
		}
		//
		IdmLongRunningTaskFilter filter = new IdmLongRunningTaskFilter();
		filter.setTaskType(AutowireHelper.getTargetType(this));
		filter.setOperationState(OperationState.RUNNING);
		// filter.setRunning(Boolean.TRUE); // ignore waiting tasks is not possible => remove vs adding new role updates the same requests
		//
		// by attribute - prevent currently processed role only
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getId().equals(getLongRunningTaskId())) {
				continue;
			}
			if (longRunningTask.getTaskProperties().get(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE).equals(automaticRole.getId())) {
				throw new AcceptedException(
						CoreResultCode.AUTOMATIC_ROLE_TASK_RUNNING,
						ImmutableMap.of("taskId", longRunningTask.getId().toString())
				);
			}
		}
		//
		filter.setTaskType(AutowireHelper.getTargetType(RemoveAutomaticRoleTaskExecutor.class));
		for (IdmLongRunningTaskDto longRunningTask : getLongRunningTaskService().find(filter, null)) {
			if (longRunningTask.getId().equals(getLongRunningTaskId())) {
				continue;
			}
			if (longRunningTask.getTaskProperties().get(AbstractAutomaticRoleTaskExecutor.PARAMETER_ROLE_TREE_NODE).equals(automaticRole.getId())) {
				throw new AcceptedException(
						CoreResultCode.AUTOMATIC_ROLE_TASK_RUNNING,
						ImmutableMap.of("taskId", longRunningTask.getId().toString())
				);
			}
		}
	}
	
	@Override
	public Boolean process() {
		UUID automaticRoleId = getAutomaticRoleId();
		IdmAutomaticRoleAttributeDto automaticRolAttributeDto = automaticRoleAttributeService.get(automaticRoleId);
		if (automaticRoleId == null || automaticRolAttributeDto == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
		}
		//
		Set<AbstractIdmAutomaticRoleDto> setWithAutomaticRole = Sets.newHashSet(automaticRolAttributeDto);
		//
		List<String> failedEntitiesAdd = new ArrayList<>();
		List<String> failedEntitiesRemove = new ArrayList<>();
		//
		// by contract
		List<UUID> newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, null).getContent();
		List<UUID> newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, null).getContent();
		//
		counter = 0L;
		count = Long.valueOf(newPassedContracts.size() + newNotPassedContracts.size());
		//
		// assign new passed roles
		boolean canContinue = true;
    	//
    	// process contracts
		for (UUID contractId : newPassedContracts) {
			IdmIdentityContractDto contract = identityContractService.get(contractId);
			//
			try {
				if (async) {
					automaticRoleAttributeService.addAutomaticRoles(contract, setWithAutomaticRole);
				} else {
					automaticRoleAttributeService.addAutomaticRolesInternal(contract, setWithAutomaticRole);
				}
				counter++;
			} catch (Exception ex) {
				LOG.error("Error while add new automatic role id [{}] to contract id [{}] and identity id [{}]", 
						automaticRoleId, contractId, contract.getIdentity(), ex);
				failedEntitiesAdd.add(contractId.toString());
			} finally {
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
		}
    	//
		if (canContinue) {
			for (UUID contractId : newNotPassedContracts) {
				try { 
					if (async) {
						automaticRoleAttributeService.removeAutomaticRoles(contractId, setWithAutomaticRole);
					} else {
						automaticRoleAttributeService.removeAutomaticRolesInternal(contractId, setWithAutomaticRole);
					}
					counter++;
				} catch (Exception ex) {
					LOG.error("Error while remove automatic role id [{}] from contract id [{}].",
									automaticRoleId, contractId, ex);
					failedEntitiesRemove.add(contractId.toString());
				} finally {
					canContinue = updateState();
					if (!canContinue) {
						break;
					}
				}
			}
		}
    	//
    	if (!failedEntitiesAdd.isEmpty() || !failedEntitiesRemove.isEmpty()) {
    		throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_PROCESS_TASK_NOT_COMPLETE,
					ImmutableMap.of(
							"automaticRole", automaticRoleId,
							"failedAddEntities", StringUtils.join(failedEntitiesAdd, ","),
							"failedRemoveEntities", StringUtils.join(failedEntitiesRemove, ",")));
    	}
		//
		return Boolean.TRUE;
	}

	public void setAsync(boolean async) {
		this.async = async;
	}
	
	@Override
	public List<IdmFormAttributeDto> getFormAttributes() {
		IdmFormAttributeDto automaticRoleAttribute = new IdmFormAttributeDto(
				PARAMETER_ROLE_TREE_NODE,
				PARAMETER_ROLE_TREE_NODE, 
				PersistentType.UUID,
				BaseFaceType.AUTOMATIC_ROLE_ATTRIBUTE_SELECT);
		automaticRoleAttribute.setRequired(true);
		//
		return Lists.newArrayList(automaticRoleAttribute);
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
