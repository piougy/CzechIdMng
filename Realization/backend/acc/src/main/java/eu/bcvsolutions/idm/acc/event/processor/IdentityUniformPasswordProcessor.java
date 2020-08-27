package eu.bcvsolutions.idm.acc.event.processor;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.acc.dto.AccAccountDto;
import eu.bcvsolutions.idm.acc.dto.AccPasswordChangeOptionDto;
import eu.bcvsolutions.idm.acc.dto.filter.AccAccountFilter;
import eu.bcvsolutions.idm.acc.service.api.AccAccountService;
import eu.bcvsolutions.idm.acc.service.api.AccUniformPasswordService;
import eu.bcvsolutions.idm.acc.service.api.PasswordFilterManager;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.PasswordChangeDto;
import eu.bcvsolutions.idm.core.api.event.CoreEvent;
import eu.bcvsolutions.idm.core.api.event.CoreEventProcessor;
import eu.bcvsolutions.idm.core.api.event.DefaultEventResult;
import eu.bcvsolutions.idm.core.api.event.EntityEvent;
import eu.bcvsolutions.idm.core.api.event.EventResult;
import eu.bcvsolutions.idm.core.api.event.processor.IdentityProcessor;
import eu.bcvsolutions.idm.core.model.event.IdentityEvent.IdentityEventType;
import eu.bcvsolutions.idm.core.model.event.processor.identity.IdentityPasswordProcessor;

/**
 * Unite accounts by uniform password definition.
 * Processors behavior can be turn of by {@link IdentityUniformPasswordProcessor#SKIP_PASSWORD_UNIFORM_SYSTEM} property.
 *
 * @author Ondrej Kopr
 * @since 10.5.0
 *
 */
@Component
@Description("Unite final accounts by uniform password definiton.")
public class IdentityUniformPasswordProcessor 
		extends CoreEventProcessor<IdmIdentityDto> 
		implements IdentityProcessor {
	
	public static final String PROCESSOR_NAME = "identity-uniform-password-processor";
	public static final String SKIP_PASSWORD_UNIFORM_SYSTEM = "skipPasswordUniformSystem"; // Whole behavior can be skipped by configuration

	@Autowired
	private AccUniformPasswordService uniformPasswordService;
	@Autowired
	private AccAccountService accountService;
	
	public IdentityUniformPasswordProcessor() {
		super(IdentityEventType.PASSWORD);
	}
	
	@Override
	public String getName() {
		return PROCESSOR_NAME;
	}

	@Override
	public EventResult<IdmIdentityDto> process(EntityEvent<IdmIdentityDto> event) {
		PasswordChangeDto passwordChangeDto = (PasswordChangeDto) event.getProperties().get(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO);
		UUID excludedSystem = (UUID) event.getProperties().get(PasswordFilterManager.EXCLUDED_SYSTEM);
		IdmIdentityDto identity = event.getContent();

		// If password change contains all get all accounts - for these account must be setup echo!
		if (passwordChangeDto.isAll()) {
			AccAccountFilter accountFilter = new AccAccountFilter();
			accountFilter.setSupportChangePassword(Boolean.TRUE);
			accountFilter.setIdentityId(identity.getId());
			List<AccAccountDto> accounts = accountService.find(accountFilter, null).getContent();
			passwordChangeDto.setAccounts(accounts //
					.stream() //
					.map(AccAccountDto::getId) //
					.map(UUID::toString) //
					.collect(Collectors.toList() //
					));
		}
		
		List<String> accounts = passwordChangeDto.getAccounts();
		if (!CollectionUtils.isEmpty(accounts)) {
			List<AccPasswordChangeOptionDto> findOptionsForPasswordChange = uniformPasswordService.findOptionsForPasswordChange(identity);

			Set<String> finalAccounts = Sets.newHashSet();
			for (String account : accounts) {
				findOptionsForPasswordChange.forEach(option -> {
					List<String> accountOptions = option.getAccounts();
					if (accountOptions.contains(account)) {
						finalAccounts.addAll(accountOptions);

						// At least one option with change in IdM and password must be changed even trought IdM
						if (option.isChangeInIdm()) {
							passwordChangeDto.setIdm(true);
						}
					}

				});
			}

			if (excludedSystem != null) {
				finalAccounts.removeIf(account -> {
					AccAccountFilter filter = new AccAccountFilter();
					filter.setId(UUID.fromString(account));
					filter.setSystemId(excludedSystem);
					return accountService.count(filter) > 0;
				});
			}
			
			passwordChangeDto.setAccounts(Lists.newArrayList(finalAccounts));
			event.getProperties().put(IdentityPasswordProcessor.PROPERTY_PASSWORD_CHANGE_DTO, passwordChangeDto);
		}

		return new DefaultEventResult<>(event, this);
	}

	@Override
	public boolean conditional(EntityEvent<IdmIdentityDto> event) {
		return super.conditional(event) && !getBooleanProperty(SKIP_PASSWORD_UNIFORM_SYSTEM, event.getProperties());
	}

	/**
	 * Before password change
	 */
	@Override
    public int getOrder() {
    	return CoreEvent.DEFAULT_ORDER - 10;
    }
}
