package eu.bcvsolutions.idm.core.model.service.impl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.google.common.collect.ImmutableMap;

import eu.bcvsolutions.idm.core.CoreModuleDescriptor;
import eu.bcvsolutions.idm.core.api.domain.CoreResultCode;
import eu.bcvsolutions.idm.core.api.domain.IdmPasswordPolicyType;
import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordDto;
import eu.bcvsolutions.idm.core.api.dto.IdmPasswordPolicyDto;
import eu.bcvsolutions.idm.core.api.dto.IdmTokenDto;
import eu.bcvsolutions.idm.core.api.exception.ResultCodeException;
import eu.bcvsolutions.idm.core.api.service.ConfigurationService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordPolicyService;
import eu.bcvsolutions.idm.core.api.service.IdmPasswordService;
import eu.bcvsolutions.idm.core.api.utils.DtoUtils;
import eu.bcvsolutions.idm.core.api.utils.ExceptionUtils;
import eu.bcvsolutions.idm.core.model.entity.IdmPassword_;
import eu.bcvsolutions.idm.core.notification.api.dto.IdmMessageDto;
import eu.bcvsolutions.idm.core.notification.api.service.NotificationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.AuthenticationManager;
import eu.bcvsolutions.idm.core.security.api.authentication.Authenticator;
import eu.bcvsolutions.idm.core.security.api.domain.AuthenticationResponseEnum;
import eu.bcvsolutions.idm.core.security.api.domain.GuardedString;
import eu.bcvsolutions.idm.core.security.api.domain.IdmJwtAuthentication;
import eu.bcvsolutions.idm.core.security.api.dto.LoginDto;
import eu.bcvsolutions.idm.core.security.api.exception.MustChangePasswordException;
import eu.bcvsolutions.idm.core.security.api.exception.TwoFactorAuthenticationRequiredException;
import eu.bcvsolutions.idm.core.security.api.service.TokenManager;
import eu.bcvsolutions.idm.core.security.service.impl.OAuthAuthenticationManager;

/**
 * Default implementation of authentication manager {@link AuthenticationManager}.
 * 
 * @author Ondrej Kopr
 * @author Radek Tomiška
 */
