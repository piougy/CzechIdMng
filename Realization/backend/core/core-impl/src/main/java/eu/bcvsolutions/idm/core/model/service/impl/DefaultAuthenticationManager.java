package eu.bcvsolutions.idm.core.model.service.impl;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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
	
	@Autowired
	private ApplicationContext context;
	
	private List<Authenticator> authenticators;
	
	@Override
	public LoginDto authenticate(LoginDto loginDto) {
		// init authenticator
		initAuthenticators();
		
		// authenticate
		return autneticateOverAuthenticator(loginDto);
	}
	
	/**
	 * If authenticators is empty or null, init it again.
	 */
	private void initAuthenticators() {
		// reload every time, dynamically disable/enable BE modules
		this.authenticators = new LinkedList<>(this.context.getBeansOfType(Authenticator.class).values());
		// sort by ordered
		AnnotationAwareOrderComparator.sort(this.authenticators);
	}
	
	/**
	 * Authenticate {@link LoginDto} over all founded {@link Authenticator}
	 * 
	 * @param loginDto
	 */
	private LoginDto autneticateOverAuthenticator(LoginDto loginDto) {
		Assert.notNull(authenticators);
		//
		List<LoginDto> resultsList = new LinkedList<>();
		RuntimeException firstFailture = null;
		//
		for(Authenticator authenticator : authenticators) {
			LOG.debug("AuthenticationManager call authenticate by [{}].", authenticator.getName());
			try {
				LoginDto result = authenticator.authenticate(cloneLoginDto(loginDto));
				if (result == null) { // not done
					// continue, authenticator is not implemented or etc.
					continue;
				}
				if (authenticator.getResponse().equals(AuthenticationResponseEnum.SUFFICIENT)) {
					return result;
				}
				// if otherwise add result too list and continue
				resultsList.add(result);
			} catch (RuntimeException e) {
				// if excepted response is REQUISITE exit immediately with error
				if (authenticator.getResponse().equals(AuthenticationResponseEnum.REQUISITE)) {
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
			throw firstFailture;
		}
		return resultsList.get(0);
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
	public boolean authenticate(String username, GuardedString password) {
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
