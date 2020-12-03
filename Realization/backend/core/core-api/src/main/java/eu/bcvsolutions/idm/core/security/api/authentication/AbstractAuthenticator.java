package eu.bcvsolutions.idm.core.security.api.authentication;

import java.text.MessageFormat;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.bcvsolutions.idm.core.api.dto.IdmIdentityDto;
import eu.bcvsolutions.idm.core.api.service.LookupService;
import eu.bcvsolutions.idm.core.security.api.exception.IdmAuthenticationException;
import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Abstract authenticator, check if is module enabled
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 * @author Radek Tomiška
 */
public abstract class AbstractAuthenticator implements Authenticator {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractAuthenticator.class);
	//
	@Autowired(required = false)
	private EnabledEvaluator enabledEvaluator; // optional internal dependency - checks for module is enabled
	@Autowired
	private LookupService lookupService;
	
	@Override
	public int getOrder() {
		return DEFAULT_AUTHENTICATOR_ORDER;
	}
	
	@Override
	public boolean isDisabled() {
		// check for module is enabled, if evaluator is given
		if (enabledEvaluator != null && !enabledEvaluator.isEnabled(this.getClass())) {
			return true;
		}
		return false;
	}
	
	/**
	 * Get valid identity by username.
	 * 
	 * @param loginDto input 
	 * @param propagateException authenticate / validate usage
	 * @return valid identity, {@code null} or exception
	 * @since 10.7.0
	 */
	protected IdmIdentityDto getValidIdentity(String username, boolean propagateException) {
		Assert.hasLength(username, "Identity username is required.");
		IdmIdentityDto identity = (IdmIdentityDto) lookupService.lookupDto(IdmIdentityDto.class, username);
		//
		// identity exists
		if (identity == null) {	
			String validationMessage = MessageFormat.format("Check identity can login: The identity "
					+ "[{0}] either doesn't exist or is deleted.", username);
			if (!propagateException) {
				LOG.debug(validationMessage);
				return null;
			}
			throw new IdmAuthenticationException(validationMessage);
		}
		//
		// valid identity
		if (identity.isDisabled()) {
			String validationMessage = MessageFormat.format("Check identity can login: The identity [{0}] is disabled.", username);
			if (!propagateException) {
				LOG.debug(validationMessage);
				return null;
			}
			throw new IdmAuthenticationException(validationMessage);
		}
		//
		return identity;
	}
}
