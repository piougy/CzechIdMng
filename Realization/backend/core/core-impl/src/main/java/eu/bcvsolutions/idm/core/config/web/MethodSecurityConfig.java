package eu.bcvsolutions.idm.core.config.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Injects role hierarchy to prePostEnabled annotations 
 * 
 * @author Radek Tomiška
 *
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class MethodSecurityConfig extends GlobalMethodSecurityConfiguration  {

	@Autowired
	private RoleHierarchy roleHierarchy;
	@Autowired
	private ApplicationContext context;

	@Override
	protected MethodSecurityExpressionHandler createExpressionHandler() {
	    DefaultMethodSecurityExpressionHandler expressionHandler = (DefaultMethodSecurityExpressionHandler) super.createExpressionHandler();
	    expressionHandler.setRoleHierarchy(roleHierarchy);
	    expressionHandler.setApplicationContext(context);
	    return expressionHandler;
	}
}
