package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.Seconds;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword_;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;

/**
 * Default implementation of authentication manager {@link AuthenticationManager}.
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 *
 */
@Service
public class DefaultAuthenticationManager implements AuthenticationManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuthenticationManager.class);
	
	private final List<Authenticator> authenticators;
	private final IdmPasswordService passwordService;
	private final IdmPasswordPolicyService passwordPolicyService;
	private final NotificationManager notificationManager;
	@Autowired
	private ConfigurationService configurationService;

	@Autowired
	public DefaultAuthenticationManager(
			List<Authenticator> authenticators,
			IdmPasswordService passwordService,
			IdmPasswordPolicyService passwordPolicyService,
			NotificationManager notificationManager) {
		//
		Assert.notNull(authenticators);
		Assert.notNull(passwordService);
		Assert.notNull(passwordPolicyService);
		Assert.notNull(notificationManager);
		//
		this.authenticators = authenticators;
		this.passwordService = passwordService;
		this.passwordPolicyService = passwordPolicyService;
		this.notificationManager = notificationManager;
	}

	@Override
	public LoginDto authenticate(LoginDto loginDto) {
		// authenticate
		return authenticateOverAuthenticator(loginDto);
	}
	
	/**
	 * Get enabled {@link Authenticator} and sort by order
	 */
	private List<Authenticator> getEnabledAuthenticators() {
		// disable/enable BE modules
		List<Authenticator> enabledAuthenticator = this.authenticators.stream().filter(auth -> !auth.isDisabled()).collect(Collectors.toList());
		// sort by ordered
		AnnotationAwareOrderComparator.sort(enabledAuthenticator);
		return enabledAuthenticator;
	}
	
	/**
	 * Authenticate {@link LoginDto} over all found {@link Authenticator}
	 * 
	 * @param loginDto
	 */
	private LoginDto authenticateOverAuthenticator(LoginDto loginDto) {

		Assert.notNull(authenticators);
		//
		List<LoginDto> resultsList = new LinkedList<>();
		RuntimeException firstFailture = null;
		//
		// check if user can log in and hasn't administrator permission
		IdmPasswordDto passwordDto = passwordService.findOrCreateByIdentity(loginDto.getUsername());
		if (passwordDto == null) {
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Invalid login or password.");
		}
		if (passwordDto.getBlockLoginDate() != null && passwordDto.getBlockLoginDate().isAfterNow()) {
			LOG.info("Identity {} has blocked login to IdM.",
					loginDto.getUsername());
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Invalid login or password.");
		}
		//
		for(Authenticator authenticator : getEnabledAuthenticators()) {
			LOG.debug("AuthenticationManager call authenticate by [{}].", authenticator.getName());
			try {
				LoginDto result = authenticator.authenticate(cloneLoginDto(loginDto));
				if (result == null) { // not done
					// continue, authenticator is not implemented or etc.
					continue;
				}
				if (authenticator.getExceptedResult() == AuthenticationResponseEnum.SUFFICIENT) {
					passwordDto = passwordService.setLastSuccessfulLogin(passwordDto);
					return result;
				}
				// if otherwise add result too list and continue
				resultsList.add(result);
			} catch (RuntimeException e) {
				// if excepted response is REQUISITE exit immediately with error
				if (authenticator.getExceptedResult() == AuthenticationResponseEnum.REQUISITE) {
					throw e;
				}
				// if otherwise save first failure into exception
				if (firstFailture == null) {
					firstFailture = e;
				}
			}
		}
		//
		// authenticator is sorted by implement ordered, return first success authenticate authenticator, if don't exist any otherwise throw first failure
		if (resultsList.isEmpty()) {
			passwordDto = passwordService.increaseUnsuccessfulAttempts(passwordDto);
			checkLoginAttempts(passwordDto, loginDto);
			throw firstFailture;
		}
		passwordDto = passwordService.setLastSuccessfulLogin(passwordDto);
		return resultsList.get(0);
	}
	
	/**
	 * Check current login attempts and if attempts exceeding lock authorization for time
	 * and send notification to user.
	 *
	 * @param passwordDto
	 * @param loginDto
	 */
	private void checkLoginAttempts(IdmPasswordDto passwordDto, LoginDto loginDto) {
		IdmPasswordPolicyDto validatePolicy = passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		// check if exists validate policy and then check if is exceeded max unsuccessful attempts
		if (validatePolicy != null && validatePolicy.getMaxUnsuccessfulAttempts() != null &&
				passwordDto.getUnsuccessfulAttempts() >= validatePolicy.getMaxUnsuccessfulAttempts()) {
			if (validatePolicy.getBlockLoginTime() != null) {
				int lockLoginTime = validatePolicy.getBlockLoginTime().intValue();
				passwordDto.setBlockLoginDate(new DateTime().plus(Seconds.seconds(lockLoginTime)));
				passwordDto = passwordService.save(passwordDto);
				IdmIdentityDto identityDto = DtoUtils.getEmbedded(passwordDto, IdmPassword_.identity);
				//
				DateTimeFormatter formatter = DateTimeFormat.forPattern(configurationService.getDateTimeSecondsFormat());
				String dateAsString = passwordDto.getBlockLoginDate().toString(formatter);
				//
				LOG.warn("For identity username: {} was lock authentization to IdM for {} seconds. Authentization will be available after: {}.",
						loginDto.getUsername(), lockLoginTime, dateAsString);
				// send notification to identity
				notificationManager.send(CoreModuleDescriptor.TOPIC_LOGIN_BLOCKED,
						new IdmMessageDto.Builder()
						.addParameter("username", loginDto.getUsername())
						.addParameter("after", dateAsString)
						.addParameter("unsuccessfulAttempts", passwordDto.getUnsuccessfulAttempts())
						.build(),
						identityDto);
			} else {
				LOG.error("For password policy: {} isn't correctly filled lock login time!",
						validatePolicy.getName());
			}
		}
	}
	
	/**
	 * Clone object {@link LoginDto} without inner class {@link IdmJwtAuthentication}
	 * @param loginDto
	 * @return
	 */
	private LoginDto cloneLoginDto(LoginDto loginDto) {
		LoginDto clone = new LoginDto();
		clone.setToken(loginDto.getToken());
		clone.setUsername(loginDto.getUsername());
		clone.setAuthenticationModule(loginDto.getAuthenticationModule());
		clone.setSkipMustChange(loginDto.isSkipMustChange());
		clone.setPassword(new GuardedString(loginDto.getPassword().asBytes()));
		return clone;
	}

	@Override
	public boolean validate(String username, GuardedString password) {
		LoginDto loginDto = new LoginDto();
		loginDto.setUsername(username);
		loginDto.setPassword(password);
		try {
			this.authenticate(loginDto);
		} catch (RuntimeException e) {
			return false;
		}
		return true;
	}
}
