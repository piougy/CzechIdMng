package eu.bcvsolutions.idm.core.security.api.authentication;

import org.springframework.beans.factory.annotation.Autowired;

import eu.bcvsolutions.idm.core.security.api.service.EnabledEvaluator;

/**
 * Abstract authenticator, check if is moduel enabled
 * 
 * @author Ondrej Kopr <kopr@xyxy.cz>
 */

public abstract class AbstractAuthenticator implements Authenticator {
	
	@Autowired(required = false)
	private EnabledEvaluator enabledEvaluator; // optional internal dependency - checks for module is enabled
	
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
}
