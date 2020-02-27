package eu.bcvsolutions.idm.core.scheduler.task.impl.password;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.DefaultResultModel;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword_;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.scheduler.api.service.AbstractSchedulableStatefulExecutor;

/**
 * Send notification for user after password expired and publish PASSWORD_EXPIRED event.
 * 
 * @author Radek Tomiška
 *
 */
@Component(PasswordExpiredTaskExecutor.TASK_NAME)
@DisallowConcurrentExecution
@Description("Send notification for user after password expired and publish PASSWORD_EXPIRED event.")
public class PasswordExpiredTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmPasswordDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordExpiredTaskExecutor.class);
	public static final String TASK_NAME = "core-password-expired-long-running-task";
	//
	@Autowired private IdmPasswordService passwordService;
	@Autowired private EntityEventManager entityEventManager;
	//
	private LocalDate expiration;
	
	@Override
	public String getName() {
		return TASK_NAME;
	}
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		expiration = LocalDate.now();
		LOG.debug("Publishing [{}] event to identities after password expired. Check date [{}]", IdentityEventType.PASSWORD_EXPIRED, expiration);
	}

	@Override
	public Page<IdmPasswordDto> getItemsToProcess(Pageable pageable) {
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setValidTill(expiration); // valid till filter <=
		filter.setIdentityDisabled(Boolean.FALSE);
		//
		return passwordService.find(filter, pageable);
	}

	@Override
	public Optional<OperationResult> processItem(IdmPasswordDto dto) {
		if (!expiration.isAfter(dto.getValidTill())) {
			// skip the same date (valid till filter <=) - just info into lrt. Will be processed next day.
			return Optional.of(new OperationResult
					.Builder(OperationState.NOT_EXECUTED)
					.setModel(new DefaultResultModel(CoreResultCode.PASSWORD_EXPIRATION_TODAY_INFO))
					.build());
		}
		//
		IdmIdentityDto identity = getLookupService().lookupEmbeddedDto(dto, IdmPassword_.identity);
		LOG.info("Publishing [{}] event to identity [{}], password expired in [{}]", 
				IdentityEventType.PASSWORD_EXPIRED, identity.getUsername(), dto.getValidTill());
		try {
			entityEventManager.process(new IdentityEvent(IdentityEventType.PASSWORD_EXPIRED, identity));
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			LOG.error("Publishing [{}] event to identity [{}], password expired in [{}] failed", 
					IdentityEventType.PASSWORD_EXPIRED, dto.getIdentity(), dto.getValidTill(), ex);
			return Optional.of(new OperationResult.Builder(OperationState.EXCEPTION)
					.setCause(ex)
					// TODO: set model
					.build());
		}
	}
	
	@Override
    public boolean isRecoverable() {
    	return true;
    }
}
