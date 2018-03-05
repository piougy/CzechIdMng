package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.scheduler.api.service.StatelessAsynchronousTask;

/**
 * Recalculate all automatic role by attribute for all contracts.
 *
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */

@Service
@DisallowConcurrentExecution
@Description("Recalculate all automatic roles by attribute.")
public class ProcessAllAutomaticRoleByAttributeTaskExecutor extends StatelessAsynchronousTask {

	private static final int DEFAULT_PAGE_SIZE_ROLE = 10;
	private static final int DEFAULT_PAGE_SIZE_PAGE_SIZE_IDENTITIES = 100;

	@Autowired private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	
	
	@Override
	public Boolean process() {
		// found all IdmAutomaticRoleAttributeDto for process
		Page<IdmAutomaticRoleAttributeDto> toProcessOthers= automaticRoleAttributeService.findAllToProcess(null, new PageRequest(0, DEFAULT_PAGE_SIZE_ROLE));
		boolean canContinue = true;
		//
		this.counter = 0L;
		this.count = Long.valueOf(toProcessOthers.getTotalElements());
		//
		// others
		while (toProcessOthers.hasContent()) {
			for (IdmAutomaticRoleAttributeDto automaticAttribute : toProcessOthers) {
				// start recalculation
				processAutomaticRoleForContract(automaticAttribute);
				//
				counter++;
				canContinue = updateState();
				if (!canContinue) {
					break;
				}
			}
			if (!toProcessOthers.hasNext()) {
				break;
			}
			toProcessOthers = automaticRoleAttributeService.findAllToProcess(null, toProcessOthers.nextPageable());
		}
		//
		return Boolean.TRUE;
	}
	
	/**
	 * Start recalculation for automatic role
	 *
	 * @param automaticRolAttributeDto
	 */
	private void processAutomaticRoleForContract(IdmAutomaticRoleAttributeDto automaticRolAttributeDto) {
		UUID automaticRoleId = automaticRolAttributeDto.getId();
		Set<AbstractIdmAutomaticRoleDto> setWithAutomaticRole = Sets.newHashSet(automaticRolAttributeDto);
		//
    	// process contracts
    	Page<UUID> newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, new PageRequest(0, DEFAULT_PAGE_SIZE_PAGE_SIZE_IDENTITIES));
    	Page<UUID> newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, new PageRequest(0, DEFAULT_PAGE_SIZE_PAGE_SIZE_IDENTITIES));
    	//
    	boolean canContinue = true;
    	while (canContinue) {
    		for(UUID contractId : newPassedContracts) {
    			IdmIdentityContractDto contract = identityContractService.get(contractId);
    			//
				IdmRoleRequestDto roleRequest = automaticRoleAttributeService.prepareAddAutomaticRoles(contract, setWithAutomaticRole);
				roleRequestService.startRequestInternal(roleRequest.getId(), false);
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
	}
}