@Service
public class DefaultAuthenticationManager implements AuthenticationManager {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DefaultAuthenticationManager.class);
	//
	@Autowired
	private List<Authenticator> authenticators;
	@Autowired
	private IdmPasswordService passwordService;
	@Autowired
	private IdmPasswordPolicyService passwordPolicyService;
	@Autowired
	private NotificationManager notificationManager;
	@Autowired
	private ConfigurationService configurationService;
	@Autowired
	private TokenManager tokenManager;
	@Autowired
	private OAuthAuthenticationManager oAuthAuthenticationManager;

	@Override
	public LoginDto authenticate(LoginDto loginDto) {
		List<LoginDto> resultsList = new LinkedList<>();
		RuntimeException firstFailure = null;
		//
		// check if user can log in and hasn't administrator permission
		IdmPasswordDto passwordDto = passwordService.findOrCreateByIdentity(loginDto.getUsername());
		if (passwordDto == null) {
			throw new ResultCodeException(CoreResultCode.AUTH_FAILED, "Invalid login or password.");
		}
		if (passwordDto.getBlockLoginDate() != null && passwordDto.getBlockLoginDate().isAfter(ZonedDateTime.now())) {
			LOG.info("Identity {} has blocked login to IdM.",
					loginDto.getUsername());
			IdmIdentityDto identityDto = DtoUtils.getEmbedded(passwordDto, IdmPassword_.identity);
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(configurationService.getDateTimeSecondsFormat());
			ZonedDateTime blockLoginDate = passwordDto.getBlockLoginDate();
			String dateAsString = blockLoginDate.format(formatter);

			// Block login date can be set manually by password metadata,
			// so block login date can be more than int amount.
			long blockMillies = blockLoginDate.toInstant().toEpochMilli();
			long nowMillis = ZonedDateTime.now().toInstant().toEpochMilli();
			long different = blockMillies - nowMillis;
			different = different / 1000;

			throw new ResultCodeException(CoreResultCode.AUTH_BLOCKED,
					ImmutableMap.of(
							"username", identityDto.getUsername(),
							"date", dateAsString,
							"seconds", different,
							"unsuccessfulAttempts", passwordDto.getUnsuccessfulAttempts()));
		}
		//
		for (Authenticator authenticator : getEnabledAuthenticators()) {
			LOG.debug("AuthenticationManager call authenticate by [{}].", authenticator.getName());
			try {
				LoginDto result = authenticator.authenticate(cloneLoginDto(loginDto));
				if (result == null) { // not done
					// continue, authenticator is not implemented or etc.
					continue;
				}
				if (authenticator.getExceptedResult() == AuthenticationResponseEnum.SUFFICIENT) {
					checkAdditionalAuthenticationRequirements(passwordDto, result);
					passwordDto = passwordService.setLastSuccessfulLogin(passwordDto);
					return result;
				}
				// if otherwise add result too list and continue
				resultsList.add(result);
			} catch (MustChangePasswordException | TwoFactorAuthenticationRequiredException ex) {
				// publish additional authentication requirement
				throw ex;
			} catch (RuntimeException e) {
				// if excepted response is REQUISITE exit immediately with error
				if (authenticator.getExceptedResult() == AuthenticationResponseEnum.REQUISITE) {
					blockLogin(passwordDto, loginDto);
					//
					throw e;
				}
				// if otherwise save first failure into exception
				if (firstFailure == null) {
					firstFailure = e;
				}
			}
		}
		//
		// authenticator is sorted by implement ordered, return first success authenticate authenticator, if don't exist any otherwise throw first failure
		if (resultsList.isEmpty()) {
			blockLogin(passwordDto, loginDto);
			throw firstFailure;
		}
		// 
		LoginDto result = resultsList.get(0);
		checkAdditionalAuthenticationRequirements(passwordDto, result);
		passwordDto = passwordService.setLastSuccessfulLogin(passwordDto);
		//
		return result;
	}
	
	@Override
	public boolean validate(LoginDto loginDto) {
		Assert.notNull(loginDto, "Credentials are required to validate.");
		String username = loginDto.getUsername();
		Assert.hasLength(username, "Username is required to validate credentials.");
		//
		try {
			RuntimeException firstFailture = null;
			boolean result = false;
			//
			IdmPasswordDto passwordDto = passwordService.findOrCreateByIdentity(username);
			if (passwordDto == null) {
				LOG.info("Identity [{}] not exist, password cannot be inited.", username);
				//
				return false;
			}
			if (passwordDto.getBlockLoginDate() != null && passwordDto.getBlockLoginDate().isAfter(ZonedDateTime.now())) {
				LOG.info("Identity [{}] has blocked login to IdM.", username);
				//
				return false;
			}
			//
			for (Authenticator authenticator : getEnabledAuthenticators()) {
				LOG.debug("AuthenticationManager call validate by [{}].", authenticator.getName());
				try {
					boolean validate = authenticator.validate(cloneLoginDto(loginDto));
					if (validate) {
						if(authenticator.getExceptedResult() == AuthenticationResponseEnum.SUFFICIENT) {
							return true;
						}
						result = true; // at least one of optional registered authenticator succeed
					}
				} catch (RuntimeException ex) {
					// if excepted response is REQUISITE exit immediately with error
					if (authenticator.getExceptedResult() == AuthenticationResponseEnum.REQUISITE) {
						if (ex instanceof ResultCodeException) {
							ExceptionUtils.log(LOG, (ResultCodeException) ex);
						} else {
							ExceptionUtils.log(LOG, null, ex);
						}
						//
						blockLogin(passwordDto, loginDto);
						//
						return false;
					}
					// if otherwise save first failure into exception
					if (firstFailture == null) {
						firstFailture = ex;
					}
				}
			}
			//
			// authenticator is sorted by implement ordered, return first success authenticate authenticator, if don't exist any otherwise throw first failure
			if (firstFailture != null) {
				if (firstFailture instanceof ResultCodeException) {
					ExceptionUtils.log(LOG, (ResultCodeException) firstFailture);
				} else {
					ExceptionUtils.log(LOG, null, firstFailture);
				}
				//
				blockLogin(passwordDto, loginDto);
				//
				return false;
			}
			//
			return result;
		} catch (RuntimeException ex) {
			LOG.warn("Authentication validation failed", ex);
			//
			return false;
		}
	}
	
	@Override
	public void logout() {
		IdmTokenDto token = tokenManager.getCurrentToken();
		if (token == null) {
			LOG.debug("Current token not found, logout is not supported (already logged out or authenticated externally without token).");
			return;
		}
		//
		// all registered authenticator should know about logout given token
		for (Authenticator authenticator : getEnabledAuthenticators()) {
			LOG.trace("Process authenticator [{}].", authenticator.getName());
			//
			authenticator.logout(token);
		}
	}
	
	/**
	 * Get enabled {@link Authenticator} and sort by order
	 */
	private List<Authenticator> getEnabledAuthenticators() {
		// disable/enable BE modules
		List<Authenticator> enabledAuthenticator = this.authenticators
				.stream()
				.filter(auth -> !auth.isDisabled())
				.collect(Collectors.toList());
		// sort by ordered
		AnnotationAwareOrderComparator.sort(enabledAuthenticator);
		return enabledAuthenticator;
	}
	
	/**
	 * Process wrong login attempt. If exceeding a maximum attempts then user is
	 * block for the time (and send notification to user). Otherwise just increase
	 * attempts.
	 *
	 * @param passwordDto
	 * @param loginDto
	 */
	private void blockLogin(IdmPasswordDto passwordDto, LoginDto loginDto) {
		Assert.notNull(passwordDto, "Password DTO is required for block login.");
		// In first increase unsuccessful attempts
		passwordDto.increaseUnsuccessfulAttempts();

		IdmPasswordPolicyDto validatePolicy = passwordPolicyService.getDefaultPasswordPolicy(IdmPasswordPolicyType.VALIDATE);
		
		if (validatePolicy == null) {
			LOG.warn("Default validation policy doesn't exist! Please setup correctly this policy type. CzechIdM without validation polcity is security risk");
			passwordDto = passwordService.save(passwordDto); // Increase unsuccessful attempts
			return;
		}

		Integer maxUnsuccessfulAttempts = validatePolicy.getMaxUnsuccessfulAttempts();
		if (maxUnsuccessfulAttempts == null) {
			LOG.warn("For default validation policy isn't set max unsuccessful attempts! Please setup correctly this policy type. CzechIdM without validation polcity is security risk");
			passwordDto = passwordService.save(passwordDto); // Increase unsuccessful attempts
			return;
		}

		Integer blockForSeconds = validatePolicy.getBlockLoginTime();
		if (blockForSeconds == null) {
			LOG.warn("For default validation policy isn't set block time! Please setup correctly this policy type. CzechIdM without validation polcity is security risk");
			passwordDto = passwordService.save(passwordDto); // Increase unsuccessful attempts
			return;
		}

		int currentUnsuccessfulAttempts = passwordDto.getUnsuccessfulAttempts();
		int remainder = currentUnsuccessfulAttempts % maxUnsuccessfulAttempts;

		// If remainder is 0 the attempts reached new block
		// Eq: 4 attempts and 4 is maximum = remainder = 0 => first block reached,
		// 6 attempts 4 is maximum = remainder = 2 => inside first block
		// 8 attempts 4 is maximum = remainder = 0 => second block reached
		if (remainder == 0) {
			// Multiplier is for increase block time
			int multiplier = currentUnsuccessfulAttempts / maxUnsuccessfulAttempts;
			int seconds = blockForSeconds * multiplier;
			ZonedDateTime blockFinalTime = ZonedDateTime.now().plusSeconds(seconds);
			passwordDto.setBlockLoginDate(blockFinalTime);
			passwordDto = passwordService.save(passwordDto);
			IdmIdentityDto identityDto = DtoUtils.getEmbedded(passwordDto, IdmPassword_.identity);

			DateTimeFormatter formatter = DateTimeFormatter.ofPattern(configurationService.getDateTimeSecondsFormat());
			String dateAsString = passwordDto.getBlockLoginDate().format(formatter);
			//
			LOG.warn("For identity username: {} was lock authentization to IdM for {} seconds. Authentization will be available after: {}.",
					loginDto.getUsername(), blockFinalTime, dateAsString);
			// send notification to identity
			notificationManager.send(CoreModuleDescriptor.TOPIC_LOGIN_BLOCKED,
					new IdmMessageDto.Builder()
					.addParameter("username", loginDto.getUsername())
					.addParameter("after", dateAsString)
					.addParameter("unsuccessfulAttempts", passwordDto.getUnsuccessfulAttempts())
					.build(),
					identityDto);

			throw new ResultCodeException(CoreResultCode.AUTH_BLOCKED,
					ImmutableMap.of(
							"username", identityDto.getUsername(),
							"date", dateAsString,
							"seconds", seconds,
							"unsuccessfulAttempts", passwordDto.getUnsuccessfulAttempts()));
		} else {
			// Just increase attempts
			passwordDto = passwordService.save(passwordDto);
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
		//
		return validate(loginDto);
	}
	
	/**
	 * check if user must change password, skip this check if loginDto contains flag
	 * @param passwordDto
	 * @param loginDto result login with token
	 */
	private void checkAdditionalAuthenticationRequirements(IdmPasswordDto passwordDto, LoginDto loginResult) {
		Assert.notNull(passwordDto, "IdM password instance is required.");
		Assert.notNull(loginResult, "Login result is required.");
		//
		if (loginResult.isSkipMustChange()) {
			return;
		}
		//
		// check if user must change password, skip this check if loginDto contains flag
		if (passwordDto.isMustChange()) {
			oAuthAuthenticationManager.logout();
			throw new MustChangePasswordException(loginResult.getUsername());
		}
	}
}
