package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.ContractState;
import eu.bcvsolutions.idm.core.api.dto.AbstractIdmAutomaticRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmAutomaticRoleAttributeDto;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.service.IdmAutomaticRoleAttributeService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmIdentityRole_;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Recalculate all automatic role by attribute for all contracts.
 * Automatic roles was added by iterate over all {@link IdmAutomaticRoleAttributeDto}.
 * For each {@link IdmAutomaticRoleAttributeDto} will be founded all newly passed {@link IdmIdentityContractDto}
 * and {@link IdmIdentityContractDto} that contains automatic role and role must be removed.
 * <br />
 * <br />
 * For each contract is created maximal twice {@link IdmRoleRequestDto}. One request contains all newly assigned roles
 * and the second contains newly removed roles. This is now only one solution.
 * <br />
 * TODO: after some big refactor can be processed all concept in one request.
 *
 * @author Ondrej Kopr
 *
 */

@Service
@DisallowConcurrentExecution
@Description("Recalculate all automatic roles by attribute. Creates new request with concepts.")
public class ProcessAllAutomaticRoleByAttributeTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {

	private static final int DEFAULT_PAGE_SIZE_ROLE = 10;
	private static final int DEFAULT_PAGE_SIZE_PAGE_SIZE_IDENTITIES = 100;

	@Autowired
	private IdmAutomaticRoleAttributeService automaticRoleAttributeService;
	@Autowired
	private IdmIdentityContractService identityContractService;
	@Autowired
	private IdmIdentityRoleService identityRoleService;
	@Autowired
	private IdmRoleRequestService roleRequestService;
	
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
	 * Start recalculation for automatic role. All identity roles (newly added and removed) will be added by {@link IdmRoleRequestDto}.
	 * But role request is created for each contract twice. One for newly added and one for newly removed. This is now only solutions.
	 *
	 * @param automaticRolAttributeDto
	 */
	private void processAutomaticRoleForContract(IdmAutomaticRoleAttributeDto automaticRolAttributeDto) {
		UUID automaticRoleId = automaticRolAttributeDto.getId();
		Set<AbstractIdmAutomaticRoleDto> automaticRoleSet = new HashSet<AbstractIdmAutomaticRoleDto>();
		automaticRoleSet.add(automaticRolAttributeDto);
		//
    	// process contracts
    	Page<UUID> newPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, true, new PageRequest(0, DEFAULT_PAGE_SIZE_PAGE_SIZE_IDENTITIES));
    	Page<UUID> newNotPassedContracts = automaticRoleAttributeService.getContractsForAutomaticRole(automaticRoleId, false, new PageRequest(0, DEFAULT_PAGE_SIZE_PAGE_SIZE_IDENTITIES));
    	//
    	//
    	boolean canContinue = true;
    	while (canContinue) {
    		for(UUID contractId : newPassedContracts) {
    			// Concepts that will be added
    			List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
    			//
    			IdmIdentityContractDto contract = identityContractService.get(contractId);
    			//
    			// check for contract validity
    			if (contract.getState() == ContractState.DISABLED || !contract.isValidNowOrInFuture()) {
    				continue;
    			}
    			IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
    			concept.setIdentityContract(contract.getId());
    			concept.setValidFrom(contract.getValidFrom());
    			concept.setValidTill(contract.getValidTill());
    			concept.setRole(automaticRolAttributeDto.getRole());
    			concept.setAutomaticRole(automaticRoleId);
				concept.setOperation(ConceptRoleRequestOperation.ADD);
				concepts.add(concept);

				if (!concepts.isEmpty()) {
    				roleRequestService.executeConceptsImmediate(contract.getIdentity(), concepts);
    			}
 
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
    			// Concepts that will be added
    			List<IdmConceptRoleRequestDto> concepts = new ArrayList<IdmConceptRoleRequestDto>();
    			//
    			// Identity id is get from embedded identity role. This is little speedup.
    			UUID identityId = null;
    			//
    			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
    			filter.setIdentityContractId(contractId);
    			filter.setAutomaticRoleId(automaticRoleId);
    			List<IdmIdentityRoleDto> identityRoles = identityRoleService.find(filter, null).getContent();
    			for (IdmIdentityRoleDto identityRole : identityRoles) {
    				IdmConceptRoleRequestDto concept = new IdmConceptRoleRequestDto();
        			concept.setIdentityContract(contractId);
        			concept.setRole(automaticRolAttributeDto.getRole());
        			concept.setAutomaticRole(automaticRoleId);
    				concept.setOperation(ConceptRoleRequestOperation.REMOVE);
    				concepts.add(concept);

    				if (identityId == null) {
    					IdmIdentityContractDto contractDto = DtoUtils.getEmbedded(identityRole, IdmIdentityRole_.identityContract, IdmIdentityContractDto.class, null);
						identityId = contractDto.getIdentity();
    				}
    			}

    			if (!concepts.isEmpty()) {
    				roleRequestService.executeConceptsImmediate(identityId, concepts);
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
