package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmIdentityRoleFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityContractService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Remove roles by expired identity contracts (=> removes assigned roles).
 * 
 * @author Radek Tomiška
 *
 */
@Service(IdentityContractExpirationTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Remove roles by expired identity contracts (=> removes assigned roles).")
public class IdentityContractExpirationTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityContractDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityContractExpirationTaskExecutor.class);
	public static final String TASK_NAME = "core-identity-contract-expiration-long-running-task";
	//
	@Autowired private IdmIdentityContractService identityContractService;
	@Autowired private IdmIdentityRoleService identityRoleService;
	@Autowired private IdmRoleRequestService roleRequestService;
	@Autowired private IdmConceptRoleRequestService conceptRoleRequestService;
	//
	private LocalDate expiration;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		expiration = LocalDate.now();
		LOG.debug("Remove roles expired identity contracts was inintialized for expiration less than [{}]", expiration);
	}

	@Override
	public Page<IdmIdentityContractDto> getItemsToProcess(Pageable pageable) {
		return identityContractService.findExpiredContracts(expiration, pageable);
	}
	
	@Override
	public boolean continueOnException() {
		return true;
	}
	
	@Override
	public boolean requireNewTransaction() {
		return true;
	}
	
	@Override
	public boolean isRecoverable() {
		return true;
	}

	@Override
	public Optional<OperationResult> processItem(IdmIdentityContractDto contract) {
		LOG.info("Remove roles by expired identity contract [{}]. Contract ended for expiration less than [{}]",  contract.getId(), expiration);
		//
		try {
			IdmIdentityRoleFilter filter = new IdmIdentityRoleFilter();
			filter.setIdentityContractId(contract.getId());
			filter.setDirectRole(Boolean.TRUE);
			// remove all referenced roles (automatic roles are included)
			List<IdmIdentityRoleDto> expiredRoles = identityRoleService.find(filter, null).getContent();
			if (expiredRoles.isEmpty()) {
				// nothing to do
				return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
			}
			//
			UUID identityId = contract.getIdentity();
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setState(RoleRequestState.CONCEPT);
			roleRequest.setExecuteImmediately(true); // without approval
			roleRequest.setApplicant(identityId);
			roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
			roleRequest = roleRequestService.save(roleRequest);
			//
			for (IdmIdentityRoleDto identityRole : expiredRoles) {
				IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
				conceptRoleRequest.setIdentityRole(identityRole.getId());
				conceptRoleRequest.setRole(identityRole.getRole());
				conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
				conceptRoleRequest.setIdentityContract(contract.getId());
				conceptRoleRequest.setRoleRequest(roleRequest.getId());
				conceptRoleRequestService.save(conceptRoleRequest);
			}
			//
			// start event with skip check authorities
			RoleRequestEvent requestEvent = new RoleRequestEvent(RoleRequestEventType.EXCECUTE, roleRequest);
			requestEvent.getProperties().put(IdmIdentityRoleService.SKIP_CHECK_AUTHORITIES, Boolean.TRUE);
			// prevent to start asynchronous event before previous update event is completed. 
			requestEvent.setSuperOwnerId(identityId);
			//
			roleRequestService.startRequestInternal(requestEvent);
			//
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			LOG.error("Removing roles of expired contract [{}] failed", contract.getId(), ex);
			return Optional.of(new OperationResult.Builder(OperationState.EXCEPTION)
					.setCause(ex)
					.build());
		}
	}
}
