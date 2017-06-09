package eu.bcvsolutions.idm.core.config.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.query.spi.EvaluationContextExtension;
import org.springframework.data.repository.query.spi.EvaluationContextExtensionSupport;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.expression.SecurityExpressionHandler;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.FilterInvocation;
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.security.auth.filter.AuthenticationFilter;
import eu.bcvsolutions.idm.core.security.auth.filter.ExtendExpirationFilter;

/**
 * Web security configuration
 * 
 * @author Radek Tomiška 
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private RoleHierarchy roleHierarchy;

	@Override
    protected void configure(HttpSecurity http) throws Exception {
    	 http.csrf().disable();
    	 http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    	 
    	 http
    	 	.addFilterAfter(authenticationFilter(), BasicAuthenticationFilter.class)
    	 	.addFilterAfter(extendExpirationFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
			.expressionHandler(expressionHandler())
			.antMatchers(HttpMethod.OPTIONS).permitAll()
			.antMatchers(BaseDtoController.BASE_PATH + "/public/**").permitAll()
			.antMatchers(BaseDtoController.BASE_PATH + "/websocket-info/**").permitAll() // websockets has their own security configuration
			.antMatchers(BaseDtoController.BASE_PATH + "/**").fullyAuthenticated() // TODO: controllers should choose security?
			.anyRequest().permitAll(); // gui could run in application context
    }

	@Override
	public void configure(WebSecurity web) throws Exception {
		// public controllers
		web.ignoring().antMatchers( //
				BaseDtoController.BASE_PATH, // endpoint with supported services list
				BaseDtoController.BASE_PATH + "/authentication", // login / out
				"/error/**",
				BaseDtoController.BASE_PATH + "/doc", // documentation is public
				BaseDtoController.BASE_PATH + "/doc/**",
				BaseDtoController.BASE_PATH + "/status" // status page
			);
	}
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	@Bean
	public AuthenticationFilter authenticationFilter() {
		return new AuthenticationFilter();
	}
	
	@Bean
	public ExtendExpirationFilter extendExpirationFilter() {
		return new ExtendExpirationFilter();
	}
	
	/**
	 * Inherit security context from parent thread
	 * 
	 * @return
	 */
	@Bean
	public MethodInvokingFactoryBean methodInvokingFactoryBean() {
	    MethodInvokingFactoryBean methodInvokingFactoryBean = new MethodInvokingFactoryBean();
	    methodInvokingFactoryBean.setTargetClass(SecurityContextHolder.class);
	    methodInvokingFactoryBean.setTargetMethod("setStrategyName");
	    methodInvokingFactoryBean.setArguments(new String[]{SecurityContextHolder.MODE_INHERITABLETHREADLOCAL});
	    return methodInvokingFactoryBean;
	}
	
	/**
	 * Support hasAuthority etc. in search queries
	 * 
	 * @return
	 */
	@Bean
	public EvaluationContextExtension securityExtension() {
		return new EvaluationContextExtensionSupport() {
			
			@Override
			public String getExtensionId() {
				return "security";
			}

			@Override
			public SecurityExpressionRoot getRootObject() {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				SecurityExpressionRoot root = new SecurityExpressionRoot(authentication) {};
				root.setRoleHierarchy(roleHierarchy);
				return root;
			}
		};
	}
	
	/**
	 * Inject role hierarchy to HttpSecurity expressions
	 * 
	 * @return
	 */
	private SecurityExpressionHandler<FilterInvocation> expressionHandler() {
        DefaultWebSecurityExpressionHandler defaultWebSecurityExpressionHandler = new DefaultWebSecurityExpressionHandler();
        defaultWebSecurityExpressionHandler.setRoleHierarchy(roleHierarchy);
        return defaultWebSecurityExpressionHandler;
    }
}
