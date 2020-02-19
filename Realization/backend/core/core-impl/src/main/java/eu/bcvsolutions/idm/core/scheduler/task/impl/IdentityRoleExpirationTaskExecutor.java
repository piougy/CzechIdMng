package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.quartz.DisallowConcurrentExecution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.ConceptRoleRequestOperation;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestState;
import eu.bcvsolutions.idm.core.api.domain.RoleRequestedByType;
import eu.bcvsolutions.idm.core.api.dto.IdmConceptRoleRequestDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityContractDto;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleDto;
import eu.bcvsolutions.idm.core.api.dto.IdmRoleRequestDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.IdmConceptRoleRequestService;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleService;
import eu.bcvsolutions.idm.core.api.service.IdmRoleRequestService;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent;
import eu.bcvsolutions.idm.core.model.event.RoleRequestEvent.RoleRequestEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Long running task for expired identity roles removal.
 * Expected usage is in cooperation with CronTaskTrigger, running
 * once a day after midnight.
 * 
 * @author Jan Helbich
 * @author Radek Tomiška
 */
@Service(IdentityRoleExpirationTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Removes expired assigned roles from identites.")
public class IdentityRoleExpirationTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmIdentityRoleDto> {
	
	private static final Logger LOG = LoggerFactory.getLogger(IdentityRoleExpirationTaskExecutor.class);
	public static final String TASK_NAME = "core-identity-role-expiration-long-running-task";
	//
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
		LOG.info("Expired roles removal task was inintialized for expiration less than [{}].", expiration);
	}
	
	@Override
	public Page<IdmIdentityRoleDto> getItemsToProcess(Pageable pageable) {
		// 0 => from start - roles from previous search are already removed
		return identityRoleService.findDirectExpiredRoles(
				expiration, 
				// sort by identity
				PageRequest.of(
						0, 
						pageable.getPageSize(), 
						Sort.by(
								Direction.ASC, 
								// sort by identity - removed roles will grouped into role requests (TODO)
								IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT + "." + IdmIdentityContractDto.PROPERTY_IDENTITY, 
								IdmIdentityRoleDto.PROPERTY_VALID_TILL
						)
				)
		);
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
	public boolean supportsQueue() {
		return false;
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
	
	@Override
	public Optional<OperationResult> processItem(IdmIdentityRoleDto identityRole) {
		LOG.info("Remove expired assigned role [{}], valid till is less than [{}]",  identityRole.getId(), expiration);
		//
		if (identityRoleService.get(identityRole) == null) {
			// already deleted - skipping
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		IdmIdentityContractDto contract = getLookupService().lookupEmbeddedDto(identityRole, IdmIdentityRoleDto.PROPERTY_IDENTITY_CONTRACT);
		if (contract == null) {
			// already deleted - skipping
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		}
		UUID identityId = contract.getIdentity();
		//
		try {
			LOG.debug("Remove expired role [{}] from contract [{}] by internal role request.", identityRole.getRole(), contract.getId());
			//
			IdmRoleRequestDto roleRequest = new IdmRoleRequestDto();
			roleRequest.setState(RoleRequestState.CONCEPT);
			roleRequest.setExecuteImmediately(true); // without approval
			roleRequest.setApplicant(identityId);
			roleRequest.setRequestedByType(RoleRequestedByType.AUTOMATICALLY);
			roleRequest = roleRequestService.save(roleRequest);
			//
			IdmConceptRoleRequestDto conceptRoleRequest = new IdmConceptRoleRequestDto();
			conceptRoleRequest.setIdentityRole(identityRole.getId());
			conceptRoleRequest.setRole(identityRole.getRole());
			conceptRoleRequest.setOperation(ConceptRoleRequestOperation.REMOVE);
			conceptRoleRequest.setIdentityContract(contract.getId());
			conceptRoleRequest.setRoleRequest(roleRequest.getId());
			conceptRoleRequestService.save(conceptRoleRequest);
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
			LOG.error("Removing expired assigned role [{}] failed", identityRole.getId(), ex);
			return Optional.of(new OperationResult.Builder(OperationState.EXCEPTION)
					.setCause(ex)
					.build());
		}
	}
}
