package eu.bcvsolutions.idm.core.config.domain;

import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;

public class IdmDefaultMethodSecurityExpressionHandler extends DefaultWebSecurityExpressionHandler {
	
	private final AuthenticationTrustResolver trustResolver = new AuthenticationTrustResolverImpl();
	
	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(
			Authentication authentication, FilterInvocation invocation) {
		IdmSecurityExpressionRoot root = new IdmSecurityExpressionRoot(
				authentication);
        root.setPermissionEvaluator(getPermissionEvaluator());
        root.setTrustResolver(trustResolver);
		return root;
	}
}
