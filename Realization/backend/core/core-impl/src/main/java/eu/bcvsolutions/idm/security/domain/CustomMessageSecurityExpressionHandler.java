package eu.bcvsolutions.idm.security.domain;

import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;

/**
 * Preset authentication from stomp message header
 * 
 * @author Radek Tomiška
 *
 * @param <T>
 */
public class CustomMessageSecurityExpressionHandler<T> extends DefaultMessageSecurityExpressionHandler<T> {
	
	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(
			Authentication authentication, Message<T> invocation) {
		Authentication simpleUserAuth = invocation.getHeaders().get(SimpMessageHeaderAccessor.USER_HEADER, Authentication.class);
		return super.createSecurityExpressionRoot(simpleUserAuth == null ? authentication : simpleUserAuth, invocation);
	}
}
