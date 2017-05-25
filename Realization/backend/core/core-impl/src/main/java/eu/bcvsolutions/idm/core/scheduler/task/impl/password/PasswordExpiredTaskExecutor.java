package eu.bcvsolutions.idm.core.scheduler.task.impl.password;

import java.util.Map;
import java.util.Optional;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.quartz.DisallowConcurrentExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.filter.PasswordFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.model.service.api.IdmPasswordService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.service.api.NotificationManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableStatefulExecutor;

/**
 * Sends warning after password expired.
 * 
 * @author Radek Tomiška
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Sends warning notification after password expired.")
public class PasswordExpiredTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmPasswordDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordExpiredTaskExecutor.class);
	//
	@Autowired private IdmPasswordService passwordService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private LookupService lookupService;
	@Autowired private ConfigurationService configurationService;
	//
	private LocalDate expiration;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		expiration = new LocalDate();
		LOG.debug("Send warning to identities with expired passwords. Check date [{}]", expiration);
	}

	@Override
	public Page<IdmPasswordDto> getItemsToProcess(Pageable pageable) {
		PasswordFilter filter = new PasswordFilter();
		filter.setValidTill(expiration);
		return passwordService.find(filter, pageable);
	}

	@Override
	public Optional<OperationResult> processItem(IdmPasswordDto dto) {
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, dto.getIdentity());
		LOG.info("Sending warning notification to identity [{}], password expired in [{}]",  identity.getUsername(), dto.getValidTill());
		try {
			DateTimeFormatter dateFormat = DateTimeFormat.forPattern(configurationService.getDateFormat());
			//
			notificationManager.send(
					CoreModuleDescriptor.TOPIC_PASSWORD_EXPIRED, 
					new IdmMessageDto
						.Builder(NotificationLevel.WARNING)
						.addParameter("expiration", dateFormat.print(dto.getValidTill()))
						.addParameter("identity", identity)
						// TODO: where is the best place for FE urls?
						// TODO: url to password reset?
						// .addParameter("url", configurationService.getFrontendUrl(String.format("password/reset?username=%s", identity.getUsername())))
						.build(), 
					identity);
			return Optional.of(new OperationResult.Builder(OperationState.EXECUTED).build());
		} catch (Exception ex) {
			LOG.error("Sending warning notification to identity [{}], password expires in [{}] failed", dto.getIdentity(), dto.getValidTill(), ex);
			return Optional.of(new OperationResult.Builder(OperationState.EXCEPTION)
					.setCause(ex)
					// TODO: set model
					.build());
		}
	}
}
