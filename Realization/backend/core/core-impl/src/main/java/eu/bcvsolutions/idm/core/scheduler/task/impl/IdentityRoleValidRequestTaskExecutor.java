package eu.bcvsolutions.idm.core.scheduler.task.impl;

import java.util.List;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityRoleValidRequestDto;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmIdentityRoleValidRequestService;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityRoleValidRequestEvent.IdentityRoleValidRequestEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableTaskExecutor;

/**
 * Create new account for roles that was newly valid.
 * 
 * @author Ondřej Kopr
 *
 */
@Component(IdentityRoleValidRequestTaskExecutor.TASK_NAME)
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Description("Create new account for roles that was newly valid.")
public class IdentityRoleValidRequestTaskExecutor extends AbstractSchedulableTaskExecutor<Boolean> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(IdentityRoleValidRequestTaskExecutor.class);
	public static final String TASK_NAME = "core-identity-role-valid-request-long-running-task";
	
	@Autowired
	private IdmIdentityRoleValidRequestService validRequestService;
	
	@Autowired
	private EntityEventManager entityEventManager;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public Boolean process() {
		boolean canContinue = true;
		counter = 0L;
		List<IdmIdentityRoleValidRequestDto> list = validRequestService.findAllValid();
		// init count
		if (count == null) {
			count = Long.valueOf(list.size());
		}
		LOG.info("Account management starts for all newly valid roles from now. Count [{}]", count);
		for (IdmIdentityRoleValidRequestDto request : list) {
			try {
				// after success provisioning is request removed from db
				entityEventManager.process(new IdentityRoleValidRequestEvent(IdentityRoleValidRequestEventType.IDENTITY_ROLE_VALID, request));
			} catch (RuntimeException e) {
				// log failed operation
				request.increaseAttempt();
				request.setResult(new OperationResult.Builder(OperationState.NOT_EXECUTED).setCause(e).build());
				this.validRequestService.save(request);
			}
			counter++;
			canContinue = updateState();
			if (!canContinue) {
				break;
			}
		}
		return Boolean.TRUE;
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
