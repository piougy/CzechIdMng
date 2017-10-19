package eu.bcvsolutions.idm.core.scheduler.task.impl.password;

import java.util.List;
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

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.OperationState;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.filter.IdmPasswordFilter;
import eu.bcvsolutions.idm.core.api.entity.OperationResult;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.notification.api.domain.NotificationLevel;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.scheduler.service.impl.AbstractSchedulableStatefulExecutor;

/**
 * Sends warning before password expires.
 * 
 * @author Radek Tomiška
 *
 */
@Service
@DisallowConcurrentExecution
@Description("Sends warning notification before password expires.")
public class PasswordExpirationWarningTaskExecutor extends AbstractSchedulableStatefulExecutor<IdmPasswordDto> {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PasswordExpirationWarningTaskExecutor.class);
	protected static final String PARAMETER_DAYS_BEFORE = "days before";
	//
	@Autowired private IdmPasswordService passwordService;
	@Autowired private NotificationManager notificationManager;
	@Autowired private LookupService lookupService;
	@Autowired private ConfigurationService configurationService;
	//
	private LocalDate expiration;
	private Long daysBefore;
	
	@Override
	public void init(Map<String, Object> properties) {
		super.init(properties);
		//
		daysBefore = getParameterConverter().toLong(properties, PARAMETER_DAYS_BEFORE);
		if (daysBefore == null || daysBefore.compareTo(0L) <= 0) {
			throw new ResultCodeException(
					CoreResultCode.PASSWORD_EXPIRATION_TASK_DAYS_BEFORE, 
					ImmutableMap.of("daysBefore", daysBefore == null ? "null" : daysBefore));
		}
		expiration = new LocalDate().plusDays(daysBefore.intValue() - 1); // valid till filter <=
		LOG.debug("Send warning to identities with password expiration less than [{}]", expiration);
	}

	@Override
	public Page<IdmPasswordDto> getItemsToProcess(Pageable pageable) {
		IdmPasswordFilter filter = new IdmPasswordFilter();
		filter.setValidTill(expiration);
		filter.setIdentityDisabled(Boolean.FALSE);
		Page<IdmPasswordDto> result =  passwordService.find(filter, pageable);
		return result;
	}

	@Override
	public Optional<OperationResult> processItem(IdmPasswordDto dto) {
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, dto.getIdentity());
		LOG.info("Sending warning notification to identity [{}], password expires in [{}]",  identity.getUsername(), dto.getValidTill());
		try {
			DateTimeFormatter dateFormat = DateTimeFormat.forPattern(configurationService.getDateFormat());
			//
			notificationManager.send(
					CoreModuleDescriptor.TOPIC_PASSWORD_EXPIRATION_WARNING, 
					new IdmMessageDto
						.Builder(NotificationLevel.WARNING)
						.addParameter("expiration", dateFormat.print(dto.getValidTill()))
						.addParameter("identity", identity)
						// TODO: where is the best place for FE urls?
						.addParameter("url", configurationService.getFrontendUrl(String.format("password/change?username=%s", identity.getUsername())))
						.addParameter("daysBefore", daysBefore)
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
	
	@Override
	public List<String> getPropertyNames() {
		List<String> parameters = super.getPropertyNames();
		parameters.add(PARAMETER_DAYS_BEFORE);
		return parameters;
	}
}
