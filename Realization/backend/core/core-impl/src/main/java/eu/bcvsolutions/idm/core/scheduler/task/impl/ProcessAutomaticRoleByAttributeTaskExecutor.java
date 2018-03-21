package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;

/**
 * Process all contracts that passed and not passed given automatic role
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@Description("Add or remove automatic role by IdmAutomaticRoleAttribute.")
public class ProcessAutomaticRoleByAttributeTaskExecutor extends AbstractAutomaticRoleTaskExecutor {

	private static final int PAGE_SIZE = 100;
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
			.getLogger(ProcessAutomaticRoleByAttributeTaskExecutor.class);
	
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmIdentityContractService identityContractService;
	
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
		Set<AbstractIdmAutomaticRoleDto> setWithAutomaticRole = Sets.newHashSet(automaticRolAttributeDto);
		//
		List<String> failedEntitiesAdd = new ArrayList<>();
		List<String> failedEntitiesRemove = new ArrayList<>();
		//
		// by contract
		Page<UUID> newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, new PageRequest(0, PAGE_SIZE));
    	Page<UUID> newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, new PageRequest(0, PAGE_SIZE));
		//
		counter = 0L;
		count = Long.valueOf(newNotPassedContracts.getTotalElements() + newNotPassedContracts.getTotalElements());
		//
		// assign new passed roles
		boolean canContinue = true;
    	//
    	// process contracts
    	canContinue = true;
    	while (canContinue) {
    		for(UUID contractId : newPassedContracts) {
    			IdmIdentityContractDto contract = identityContractService.get(contractId);
    			//
    			// check for contract validity
    			if (contract.getState() == ContractState.DISABLED || !contract.isValidNowOrInFuture()) {
    				continue;
    			}
    			//
    			try {
    				automaticRoleAttributeService.addAutomaticRoles(contract, setWithAutomaticRole);
    				counter++;
    			} catch (Exception ex) {
    				LOG.error(
    						MessageFormat.format("Error while add new automatic role id [{0}] to contract id [{1}] and identity id [{2}]",
    								automaticRoleId, contractId, contract.getIdentity()), ex);
    				failedEntitiesAdd.add(contractId.toString());
    			} finally {
    				canContinue = updateState();
    				if (!canContinue) {
    					break;
    				}
				}
    		}
    		if (newPassedContracts.hasNext()) {
    			newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, newPassedContracts.nextPageable());
    		} else {
    			break;
    		}
    	}
    	//
    	while (canContinue) {
    		for(UUID contractId : newNotPassedContracts) {
    			try { 
    				automaticRoleAttributeService.removeAutomaticRoles(contractId, setWithAutomaticRole);
    				counter++;
    			} catch (Exception ex) {
    				LOG.error(
    						MessageFormat.format("Error while remove automatic role id [{0}] from contract id [{1}].",
    								automaticRoleId, contractId), ex);
    				failedEntitiesRemove.add(contractId.toString());
    			} finally {
    				canContinue = updateState();
    				if (!canContinue) {
    					break;
    				}
				}
    		}
    		if (newNotPassedContracts.hasNext()) {
    			newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, newNotPassedContracts.nextPageable());
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

}
