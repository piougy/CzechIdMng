package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.eav.api.domain.BaseFaceType;
import eu.bcvsolutions.idm.core.eav.api.domain.PersistentType;
import eu.bcvsolutions.idm.core.eav.api.dto.IdmFormAttributeDto;

/**
 * Process all contracts that passed and not passed given automatic role
 * 
 * @author Ondrej Kopr
 *
 */
@Component(ProcessAutomaticRoleByAttributeTaskExecutor.TASK_NAME)
public class ProcessAutomaticRoleByAttributeTaskExecutor extends AbstractAutomaticRoleTaskExecutor {

	public static final String TASK_NAME = "core-process-automatic-role-attribute-long-running-task";
	private static final int PAGE_SIZE = 100;
	
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
	
	@Override
	public Boolean process() {
		UUID automaticRoleId = getAutomaticRoleId();
		IdmAutomaticRoleAttributeDto automaticRolAttributeDto = automaticRoleAttributeService.get(automaticRoleId);
		if (automaticRoleId == null || automaticRolAttributeDto == null) {
			throw new ResultCodeException(CoreResultCode.AUTOMATIC_ROLE_TASK_EMPTY);
		}
		// For every query is get first page with 100 rows
		PageRequest defaultPageRequest = PageRequest.of(0, PAGE_SIZE);
		Set<AbstractIdmAutomaticRoleDto> setWithAutomaticRole = Sets.newHashSet(automaticRolAttributeDto);
		//
		List<String> failedEntitiesAdd = new ArrayList<>();
		List<String> failedEntitiesRemove = new ArrayList<>();
		//
		// by contract
		Page<UUID> newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, defaultPageRequest);
    	Page<UUID> newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, defaultPageRequest);
		//
		counter = 0L;
		count = Long.valueOf(newPassedContracts.getTotalElements() + newNotPassedContracts.getTotalElements());
		//
		// assign new passed roles
		boolean canContinue = true;
    	//
    	// process contracts
    	while (canContinue) {
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
    		if (newPassedContracts.hasNext()) {
    			newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, defaultPageRequest);
    		} else {
    			break;
    		}
    	}
    	//
    	while (canContinue) {
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
    		if (newNotPassedContracts.hasNext()) {
    			newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, defaultPageRequest);
    		} else {
    			break;
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
