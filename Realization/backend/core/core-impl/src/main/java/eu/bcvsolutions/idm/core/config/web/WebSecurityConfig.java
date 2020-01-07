package eu.bcvsolutions.idm.core.config.web;

import java.lang.reflect.Method;
import java.util.Set;

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.spel.spi.EvaluationContextExtension;
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
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.filter.RequestContextFilter;

import com.google.common.collect.Sets;

import eu.bcvsolutions.idm.core.api.rest.BaseDtoController;
import eu.bcvsolutions.idm.core.api.rest.PublicController;
import eu.bcvsolutions.idm.core.security.api.auth.filter.AuthenticationFilter;
import eu.bcvsolutions.idm.core.security.auth.filter.ExtendExpirationFilter;
import eu.bcvsolutions.idm.core.security.auth.filter.StartUserTransactionFilter;

/**
 * Web security configuration.
 * 
 * @author Radek Tomiška 
 *
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebSecurityConfig.class);
	//
	@Autowired private RoleHierarchy roleHierarchy;
	@Autowired private ApplicationContext context;

	@Override
    protected void configure(HttpSecurity http) throws Exception {
    	 http.csrf().disable();
    	 http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    	 //
    	 Set<String> publicPaths = getPublicPaths();
    	 LOG.debug("Resolved public paths [{}]", publicPaths);
    	 //
    	 http
    	 	.addFilterBefore(requestContextFilter(), BasicAuthenticationFilter.class)
    	 	.addFilterBefore(startUserTransactionFilter(), BasicAuthenticationFilter.class)
    	 	.addFilterAfter(authenticationFilter(), BasicAuthenticationFilter.class)
    	 	.addFilterAfter(extendExpirationFilter(), BasicAuthenticationFilter.class)
			.authorizeRequests()
			.expressionHandler(expressionHandler())
			.antMatchers(HttpMethod.OPTIONS).permitAll()
			.antMatchers(publicPaths.toArray(new String[publicPaths.size()])).permitAll()
			.antMatchers(BaseDtoController.BASE_PATH + "/**").fullyAuthenticated() // TODO: controllers should choose security?
			.anyRequest().permitAll(); // gui could run in application context
    }

	@Override
	public void configure(WebSecurity web) throws Exception {
		web // allow url encoded params
			.httpFirewall(allowUrlEncodedSlashHttpFirewall())
			// public controllers
			.ignoring()
			.antMatchers( //
					BaseDtoController.BASE_PATH, // endpoint with supported services list
					BaseDtoController.BASE_PATH + "/authentication", // login
					"/error/**",
					BaseDtoController.BASE_PATH + "/doc", // documentation is public
					BaseDtoController.BASE_PATH + "/doc/**"
					);
	}
	
	/**
	 * Resolve public paths
	 * 
	 * @return
	 */
	private Set<String> getPublicPaths() {
		Set<String> publicPaths = Sets.newHashSet(
				BaseDtoController.BASE_PATH + "/public/**", // controllers with public prefix is public by default
				BaseDtoController.BASE_PATH + "/websocket-info/**"); // websockets has their own security configuration
		context
			.getBeansOfType(PublicController.class)
			.values()
			.forEach(publicController -> {
				Class<?> clazz = AopUtils.getTargetClass(publicController);
			    if (clazz.isAnnotationPresent(RequestMapping.class)) {
			    	RequestMapping mapping = clazz.getAnnotation(RequestMapping.class);
					publicPaths.addAll(Sets.newHashSet(mapping.value()));
					publicPaths.addAll(Sets.newHashSet(mapping.path()));
				}
				// controller methods mapping should be public too
				while (clazz != Object.class) {
			        for (Method method : clazz.getDeclaredMethods()) {
			            if (method.isAnnotationPresent(RequestMapping.class)) {
			            	RequestMapping mapping = method.getAnnotation(RequestMapping.class);
		                	publicPaths.addAll(Sets.newHashSet(mapping.value()));
		                	publicPaths.addAll(Sets.newHashSet(mapping.path()));
			            }
			        }
			        clazz = clazz.getSuperclass();
			    }
			});
		return publicPaths;
	}
	
	@Bean
	public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
	    StrictHttpFirewall firewall = new StrictHttpFirewall();
	    firewall.setAllowUrlEncodedSlash(true);
	    firewall.setAllowSemicolon(true);
	    firewall.setAllowUrlEncodedPercent(true);
	    //
	    return firewall;
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
	 * User transaction holder.
	 * 
	 * @return
	 */
	@Bean
	public StartUserTransactionFilter startUserTransactionFilter() {
		return new StartUserTransactionFilter();
	}
	
	/**
	 * Request scope bean will be available in deferred requests.
	 * 
	 * @return
	 */
	@Bean
	public RequestContextFilter requestContextFilter() {
	    return new RequestContextFilter();
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
	    methodInvokingFactoryBean.setArguments(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
	    return methodInvokingFactoryBean;
	}
	
	/**
	 * Support hasAuthority etc. in search queries
	 * 
	 * @return
	 */
	@Bean
	public EvaluationContextExtension securityExtension() {
		return new EvaluationContextExtension() {
			
			@Override
			public String getExtensionId() {
				return "security";
			}

			@Override
			public SecurityExpressionRoot getRootObject() {
				Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
				if (authentication == null) {
					// not authenticated
					return null;
				}
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
