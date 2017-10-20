package eu.bcvsolutions.idm.core.scheduler.task.impl.password;

import java.util.Map;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.EntityEventManager;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableStatefulExecutor;

/**
 * Publish event PASSWORD_EXPIRED event on identity after password expired.
 * 
 * @author Radek Tomiška
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Publish PASSWORD_EXPIRED event on identity after password expired.")
public class PasswordExpiredTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmPasswordDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordExpiredTaskExecutor.class);
	//
	@Autowired private IdmPasswordService passwordService;
	@Autowired private LookupService lookupService;
	@Autowired private EntityEventManager entityEventManager;
	//
	private LocalDate expiration;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		expiration = new LocalDate();
		LOG.debug("Publishing [{}] event to identities after password expired. Check date [{}]", IdentityEventType.PASSWORD_EXPIRED, expiration);
	}

	@Override
	public Page<IdmPasswordDto> getItemsToProcess(Pageable pageable) {
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setValidTill(expiration);
		filter.setIdentityDisabled(Boolean.FALSE);
		return passwordService.find(filter, pageable);
	}

	@Override
	public Optional<OperationResult> processItem(IdmPasswordDto dto) {
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, dto.getIdentity());
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
}
