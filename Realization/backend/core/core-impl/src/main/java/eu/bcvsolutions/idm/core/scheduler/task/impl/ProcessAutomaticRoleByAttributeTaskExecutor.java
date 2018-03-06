package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;

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
	
	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
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
				IdmRoleRequestDto roleRequest = automaticRoleAttributeService.prepareAddAutomaticRoles(contract, setWithAutomaticRole);
				roleRequestService.startRequestInternal(roleRequest.getId(), false);
				//
				counter++;
				canContinue = updateState();
				if (!canContinue) {
					break;
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
    			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
    			filter.setIdentityContractId(contractId);
    			filter.setAutomaticRoleId(automaticRoleId);
    			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
    			for (IdmIdentityRoleDto identityRole : identityRoles) {
    				IdmRoleRequestDto roleRequest = automaticRoleAttributeService.prepareRemoveAutomaticRoles(identityRole, setWithAutomaticRole);
    				roleRequestService.startRequestInternal(roleRequest.getId(), false);
    			}
    			//
    			counter++;
    			canContinue = updateState();
    			if (!canContinue) {
    				break;
    			}
    		}
    		if (newNotPassedContracts.hasNext()) {
    			newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, newNotPassedContracts.nextPageable());
    		} else {
    			break;
    		}
    	}
		//
		return Boolean.TRUE;
	}

}
